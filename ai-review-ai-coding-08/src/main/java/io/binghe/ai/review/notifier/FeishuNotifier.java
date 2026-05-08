package io.binghe.ai.review.notifier;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.binghe.ai.review.constants.AiReviewConstants;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author binghe
 * @version 1.0.0
 * @description Feishu notifier
 */
@Slf4j
@Component
public class FeishuNotifier {

    @Value("${notification.feishu.enabled:false}")
    private boolean enabled;

    @Value("${notification.feishu.webhook-url:}")
    private String defaultWebhookUrl;

    @Value("${notification.feishu.max-markdown-bytes:8000}")
    private int maxMarkdownBytes;

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public FeishuNotifier() {
        this.objectMapper = new ObjectMapper();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    private String getWebhookUrl(String projectName, String urlSlug) {
        if (projectName != null && !projectName.isBlank()) {
            String envKey = AiReviewConstants.ENV_FEISHU_WEBHOOK_URL_PREFIX + projectName.toUpperCase().replace("-", "_");
            String url = System.getenv(envKey);
            if (url != null && !url.isBlank()) return url;
        }
        if (urlSlug != null && !urlSlug.isBlank()) {
            String envKey = AiReviewConstants.ENV_FEISHU_WEBHOOK_URL_PREFIX + urlSlug.toUpperCase();
            String url = System.getenv(envKey);
            if (url != null && !url.isBlank()) return url;
        }
        return defaultWebhookUrl;
    }

    public void sendMarkdown(String content) {
        sendMarkdown(content, null, null, null);
    }

    /**
     * Send Feishu card message with title and markdown content.
     */
    public void sendMarkdown(String content, String title, String projectName, String urlSlug) {
        if (!enabled) {
            log.debug("Feishu notification disabled");
            return;
        }

        String postUrl = getWebhookUrl(projectName, urlSlug);
        if (postUrl == null || postUrl.isBlank()) {
            log.debug("Feishu webhook URL is not configured");
            return;
        }

        String safeContent = content != null ? content : "";
        int safeMaxBytes = Math.max(maxMarkdownBytes, 4);
        if (safeContent.getBytes(StandardCharsets.UTF_8).length <= safeMaxBytes) {
            sendMarkdownCard(postUrl, safeContent, title);
            return;
        }

        List<String> chunks = splitContent(safeContent, safeMaxBytes);
        log.warn("Feishu markdown content exceeds {} bytes, splitting. totalBytes: {}, chunks: {}",
                safeMaxBytes, safeContent.getBytes(StandardCharsets.UTF_8).length, chunks.size());
        for (int i = 0; i < chunks.size(); i++) {
            String chunkTitle = buildChunkTitle(title, i + 1, chunks.size());
            sendMarkdownCard(postUrl, chunks.get(i), chunkTitle);
        }
    }

    public void sendText(String content, String projectName, String urlSlug) {
        if (!enabled) {
            return;
        }

        String postUrl = getWebhookUrl(projectName, urlSlug);
        if (postUrl == null || postUrl.isBlank()) {
            return;
        }

        try {
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("msg_type", "text");
            ObjectNode contentNode = objectMapper.createObjectNode();
            contentNode.put("text", content);
            payload.set("content", contentNode);

            RequestBody requestBody = RequestBody.create(
                    objectMapper.writeValueAsString(payload),
                    MediaType.parse(AiReviewConstants.MEDIA_TYPE_JSON)
            );

            Request request = new Request.Builder()
                    .url(postUrl)
                    .addHeader(AiReviewConstants.HEADER_CONTENT_TYPE, "application/json")
                    .post(requestBody)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                log.info("Feishu text message sent with status: {}", response.code());
            }
        } catch (Exception e) {
            log.error("Feishu text message failed! {}", e.getMessage(), e);
        }
    }

    private void sendMarkdownCard(String postUrl, String content, String title) {
        try {
            ObjectNode payload = buildMarkdownPayload(content, title);

            RequestBody body = RequestBody.create(
                    objectMapper.writeValueAsString(payload),
                    MediaType.parse(AiReviewConstants.MEDIA_TYPE_JSON)
            );

            Request request = new Request.Builder()
                    .url(postUrl)
                    .addHeader(AiReviewConstants.HEADER_CONTENT_TYPE, "application/json")
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                String respBody = response.body() != null ? response.body().string() : "{}";
                if (response.isSuccessful()) {
                    com.fasterxml.jackson.databind.JsonNode respJson = objectMapper.readTree(respBody);
                    if ("success".equals(respJson.path("msg").asText())) {
                        log.info("Feishu message sent successfully! webhook_url:{}", postUrl);
                    } else {
                        log.error("Feishu message failed! webhook_url:{}, errmsg:{}", postUrl, respJson);
                    }
                } else {
                    log.error("Feishu message failed! HTTP status: {}, body:{}", response.code(), respBody);
                }
            }
        } catch (Exception e) {
            log.error("Feishu message failed! {}", e.getMessage(), e);
        }
    }

    private ObjectNode buildMarkdownPayload(String content, String title) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("msg_type", "interactive");

        ObjectNode card = objectMapper.createObjectNode();
        card.put("schema", "2.0");

        ObjectNode config = objectMapper.createObjectNode();
        config.put("update_multi", true);
        ObjectNode textSize = objectMapper.createObjectNode();
        ObjectNode normalV2 = objectMapper.createObjectNode();
        normalV2.put("default", "normal");
        normalV2.put("pc", "normal");
        normalV2.put("mobile", "heading");
        textSize.set("normal_v2", normalV2);
        ObjectNode style = objectMapper.createObjectNode();
        style.set("text_size", textSize);
        config.set("style", style);
        card.set("config", config);

        ObjectNode body = objectMapper.createObjectNode();
        body.put("direction", "vertical");
        body.put("padding", "12px 12px 12px 12px");
        ArrayNode elements = objectMapper.createArrayNode();
        ObjectNode markdownElement = objectMapper.createObjectNode();
        markdownElement.put("tag", "markdown");
        markdownElement.put("content", content);
        markdownElement.put("text_align", "left");
        markdownElement.put("text_size", "normal_v2");
        markdownElement.put("margin", "0px 0px 0px 0px");
        elements.add(markdownElement);
        body.set("elements", elements);
        card.set("body", body);

        if (title != null && !title.isBlank()) {
            ObjectNode header = objectMapper.createObjectNode();
            ObjectNode headerTitle = objectMapper.createObjectNode();
            headerTitle.put("tag", "plain_text");
            headerTitle.put("content", title);
            header.set("title", headerTitle);
            header.put("template", "blue");
            header.put("padding", "12px 12px 12px 12px");
            card.set("header", header);
        }

        payload.set("card", card);
        return payload;
    }

    private String buildChunkTitle(String title, int current, int total) {
        String baseTitle = title != null && !title.isBlank() ? title : "Feishu message";
        return baseTitle + " (\u7b2c " + current + "/" + total + " \u90e8\u5206)";
    }

    List<String> splitContent(String content, int maxBytes) {
        List<String> chunks = new ArrayList<>();
        if (content == null || content.isEmpty()) {
            chunks.add("");
            return chunks;
        }

        int safeMaxBytes = Math.max(maxBytes, 4);
        int start = 0;
        while (start < content.length()) {
            int end = findMaxEndByUtf8Bytes(content, start, safeMaxBytes);
            if (end >= content.length()) {
                chunks.add(content.substring(start));
                break;
            }

            int newlineIndex = content.lastIndexOf('\n', end - 1);
            if (newlineIndex >= start) {
                end = newlineIndex + 1;
            }
            chunks.add(content.substring(start, end));
            start = end;
        }
        return chunks;
    }

    private int findMaxEndByUtf8Bytes(String content, int start, int maxBytes) {
        int bytes = 0;
        int index = start;
        while (index < content.length()) {
            int codePoint = content.codePointAt(index);
            int codePointBytes = utf8Bytes(codePoint);
            if (bytes + codePointBytes > maxBytes) {
                break;
            }
            bytes += codePointBytes;
            index += Character.charCount(codePoint);
        }
        if (index == start) {
            return start + Character.charCount(content.codePointAt(start));
        }
        return index;
    }

    private int utf8Bytes(int codePoint) {
        if (codePoint <= 0x7F) {
            return 1;
        }
        if (codePoint <= 0x7FF) {
            return 2;
        }
        if (codePoint <= 0xFFFF) {
            return 3;
        }
        return 4;
    }
}

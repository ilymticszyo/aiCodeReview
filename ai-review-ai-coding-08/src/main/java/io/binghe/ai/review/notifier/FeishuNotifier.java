package io.binghe.ai.review.notifier;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.binghe.ai.review.constants.AiReviewConstants;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author binghe(微信 : hacker_binghe)
 * @version 1.0.0
 * @github https://github.com/binghe001
 * @copyright 公众号: 冰河技术
 * @description 飞书通知器
 *  * 支持按项目名或URL slug路由不同webhook URL
 *  * 使用飞书卡片消息格式（带header和body）
 */
@Slf4j
@Component
public class FeishuNotifier {

    @Value("${notification.feishu.enabled:false}")
    private boolean enabled;

    @Value("${notification.feishu.webhook-url:}")
    private String defaultWebhookUrl;

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
     * 发送飞书卡片消息（带标题和内容）
     * 使用飞书2.0卡片格式，支持Markdown内容
     */
    public void sendMarkdown(String content, String title, String projectName, String urlSlug) {
        if (!enabled) {
            log.debug("飞书推送未启用");
            return;
        }

        String postUrl = getWebhookUrl(projectName, urlSlug);
        if (postUrl == null || postUrl.isBlank()) {
            log.debug("未配置飞书webhook URL");
            return;
        }

        try {
            // 构建飞书卡片消息（schema 2.0，带header和body）
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("msg_type", "interactive");

            ObjectNode card = objectMapper.createObjectNode();
            card.put("schema", "2.0");

            // config
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

            // body
            ObjectNode body = objectMapper.createObjectNode();
            body.put("direction", "vertical");
            body.put("padding", "12px 12px 12px 12px");
            com.fasterxml.jackson.databind.node.ArrayNode elements = objectMapper.createArrayNode();
            ObjectNode markdownElement = objectMapper.createObjectNode();
            markdownElement.put("tag", "markdown");
            markdownElement.put("content", content);
            markdownElement.put("text_align", "left");
            markdownElement.put("text_size", "normal_v2");
            markdownElement.put("margin", "0px 0px 0px 0px");
            elements.add(markdownElement);
            body.set("elements", elements);
            card.set("body", body);

            // header（仅当有标题时添加）
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

            RequestBody body2 = RequestBody.create(
                    objectMapper.writeValueAsString(payload),
                    MediaType.parse(AiReviewConstants.MEDIA_TYPE_JSON)
            );

            Request request = new Request.Builder()
                    .url(postUrl)
                    .addHeader(AiReviewConstants.HEADER_CONTENT_TYPE, "application/json")
                    .post(body2)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                String respBody = response.body() != null ? response.body().string() : "{}";
                if (response.isSuccessful()) {
                    com.fasterxml.jackson.databind.JsonNode respJson = objectMapper.readTree(respBody);
                    if ("success".equals(respJson.path("msg").asText())) {
                        log.info("飞书消息发送成功! webhook_url:{}", postUrl);
                    } else {
                        log.error("飞书消息发送失败! webhook_url:{}, errmsg:{}", postUrl, respJson);
                    }
                } else {
                    log.error("飞书消息发送失败! HTTP状态码: {}, body:{}", response.code(), respBody);
                }
            }
        } catch (Exception e) {
            log.error("飞书消息发送失败! {}", e.getMessage(), e);
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
                log.info("飞书文本消息发送结果: {}", response.code());
            }
        } catch (Exception e) {
            log.error("飞书文本消息发送失败! {}", e.getMessage(), e);
        }
    }
}

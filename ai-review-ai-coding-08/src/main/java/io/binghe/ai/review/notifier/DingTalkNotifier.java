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
 * @description 钉钉通知器
 *  * 支持按项目名或URL slug路由不同webhook URL
 *  * 环境变量格式: DINGTALK_WEBHOOK_URL_{PROJECT_NAME} 或 DINGTALK_WEBHOOK_URL_{URL_SLUG}
 */
@Slf4j
@Component
public class DingTalkNotifier {

    @Value("${notification.dingtalk.enabled:false}")
    private boolean enabled;

    @Value("${notification.dingtalk.webhook-url:}")
    private String defaultWebhookUrl;

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public DingTalkNotifier() {
        this.objectMapper = new ObjectMapper();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 获取项目对应的webhook URL（支持per-project路由）
     */
    private String getWebhookUrl(String projectName, String urlSlug) {
        if (projectName != null && !projectName.isBlank()) {
            String envKey = AiReviewConstants.ENV_DINGTALK_WEBHOOK_URL_PREFIX + projectName.toUpperCase().replace("-", "_");
            String url = System.getenv(envKey);
            if (url != null && !url.isBlank()) return url;
        }
        if (urlSlug != null && !urlSlug.isBlank()) {
            String envKey = AiReviewConstants.ENV_DINGTALK_WEBHOOK_URL_PREFIX + urlSlug.toUpperCase();
            String url = System.getenv(envKey);
            if (url != null && !url.isBlank()) return url;
        }
        return defaultWebhookUrl;
    }

    public void sendMarkdown(String title, String content) {
        sendMarkdown(title, content, false, null, null);
    }

    public void sendMarkdown(String title, String content, boolean isAtAll, String projectName, String urlSlug) {
        if (!enabled) {
            log.debug("钉钉推送未启用");
            return;
        }

        String postUrl = getWebhookUrl(projectName, urlSlug);
        if (postUrl == null || postUrl.isBlank()) {
            log.debug("未配置钉钉webhook URL");
            return;
        }

        try {
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("msgtype", "markdown");
            ObjectNode markdown = objectMapper.createObjectNode();
            markdown.put("title", title);
            markdown.put("text", content);
            payload.set("markdown", markdown);
            ObjectNode at = objectMapper.createObjectNode();
            at.put("isAtAll", isAtAll);
            payload.set("at", at);

            RequestBody body = RequestBody.create(
                    objectMapper.writeValueAsString(payload),
                    MediaType.parse(AiReviewConstants.MEDIA_TYPE_JSON)
            );

            Request request = new Request.Builder()
                    .url(postUrl)
                    .addHeader(AiReviewConstants.HEADER_CONTENT_TYPE, "application/json")
                    .addHeader(AiReviewConstants.HEADER_CHARSET, AiReviewConstants.CHARSET_UTF8)
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String respBody = response.body() != null ? response.body().string() : "{}";
                    com.fasterxml.jackson.databind.JsonNode respJson = objectMapper.readTree(respBody);
                    if ("ok".equals(respJson.path("errmsg").asText())) {
                        log.info("钉钉消息发送成功! webhook_url:{}", postUrl);
                    } else {
                        log.error("钉钉消息发送失败! webhook_url:{}, errmsg:{}", postUrl, respJson.path("errmsg").asText());
                    }
                } else {
                    log.error("钉钉消息发送失败! HTTP状态码: {}", response.code());
                }
            }
        } catch (Exception e) {
            log.error("钉钉消息发送失败! {}", e.getMessage(), e);
        }
    }

    public void sendText(String content) {
        sendText(content, false, null, null);
    }

    public void sendText(String content, boolean isAtAll, String projectName, String urlSlug) {
        if (!enabled) {
            log.debug("钉钉推送未启用");
            return;
        }

        String postUrl = getWebhookUrl(projectName, urlSlug);
        if (postUrl == null || postUrl.isBlank()) {
            return;
        }

        try {
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("msgtype", "text");
            ObjectNode text = objectMapper.createObjectNode();
            text.put("content", content);
            payload.set("text", text);
            ObjectNode at = objectMapper.createObjectNode();
            at.put("isAtAll", isAtAll);
            payload.set("at", at);

            RequestBody body = RequestBody.create(
                    objectMapper.writeValueAsString(payload),
                    MediaType.parse(AiReviewConstants.MEDIA_TYPE_JSON)
            );

            Request request = new Request.Builder()
                    .url(postUrl)
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                log.info("钉钉文本消息发送结果: {}", response.code());
            }
        } catch (Exception e) {
            log.error("钉钉文本消息发送失败! {}", e.getMessage(), e);
        }
    }
}

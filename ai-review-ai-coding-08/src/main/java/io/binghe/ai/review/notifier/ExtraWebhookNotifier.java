package io.binghe.ai.review.notifier;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.binghe.ai.review.constants.AiReviewConstants;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author binghe(微信 : hacker_binghe)
 * @version 1.0.0
 * @github https://github.com/binghe001
 * @copyright 公众号: 冰河技术
 * @description 扩展WebHook通知
 */
@Slf4j
@Component
public class ExtraWebhookNotifier {

    @Value("${notification.extra-webhook.enabled:false}")
    private boolean enabled;

    @Value("${notification.extra-webhook.url:}")
    private String webhookUrl;

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ExtraWebhookNotifier() {
        this.objectMapper = new ObjectMapper();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 发送额外自定义webhook消息
     * @param systemData 系统消息内容（content, msg_type, title, project_name等）
     * @param webhookData 原始webhook数据（github/gitlab的push/merge event数据）
     */
    public void sendMessage(Map<String, Object> systemData, Map<String, Object> webhookData) {
        if (!enabled || webhookUrl == null || webhookUrl.isBlank()) {
            log.debug("ExtraWebhook推送未启用");
            return;
        }

        try {
            ObjectNode payload = objectMapper.createObjectNode();
            payload.set("ai_codereview_data", objectMapper.valueToTree(systemData));
            payload.set("webhook_data", objectMapper.valueToTree(webhookData));

            RequestBody body = RequestBody.create(
                    objectMapper.writeValueAsString(payload),
                    MediaType.parse(AiReviewConstants.MEDIA_TYPE_JSON)
            );

            Request request = new Request.Builder()
                    .url(webhookUrl)
                    .addHeader(AiReviewConstants.HEADER_CONTENT_TYPE, "application/json")
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    log.info("ExtraWebhook消息发送成功! webhook_url:{}", webhookUrl);
                } else {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    log.error("ExtraWebhook消息发送失败! webhook_url:{}, error_msg:{}", webhookUrl, responseBody);
                }
            }
        } catch (Exception e) {
            log.error("ExtraWebhook消息发送失败! {}", e.getMessage(), e);
        }
    }
}

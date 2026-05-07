package io.binghe.ai.review.llm.qwen;

import io.binghe.ai.review.llm.BaseOpenAIClient;
import lombok.extern.slf4j.Slf4j;

/**
 * @author binghe(微信 : hacker_binghe)
 * @version 1.0.0
 * @description 对接通义千问
 * @github https://github.com/binghe001
 * @copyright 公众号: 冰河技术
 */
@Slf4j
public class QwenClient extends BaseOpenAIClient {

    public QwenClient(String apiKey, String baseUrl, String provider, String model) {
        super(apiKey, baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl, provider, model);
        log.info("Qwen client initialized, model: {}, baseUrl: {}", model, baseUrl);
    }
}

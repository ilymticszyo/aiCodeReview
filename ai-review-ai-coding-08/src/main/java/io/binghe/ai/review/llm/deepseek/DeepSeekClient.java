package io.binghe.ai.review.llm.deepseek;

import io.binghe.ai.review.llm.BaseOpenAIClient;
import lombok.extern.slf4j.Slf4j;

/**
 * @author binghe(微信 : hacker_binghe)
 * @version 1.0.0
 * @description 对接DeepSeek
 * @github https://github.com/binghe001
 * @copyright 公众号: 冰河技术
 */
@Slf4j
public class DeepSeekClient extends BaseOpenAIClient {

    public DeepSeekClient(String apiKey, String baseUrl, String provider, String model) {
        super(apiKey, baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl, provider, model);
        log.info("DeepSeek client initialized, model: {}, baseUrl: {}", model, baseUrl);
    }
}

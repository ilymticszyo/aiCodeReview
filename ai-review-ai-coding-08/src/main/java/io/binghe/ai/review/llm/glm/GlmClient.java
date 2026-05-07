package io.binghe.ai.review.llm.glm;

import io.binghe.ai.review.llm.BaseOpenAIClient;
import lombok.extern.slf4j.Slf4j;

/**
 * @author binghe(微信 : hacker_binghe)
 * @version 1.0.0
 * @description 智谱大模型
 * @github https://github.com/binghe001
 * @copyright 公众号: 冰河技术
 */
@Slf4j
public class GlmClient extends BaseOpenAIClient {

    public GlmClient(String apiKey, String baseUrl, String provider, String model) {
        super(apiKey, baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl, provider, model);
        log.info("GLM client initialized, model: {}, baseUrl: {}", model, baseUrl);
    }
}

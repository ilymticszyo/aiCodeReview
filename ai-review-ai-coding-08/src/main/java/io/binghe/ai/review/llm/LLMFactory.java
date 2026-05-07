package io.binghe.ai.review.llm;

import io.binghe.ai.review.constants.AiReviewConstants;
import io.binghe.ai.review.llm.deepseek.DeepSeekClient;
import io.binghe.ai.review.llm.glm.GlmClient;
import io.binghe.ai.review.llm.openai.OpenAIClient;
import io.binghe.ai.review.llm.qwen.QwenClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author binghe(微信 : hacker_binghe)
 * @version 1.0.0
 * @description 大模型工厂
 * @github https://github.com/binghe001
 * @copyright 公众号: 冰河技术
 */
@Slf4j
@Component
public class LLMFactory {

    @Value("${llm.provider:openai}")
    private String provider;

    @Value("${llm.openai.api-key:}")
    private String openaiApiKey;

    @Value("${llm.openai.base-url:https://api.openai.com/v1}")
    private String openaiBaseUrl;

    @Value("${llm.openai.model:gpt-3.5-turbo}")
    private String openaiModel;

    @Value("${llm.deepseek.api-key:}")
    private String deepseekApiKey;

    @Value("${llm.deepseek.base-url:https://api.deepseek.com}")
    private String deepseekBaseUrl;

    @Value("${llm.deepseek.model:deepseek-chat}")
    private String deepseekModel;

    @Value("${llm.qwen.api-key:}")
    private String qwenApiKey;

    @Value("${llm.qwen.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}")
    private String qwenBaseUrl;

    @Value("${llm.qwen.model:qwen-turbo}")
    private String qwenModel;

    @Value("${llm.glm.api-key:}")
    private String glmApiKey;

    @Value("${llm.glm.base-url:https://open.bigmodel.cn}")
    private String glmBaseUrl;

    @Value("${llm.glm.model:glm-4.7-flash}")
    private String glmModel;

    public LLMClient getClient() {
        return getClient(provider);
    }

    public LLMClient getClient(String providerName) {
        String p = providerName != null ? providerName.toLowerCase() : provider.toLowerCase();
        log.info("Creating LLM client for provider: {}", p);

        return switch (p) {
            case AiReviewConstants.LLM_PROVIDER_DEEPSEEK -> new DeepSeekClient(deepseekApiKey, deepseekBaseUrl, p, deepseekModel);
            case AiReviewConstants.LLM_PROVIDER_QWEN -> new QwenClient(qwenApiKey, qwenBaseUrl, p, qwenModel);
            case AiReviewConstants.LLM_PROVIDER_GLM -> new GlmClient(glmApiKey, glmBaseUrl, p, glmModel);
            default -> new OpenAIClient(openaiApiKey, openaiBaseUrl, p, openaiModel);
        };
    }
}

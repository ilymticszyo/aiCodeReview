package io.binghe.ai.review.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.binghe.ai.review.constants.AiReviewConstants;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author binghe(微信 : hacker_binghe)
 * @version 1.0.0
 * @description 大模型基础客户端
 * @github https://github.com/binghe001
 * @copyright 公众号: 冰河技术
 */
@Slf4j
public abstract class BaseOpenAIClient implements LLMClient {

    protected final String apiKey;
    protected final String baseUrl;
    protected final String model;
    protected final String provider;
    protected final OkHttpClient httpClient;
    protected final ObjectMapper objectMapper;

    protected BaseOpenAIClient(String apiKey, String baseUrl, String provider, String model) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.model = model;
        this.provider = provider;
        this.objectMapper = new ObjectMapper();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .hostnameVerifier((hostname, session) -> true)
                .build();
    }

    @Override
    public String completions(List<Map<String, String>> messages) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put(AiReviewConstants.MODEL, model);

            ArrayNode messagesNode = objectMapper.createArrayNode();
            for (Map<String, String> message : messages) {
                ObjectNode msgNode = objectMapper.createObjectNode();
                msgNode.put(AiReviewConstants.ROLE, message.get(AiReviewConstants.ROLE));
                msgNode.put(AiReviewConstants.CONTENT, message.get(AiReviewConstants.CONTENT));
                messagesNode.add(msgNode);
            }
            requestBody.set(AiReviewConstants.MESSAGES, messagesNode);
            requestBody.put(AiReviewConstants.TEMPERATURE, 0.1);

            String url = baseUrl;

            if (AiReviewConstants.LLM_PROVIDER_GLM.equals(provider) && AiReviewConstants.LLM_MODEL_GLM_47_FLASH.equals(model)) {
                url = url + AiReviewConstants.GLM_CHAT_COMPLETIONS_PATH;
            }else{
                url = url + AiReviewConstants.LLM_CHAT_COMPLETIONS_PATH;
            }

            RequestBody body = RequestBody.create(
                    objectMapper.writeValueAsString(requestBody),
                    MediaType.parse(AiReviewConstants.MEDIA_TYPE_JSON)
            );

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader(AiReviewConstants.HEADER_AUTHORIZATION, AiReviewConstants.AUTH_BEARER_PREFIX + apiKey)
                    .addHeader(AiReviewConstants.HEADER_CONTENT_TYPE, AiReviewConstants.APPLICATION_JSON)
                    .post(body)
                    .build();

            log.info("Sending request to LLM API: {}, model: {}", url, model);

            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    log.error("LLM API call failed: {} - {}", response.code(), responseBody);
                    throw new RuntimeException("LLM API call failed: " + response.code() + " - " + responseBody);
                }

                JsonNode responseJson = objectMapper.readTree(responseBody);
                String content = responseJson
                        .path(AiReviewConstants.CHOICES)
                        .path(0)
                        .path(AiReviewConstants.MESSAGE)
                        .path(AiReviewConstants.CONTENT)
                        .asText();

                log.info("LLM API call succeeded, response length: {}", content.length());
                return content;
            }
        } catch (IOException e) {
            log.error("LLM API call IO error: {}", e.getMessage(), e);
            throw new RuntimeException("LLM API call failed: " + e.getMessage(), e);
        }
    }
}

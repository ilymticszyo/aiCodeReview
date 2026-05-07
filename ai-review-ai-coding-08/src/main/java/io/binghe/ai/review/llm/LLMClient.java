package io.binghe.ai.review.llm;

import java.util.List;
import java.util.Map;

/**
 * @author binghe(微信 : hacker_binghe)
 * @version 1.0.0
 * @description 大模型客户端接口
 * @github https://github.com/binghe001
 * @copyright 公众号: 冰河技术
 */
public interface LLMClient {

    /**
     * 调用LLM进行对话
     * @param messages 消息列表，每个消息包含role和content
     * @return AI返回的文本内容
     */
    String completions(List<Map<String, String>> messages);
}

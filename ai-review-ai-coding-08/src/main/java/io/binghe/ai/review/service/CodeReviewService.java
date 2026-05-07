package io.binghe.ai.review.service;

/**
 * @author binghe(微信 : hacker_binghe)
 * @version 1.0.0
 * @description CodeReview服务接口
 * @github https://github.com/binghe001
 * @copyright 公众号: 冰河技术
 */
public interface CodeReviewService {

    /**
     * 审查代码并去除markdown格式
     */
    String reviewAndStripCode(String changesText, String commitsText);

    /**
     *
     * 从diff文本中检测主要编程语言
     */
    String detectLanguageFromDiff(String diffsText);

    /**
     * 解析AI返回的审查结果中的分数
     */
    int parseReviewScore(String reviewText);
}

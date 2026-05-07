package io.binghe.ai.review.messaging;

import java.util.Map;

/**
 * @author binghe(微信 : hacker_binghe)
 * @version 1.0.0
 * @github https://github.com/binghe001
 * @copyright 公众号: 冰河技术
 * @description 通知服务
 */
public interface NotificationService {
    /**
     * 发送代码审查结果通知
     */
    void sendReviewNotification(String projectName, String author, String reviewType,
                                        String branch, String targetBranch, String reviewResult,
                                        Integer score, String url);
    /**
     * 发送代码审查结果通知（带项目路由和原始webhook数据）
     */
    void sendReviewNotification(String projectName, String author, String reviewType,
                                        String branch, String targetBranch, String reviewResult,
                                        Integer score, String url, String urlSlug,
                                        Map<String, Object> webhookData);

    /**
     * 发送日报通知
     */
    void sendDailyReport(String reportContent) ;

    /**
     * 发送错误通知
     */
    void sendErrorNotification(String errorMessage);
}

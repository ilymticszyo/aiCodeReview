package io.binghe.ai.review.messaging.impl;

import io.binghe.ai.review.constants.AiReviewConstants;
import io.binghe.ai.review.messaging.NotificationService;
import io.binghe.ai.review.notifier.DingTalkNotifier;
import io.binghe.ai.review.notifier.ExtraWebhookNotifier;
import io.binghe.ai.review.notifier.FeishuNotifier;
import io.binghe.ai.review.notifier.WeComNotifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author binghe(微信 : hacker_binghe)
 * @version 1.0.0
 * @github https://github.com/binghe001
 * @copyright 公众号: 冰河技术
 * @description 通知服务
 */
@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private DingTalkNotifier dingTalkNotifier;

    @Autowired
    private WeComNotifier weComNotifier;

    @Autowired
    private FeishuNotifier feishuNotifier;

    @Autowired
    private ExtraWebhookNotifier extraWebhookNotifier;

    /**
     * 发送代码审查结果通知
     */
    @Override
    public void sendReviewNotification(String projectName, String author, String reviewType,
                                        String branch, String targetBranch, String reviewResult,
                                        Integer score, String url) {
        sendReviewNotification(projectName, author, reviewType, branch, targetBranch,
                reviewResult, score, url, null, null);
    }

    /**
     * 发送代码审查结果通知（带项目路由和原始webhook数据）
     */
    @Override
    public void sendReviewNotification(String projectName, String author, String reviewType,
                                        String branch, String targetBranch, String reviewResult,
                                        Integer score, String url, String urlSlug,
                                        Map<String, Object> webhookData) {
        try {
            String title = String.format(AiReviewConstants.MSG_REVIEW_NOTIFICATION_TITLE_FORMAT, projectName);

            StringBuilder sb = new StringBuilder();
            sb.append(AiReviewConstants.MARKDOWN_NOTIFICATION_TITLE);
            sb.append(AiReviewConstants.MARKDOWN_PROJECT_LABEL).append(" ").append(projectName).append("\n");
            sb.append(AiReviewConstants.MARKDOWN_AUTHOR_LABEL).append(" ").append(author).append("\n");
            sb.append(AiReviewConstants.MARKDOWN_TYPE_LABEL).append(" ").append(reviewType).append("\n");
            sb.append(AiReviewConstants.MARKDOWN_BRANCH_LABEL).append(" ").append(branch);
            if (targetBranch != null && !targetBranch.isBlank()) {
                sb.append(AiReviewConstants.MARKDOWN_BRANCH_SEPARATOR).append(targetBranch);
            }
            sb.append("\n");
            if (score != null) {
                sb.append(AiReviewConstants.MARKDOWN_SCORE_LABEL).append(" ").append(score).append(AiReviewConstants.MARKDOWN_SCORE_UNIT).append("\n");
            }
            if (url != null && !url.isBlank()) {
                sb.append(AiReviewConstants.MARKDOWN_LINK_LABEL).append(" ").append(AiReviewConstants.MARKDOWN_LINK_PREFIX).append(url).append(AiReviewConstants.MARKDOWN_LINK_SUFFIX).append("\n");
            }
            sb.append(AiReviewConstants.MARKDOWN_SECTION_SEPARATOR);
            // 截断过长的审查结果
            if (reviewResult != null && reviewResult.length() > AiReviewConstants.MAX_REVIEW_RESULT_LENGTH) {
                sb.append(reviewResult, 0, AiReviewConstants.MAX_REVIEW_RESULT_LENGTH).append("\n").append(AiReviewConstants.MSG_CONTENT_TRUNCATED);
            } else if (reviewResult != null) {
                sb.append(reviewResult);
            }

            String content = sb.toString();

            // 发送各平台通知
            dingTalkNotifier.sendMarkdown(title, content, false, projectName, urlSlug);
            weComNotifier.sendMarkdown(content, title, projectName, urlSlug);
            feishuNotifier.sendMarkdown(content, title, projectName, urlSlug);

            // 发送额外webhook
            Map<String, Object> systemData = new HashMap<>();
            systemData.put(AiReviewConstants.JSON_FIELD_CONTENT, content);
            systemData.put(AiReviewConstants.NOTIFICATION_FIELD_MSG_TYPE, "markdown");
            systemData.put(AiReviewConstants.NOTIFICATION_FIELD_TITLE, title);
            systemData.put(AiReviewConstants.NOTIFICATION_FIELD_IS_AT_ALL, false);
            systemData.put(AiReviewConstants.NOTIFICATION_FIELD_PROJECT_NAME, projectName);
            systemData.put(AiReviewConstants.NOTIFICATION_FIELD_URL_SLUG, urlSlug);
            extraWebhookNotifier.sendMessage(systemData, webhookData != null ? webhookData : Map.of());

            log.info("Review notification sent for project: {}, author: {}", projectName, author);
        } catch (Exception e) {
            log.error("Failed to send review notification: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送日报通知
     */
    @Override
    public void sendDailyReport(String reportContent) {
        try {
            String title = AiReviewConstants.MSG_DAILY_REPORT_TITLE;
            dingTalkNotifier.sendMarkdown(title, reportContent, false, null, null);
            weComNotifier.sendMarkdown(reportContent, title, null, null);
            feishuNotifier.sendMarkdown(reportContent, title, null, null);

            Map<String, Object> systemData = new HashMap<>();
            systemData.put(AiReviewConstants.JSON_FIELD_CONTENT, reportContent);
            systemData.put(AiReviewConstants.NOTIFICATION_FIELD_MSG_TYPE, "markdown");
            systemData.put(AiReviewConstants.NOTIFICATION_FIELD_TITLE, title);
            extraWebhookNotifier.sendMessage(systemData, Map.of());

            log.info("Daily report notification sent");
        } catch (Exception e) {
            log.error("Failed to send daily report: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送错误通知
     */
    @Override
    public void sendErrorNotification(String errorMessage) {
        try {
            dingTalkNotifier.sendText(errorMessage, false, null, null);
            weComNotifier.sendText(errorMessage, false, null, null);
            feishuNotifier.sendText(errorMessage, null, null);
        } catch (Exception e) {
            log.error("Failed to send error notification: {}", e.getMessage(), e);
        }
    }
}

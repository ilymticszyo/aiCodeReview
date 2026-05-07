package io.binghe.ai.review.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.binghe.ai.review.constants.AiReviewConstants;
import io.binghe.ai.review.entity.MrReviewLog;
import io.binghe.ai.review.entity.PushReviewLog;
import io.binghe.ai.review.llm.LLMClient;
import io.binghe.ai.review.llm.LLMFactory;
import io.binghe.ai.review.messaging.NotificationService;
import io.binghe.ai.review.service.ReportService;
import io.binghe.ai.review.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * @author binghe(微信 : hacker_binghe)
 * @version 1.0.0
 * @description ReportServiceImpl
 * @github https://github.com/binghe001
 * @copyright 公众号: 冰河技术
 */
@Slf4j
@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private LLMFactory llmFactory;

    @Autowired
    private NotificationService notificationService;

    @Value("${review.push-enabled:false}")
    private boolean pushReviewEnabled;

    /**
     * 定时生成日报（工作日下午6点）
     */
    @Scheduled(cron = "${scheduler.report-cron:0 0 18 * * MON-FRI}")
    public void scheduledDailyReport() {
        log.info("Starting scheduled daily report");
        generateAndSendDailyReport();
    }

    /**
     * 手动触发日报生成
     */
    public String generateAndSendDailyReport() {
        try {
            // 获取今日时间范围
            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
            long startTs = startOfDay.atZone(ZoneId.systemDefault()).toEpochSecond();
            long endTs = endOfDay.atZone(ZoneId.systemDefault()).toEpochSecond();

            List<Map<String, Object>> records = new ArrayList<>();

            if (pushReviewEnabled) {
                List<PushReviewLog> pushLogs = reviewService.getPushReviewLogs(null, null, startTs, endTs);
                for (PushReviewLog l : pushLogs) {
                    records.add(Map.of(
                            AiReviewConstants.JSON_FIELD_AUTHOR, l.getAuthor() != null ? l.getAuthor() : "",
                            AiReviewConstants.JSON_FIELD_PROJECT_NAME, l.getProjectName() != null ? l.getProjectName() : "",
                            AiReviewConstants.BRANCH, l.getBranch() != null ? l.getBranch() : "",
                            AiReviewConstants.JSON_FIELD_COMMIT_MESSAGES, l.getCommitMessages() != null ? l.getCommitMessages() : "",
                            AiReviewConstants.JSON_FIELD_SCORE, l.getScore() != null ? l.getScore() : AiReviewConstants.ZERO
                    ));
                }
            } else {
                List<MrReviewLog> mrLogs = reviewService.getMrReviewLogs(null, null, startTs, endTs);
                for (MrReviewLog l : mrLogs) {
                    records.add(Map.of(
                            AiReviewConstants.JSON_FIELD_AUTHOR, l.getAuthor() != null ? l.getAuthor() : "",
                            AiReviewConstants.JSON_FIELD_PROJECT_NAME, l.getProjectName() != null ? l.getProjectName() : "",
                            AiReviewConstants.SOURCE_BRANCH, l.getSourceBranch() != null ? l.getSourceBranch() : "",
                            AiReviewConstants.TARGET_BRANCH, l.getTargetBranch() != null ? l.getTargetBranch() : "",
                            AiReviewConstants.JSON_FIELD_COMMIT_MESSAGES, l.getCommitMessages() != null ? l.getCommitMessages() : "",
                            AiReviewConstants.JSON_FIELD_SCORE, l.getScore() != null ? l.getScore() : AiReviewConstants.ZERO
                    ));
                }
            }

            if (records.isEmpty()) {
                log.info("No records for daily report");
                return AiReviewConstants.MSG_NO_DAILY_RECORDS;
            }

            // 去重：基于 (author, commit_messages) 组合
            Set<String> seen = new LinkedHashSet<>();
            List<Map<String, Object>> deduped = new ArrayList<>();
            for (Map<String, Object> r : records) {
                String key = r.get(AiReviewConstants.AUTHOR) + "|" + r.get(AiReviewConstants.COMMIT_MESSAGES);
                if (seen.add(key)) {
                    deduped.add(r);
                }
            }
            // 按照 author 排序
            deduped.sort(Comparator.comparing(r -> String.valueOf(r.get(AiReviewConstants.AUTHOR))));

            // 使用LLM生成日报
            String reportContent = generateReport(deduped);

            // 发送通知
            notificationService.sendDailyReport(reportContent);

            return reportContent;
        } catch (Exception e) {
            log.error("Failed to generate daily report: {}", e.getMessage(), e);
            return AiReviewConstants.MSG_DAILY_REPORT_FAILED_PREFIX + e.getMessage();
        }
    }

    private String generateReport(List<Map<String, Object>> records) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String data = mapper.writeValueAsString(records);

            List<Map<String, String>> messages = List.of(
                    Map.of(
                            AiReviewConstants.LLM_FIELD_ROLE, AiReviewConstants.LLM_ROLE_USER,
                            AiReviewConstants.LLM_FIELD_CONTENT,
                            AiReviewConstants.LLM_DAILY_REPORT_PROMPT + data)
            );

            LLMClient client = llmFactory.getClient();
            return client.completions(messages);
        } catch (Exception e) {
            log.error("Failed to generate report with LLM: {}", e.getMessage(), e);
            // 生成简单的日报
            StringBuilder report = new StringBuilder(AiReviewConstants.MSG_DAILY_REPORT_FALLBACK_HEADER);
            for (Map<String, Object> r : records) {
                report.append("- **").append(r.get(AiReviewConstants.JSON_FIELD_AUTHOR)).append("** 提交到 ")
                        .append(r.get(AiReviewConstants.JSON_FIELD_PROJECT_NAME)).append("：")
                        .append(r.get(AiReviewConstants.JSON_FIELD_COMMIT_MESSAGES)).append("（评分：")
                        .append(r.get(AiReviewConstants.JSON_FIELD_SCORE)).append("分）\n");
            }
            return report.toString();
        }
    }
}

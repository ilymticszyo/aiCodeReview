package io.binghe.ai.review.controller;

import io.binghe.ai.review.constants.AiReviewConstants;
import io.binghe.ai.review.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author binghe(微信 : hacker_binghe)
 * @version 1.0.0
 * @description ReportController
 * @github https://github.com/binghe001
 * @copyright 公众号: 冰河技术
 */
@Slf4j
@RestController
@RequestMapping("/review")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping(value = "/daily_report", produces = MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8")
    public ResponseEntity<String> dailyReport() {
        try {
            String reportContent = reportService.generateAndSendDailyReport();
            return ResponseEntity.ok(reportContent);
        } catch (Exception e) {
            log.error("Failed to generate daily report: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(AiReviewConstants.MSG_GENERATE_DAILY_REPORT_FAILED_PREFIX + e.getMessage());
        }
    }
}

package io.binghe.ai.review.controller;

import io.binghe.ai.review.constants.AiReviewConstants;
import io.binghe.ai.review.entity.MrReviewLog;
import io.binghe.ai.review.entity.PushReviewLog;
import io.binghe.ai.review.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author binghe(微信 : hacker_binghe)
 * @version 1.0.0
 * @description ReviewApiController
 * @github https://github.com/binghe001
 * @copyright 公众号: 冰河技术
 */
@Slf4j
@RestController
@RequestMapping("/api/review")
public class ReviewApiController {

    @Autowired
    private ReviewService reviewService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    @GetMapping("/logs")
    public ResponseEntity<Map<String, Object>> getReviewLogs(
            @RequestParam(defaultValue = "mr") String type,
            @RequestParam(required = false) List<String> authors,
            @RequestParam(name = AiReviewConstants.API_PARAM_PROJECT_NAMES, required = false) List<String> projectNames,
            @RequestParam(name = AiReviewConstants.API_PARAM_UPDATED_AT_GTE, required = false) Long updatedAtGte,
            @RequestParam(name = AiReviewConstants.API_PARAM_UPDATED_AT_LTE, required = false) Long updatedAtLte) {

        try {
            if (AiReviewConstants.EVENT_PUSH.equals(type)) {
                List<PushReviewLog> logs = reviewService.getPushReviewLogs(authors, projectNames, updatedAtGte, updatedAtLte);
                List<Map<String, Object>> data = logs.stream().map(this::pushLogToMap).collect(Collectors.toList());

                double avgScore = logs.stream()
                        .filter(l -> l.getScore() != null)
                        .mapToInt(PushReviewLog::getScore)
                        .average()
                        .orElse(0);

                return ResponseEntity.ok(Map.of(
                        AiReviewConstants.JSON_FIELD_DATA, data,
                        AiReviewConstants.JSON_FIELD_TOTAL, data.size(),
                        AiReviewConstants.JSON_FIELD_AVERAGE_SCORE, avgScore
                ));
            } else {
                List<MrReviewLog> logs = reviewService.getMrReviewLogs(authors, projectNames, updatedAtGte, updatedAtLte);
                List<Map<String, Object>> data = logs.stream().map(this::mrLogToMap).collect(Collectors.toList());

                double avgScore = logs.stream()
                        .filter(l -> l.getScore() != null)
                        .mapToInt(MrReviewLog::getScore)
                        .average()
                        .orElse(0);

                return ResponseEntity.ok(Map.of(
                        AiReviewConstants.JSON_FIELD_DATA, data,
                        AiReviewConstants.JSON_FIELD_TOTAL, data.size(),
                        AiReviewConstants.JSON_FIELD_AVERAGE_SCORE, avgScore
                ));
            }
        } catch (Exception e) {
            log.error("Error getting review logs: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(AiReviewConstants.JSON_FIELD_ERROR, e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getReviewStats(
            @RequestParam(defaultValue = "mr") String type,
            @RequestParam(required = false) List<String> authors,
            @RequestParam(name = AiReviewConstants.API_PARAM_PROJECT_NAMES, required = false) List<String> projectNames,
            @RequestParam(name = AiReviewConstants.API_PARAM_UPDATED_AT_GTE, required = false) Long updatedAtGte,
            @RequestParam(name = AiReviewConstants.API_PARAM_UPDATED_AT_LTE, required = false) Long updatedAtLte) {

        try {
            if (AiReviewConstants.EVENT_PUSH.equals(type)) {
                List<PushReviewLog> logs = reviewService.getPushReviewLogs(authors, projectNames, updatedAtGte, updatedAtLte);
                return ResponseEntity.ok(buildStats(
                        logs.stream().map(l -> Map.of(
                                AiReviewConstants.JSON_FIELD_PROJECT_NAME, (Object) l.getProjectName(),
                                AiReviewConstants.JSON_FIELD_AUTHOR, (Object) l.getAuthor(),
                                AiReviewConstants.JSON_FIELD_SCORE, (Object) (l.getScore() != null ? l.getScore() : 0),
                                AiReviewConstants.JSON_FIELD_ADDITIONS, (Object) (l.getAdditions() != null ? l.getAdditions() : 0),
                                AiReviewConstants.JSON_FIELD_DELETIONS, (Object) (l.getDeletions() != null ? l.getDeletions() : 0)
                        )).collect(Collectors.toList())
                ));
            } else {
                List<MrReviewLog> logs = reviewService.getMrReviewLogs(authors, projectNames, updatedAtGte, updatedAtLte);
                return ResponseEntity.ok(buildStats(
                        logs.stream().map(l -> Map.of(
                                AiReviewConstants.JSON_FIELD_PROJECT_NAME, (Object) l.getProjectName(),
                                AiReviewConstants.JSON_FIELD_AUTHOR, (Object) l.getAuthor(),
                                AiReviewConstants.JSON_FIELD_SCORE, (Object) (l.getScore() != null ? l.getScore() : 0),
                                AiReviewConstants.JSON_FIELD_ADDITIONS, (Object) (l.getAdditions() != null ? l.getAdditions() : 0),
                                AiReviewConstants.JSON_FIELD_DELETIONS, (Object) (l.getDeletions() != null ? l.getDeletions() : 0)
                        )).collect(Collectors.toList())
                ));
            }
        } catch (Exception e) {
            log.error("Error getting review stats: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(AiReviewConstants.JSON_FIELD_ERROR, e.getMessage()));
        }
    }

    @GetMapping("/filter-options")
    public ResponseEntity<Map<String, Object>> getFilterOptions(
            @RequestParam(defaultValue = "mr") String type) {
        try {
            List<String> authorsResult;
            List<String> projectNamesResult;

            if (AiReviewConstants.EVENT_PUSH.equals(type)) {
                authorsResult = reviewService.getPushDistinctAuthors();
                projectNamesResult = reviewService.getPushDistinctProjectNames();
            } else {
                authorsResult = reviewService.getMrDistinctAuthors();
                projectNamesResult = reviewService.getMrDistinctProjectNames();
            }

            return ResponseEntity.ok(Map.of(
                    AiReviewConstants.JSON_FIELD_AUTHORS, authorsResult,
                    AiReviewConstants.API_PARAM_PROJECT_NAMES, projectNamesResult
            ));
        } catch (Exception e) {
            log.error("Error getting filter options: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(AiReviewConstants.JSON_FIELD_ERROR, e.getMessage()));
        }
    }

    private Map<String, Object> mrLogToMap(MrReviewLog log) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(AiReviewConstants.JSON_FIELD_ID, log.getId());
        map.put(AiReviewConstants.JSON_FIELD_PROJECT_NAME, log.getProjectName());
        map.put(AiReviewConstants.JSON_FIELD_AUTHOR, log.getAuthor());
        map.put(AiReviewConstants.JSON_FIELD_SOURCE_BRANCH, log.getSourceBranch());
        map.put(AiReviewConstants.JSON_FIELD_TARGET_BRANCH, log.getTargetBranch());
        map.put(AiReviewConstants.JSON_FIELD_UPDATED_AT, formatTimestamp(log.getUpdatedAt()));
        map.put(AiReviewConstants.JSON_FIELD_COMMIT_MESSAGES, log.getCommitMessages());
        map.put(AiReviewConstants.JSON_FIELD_SCORE, log.getScore() != null ? log.getScore() : 0);
        map.put(AiReviewConstants.JSON_FIELD_URL, log.getUrl());
        map.put(AiReviewConstants.JSON_FIELD_REVIEW_RESULT, log.getReviewResult());
        map.put(AiReviewConstants.JSON_FIELD_ADDITIONS, log.getAdditions() != null ? log.getAdditions() : 0);
        map.put(AiReviewConstants.JSON_FIELD_DELETIONS, log.getDeletions() != null ? log.getDeletions() : 0);
        map.put(AiReviewConstants.JSON_FIELD_DELTA, buildDelta(log.getAdditions(), log.getDeletions()));
        return map;
    }

    private Map<String, Object> pushLogToMap(PushReviewLog log) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(AiReviewConstants.JSON_FIELD_ID, log.getId());
        map.put(AiReviewConstants.JSON_FIELD_PROJECT_NAME, log.getProjectName());
        map.put(AiReviewConstants.JSON_FIELD_AUTHOR, log.getAuthor());
        map.put(AiReviewConstants.JSON_FIELD_BRANCH, log.getBranch());
        map.put(AiReviewConstants.JSON_FIELD_UPDATED_AT, formatTimestamp(log.getUpdatedAt()));
        map.put(AiReviewConstants.JSON_FIELD_COMMIT_MESSAGES, log.getCommitMessages());
        map.put(AiReviewConstants.JSON_FIELD_SCORE, log.getScore() != null ? log.getScore() : 0);
        map.put(AiReviewConstants.JSON_FIELD_URL, log.getUrl());
        map.put(AiReviewConstants.JSON_FIELD_REVIEW_RESULT, log.getReviewResult());
        map.put(AiReviewConstants.JSON_FIELD_ADDITIONS, log.getAdditions() != null ? log.getAdditions() : 0);
        map.put(AiReviewConstants.JSON_FIELD_DELETIONS, log.getDeletions() != null ? log.getDeletions() : 0);
        map.put(AiReviewConstants.JSON_FIELD_DELTA, buildDelta(log.getAdditions(), log.getDeletions()));
        return map;
    }

    private String formatTimestamp(Long ts) {
        if (ts == null) return "";
        return FORMATTER.format(Instant.ofEpochSecond(ts));
    }

    private String buildDelta(Integer additions, Integer deletions) {
        int add = additions != null ? additions : 0;
        int del = deletions != null ? deletions : 0;
        return "+" + add + "  -" + del;
    }

    private Map<String, Object> buildStats(List<Map<String, Object>> records) {
        // 项目提交次数
        Map<String, Long> projectCounts = new LinkedHashMap<>();
        Map<String, List<Integer>> projectScoresMap = new LinkedHashMap<>();
        Map<String, Long> authorCounts = new LinkedHashMap<>();
        Map<String, List<Integer>> authorScoresMap = new LinkedHashMap<>();
        Map<String, Long> authorCodeLinesMap = new LinkedHashMap<>();

        for (Map<String, Object> r : records) {
            String project = String.valueOf(r.getOrDefault(AiReviewConstants.JSON_FIELD_PROJECT_NAME, ""));
            String author = String.valueOf(r.getOrDefault(AiReviewConstants.JSON_FIELD_AUTHOR, ""));
            int score = ((Number) r.getOrDefault(AiReviewConstants.JSON_FIELD_SCORE, 0)).intValue();
            int additions = ((Number) r.getOrDefault(AiReviewConstants.JSON_FIELD_ADDITIONS, 0)).intValue();
            int deletions = ((Number) r.getOrDefault(AiReviewConstants.JSON_FIELD_DELETIONS, 0)).intValue();

            if (!project.isBlank()) {
                projectCounts.merge(project, 1L, Long::sum);
                projectScoresMap.computeIfAbsent(project, k -> new ArrayList<>()).add(score);
            }
            if (!author.isBlank()) {
                authorCounts.merge(author, 1L, Long::sum);
                authorScoresMap.computeIfAbsent(author, k -> new ArrayList<>()).add(score);
                authorCodeLinesMap.merge(author, (long)(additions + deletions), Long::sum);
            }
        }

        List<Map<String, Object>> projectCountsList = projectCounts.entrySet().stream()
                .map(e -> Map.of(AiReviewConstants.JSON_FIELD_NAME, (Object) e.getKey(), AiReviewConstants.JSON_FIELD_COUNT, (Object) e.getValue()))
                .collect(Collectors.toList());

        List<Map<String, Object>> projectScoresList = projectScoresMap.entrySet().stream()
                .map(e -> Map.of(AiReviewConstants.JSON_FIELD_NAME, (Object) e.getKey(), AiReviewConstants.JSON_FIELD_AVERAGE_SCORE,
                        (Object) e.getValue().stream().mapToInt(i -> i).average().orElse(0)))
                .collect(Collectors.toList());

        List<Map<String, Object>> authorCountsList = authorCounts.entrySet().stream()
                .map(e -> Map.of(AiReviewConstants.JSON_FIELD_NAME, (Object) e.getKey(), AiReviewConstants.JSON_FIELD_COUNT, (Object) e.getValue()))
                .collect(Collectors.toList());

        List<Map<String, Object>> authorScoresList = authorScoresMap.entrySet().stream()
                .map(e -> Map.of(AiReviewConstants.JSON_FIELD_NAME, (Object) e.getKey(), AiReviewConstants.JSON_FIELD_AVERAGE_SCORE,
                        (Object) e.getValue().stream().mapToInt(i -> i).average().orElse(0)))
                .collect(Collectors.toList());

        List<Map<String, Object>> authorCodeLinesList = authorCodeLinesMap.entrySet().stream()
                .map(e -> Map.of(AiReviewConstants.JSON_FIELD_NAME, (Object) e.getKey(), AiReviewConstants.JSON_FIELD_CODE_LINES, (Object) e.getValue()))
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("project_counts", projectCountsList);
        result.put("project_scores", projectScoresList);
        result.put("author_counts", authorCountsList);
        result.put("author_scores", authorScoresList);
        result.put("author_code_lines", authorCodeLinesList);
        return result;
    }
}

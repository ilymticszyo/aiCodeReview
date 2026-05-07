package io.binghe.ai.review.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.binghe.ai.review.constants.AiReviewConstants;
import io.binghe.ai.review.entity.MrReviewLog;
import io.binghe.ai.review.entity.PushReviewLog;
import io.binghe.ai.review.messaging.NotificationService;
import io.binghe.ai.review.service.CodeReviewService;
import io.binghe.ai.review.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author binghe(微信 : hacker_binghe)
 * @version 1.0.0
 * @description GitHubWebhookHandler
 * @github https://github.com/binghe001
 * @copyright 公众号: 冰河技术
 */
@Slf4j
@Component
public class GitHubWebhookHandler {

    @Autowired
    private CodeReviewService codeReviewService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private NotificationService notificationService;

    @Value("${review.supported-extensions:.java,.py,.php,.yml,.vue,.go,.c,.cpp,.h,.js,.css,.md,.sql,.ts,.tsx,.jsx}")
    private String supportedExtensions;

    @Value("${review.push-enabled:false}")
    private boolean pushReviewEnabled;

    @Value("${review.only-protected-branches:false}")
    private boolean onlyProtectedBranches;

    @Value("${review.only-branch-name:}")
    private String onlyBranchName;

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GitHubWebhookHandler() {
        this.objectMapper = new ObjectMapper();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .hostnameVerifier((hostname, session) -> true)
                .build();
    }

    @Async
    public void handlePullRequest(JsonNode payload, String githubToken, String githubUrl) {
        try {
            log.info("Processing GitHub PR webhook");

            String action = payload.path(AiReviewConstants.ACTION).asText();
            if (!List.of(AiReviewConstants.ACTION_OPENED, AiReviewConstants.ACTION_SYNCHRONIZE, AiReviewConstants.ACTION_EDITED).contains(action)) {
                log.info("GitHub PR action '{}' ignored", action);
                return;
            }

            JsonNode pr = payload.path(AiReviewConstants.PULL_REQUEST);
            String projectName = payload.path(AiReviewConstants.REPOSITORY).path(AiReviewConstants.NAME).asText();
            String author = pr.path(AiReviewConstants.USER).path(AiReviewConstants.LOGIN).asText();
            String sourceBranch = pr.path(AiReviewConstants.HEAD).path(AiReviewConstants.REF).asText();
            String targetBranch = pr.path(AiReviewConstants.BASE).path(AiReviewConstants.REF).asText();
            long updatedAt = System.currentTimeMillis() / 1000;
            String prUrl = normalizeUrl(pr.path(AiReviewConstants.HTML_URL).asText(), githubUrl);
            String repoFullName = payload.path(AiReviewConstants.REPOSITORY).path(AiReviewConstants.FULL_NAME).asText();
            int prNumber = pr.path(AiReviewConstants.NUMBER).asInt();

            // 过滤目标分支（如果配置了仅审查指定分支，支持逗号分隔多分支）
            if (onlyBranchName != null && !onlyBranchName.isBlank()) {
                List<String> allowedBranches = Arrays.stream(onlyBranchName.split(","))
                        .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
                if (!allowedBranches.isEmpty() && !allowedBranches.contains(targetBranch)) {
                    log.info("GitHub PR target branch '{}' not in allowed branches {}, skipping", targetBranch, allowedBranches);
                    return;
                }
            }

            // 过滤受保护分支
            if (onlyProtectedBranches && !isTargetBranchProtected(githubToken, repoFullName, targetBranch)) {
                log.info("GitHub PR target branch '{}' not in protected branches, skipping", targetBranch);
                return;
            }

            // 获取PR的diff
            String diffUrl = pr.path(AiReviewConstants.URL).asText() + "/files";
            String changesText = fetchPrFiles(diffUrl, githubToken);

            if (changesText == null || changesText.isBlank()) {
                log.info("No supported changes found in PR");
                return;
            }

            // 获取提交信息
            String commitsUrl = pr.path(AiReviewConstants.COMMITS_URL).asText();
            String commitsText = fetchCommits(commitsUrl, githubToken);

            // 执行AI代码审查
            log.info("Starting AI review for GitHub PR: {}/#{}", projectName, prNumber);
            String reviewResult = codeReviewService.reviewAndStripCode(changesText, commitsText);
            int score = codeReviewService.parseReviewScore(reviewResult);

            // 发布PR评论
            String apiBase = githubUrl.contains(AiReviewConstants.GITHUB_API_HOSTNAME) ? githubUrl : AiReviewConstants.GITHUB_DEFAULT_API_URL;
            postPrComment(apiBase, githubToken, repoFullName, prNumber, reviewResult);

            // 计算代码行数
            int[] additions = {0};
            int[] deletions = {0};
            calculateChanges(changesText, additions, deletions);

            // 保存到数据库
            MrReviewLog mrLog = MrReviewLog.builder()
                    .projectName(projectName)
                    .author(author)
                    .sourceBranch(sourceBranch)
                    .targetBranch(targetBranch)
                    .updatedAt(updatedAt)
                    .commitMessages(commitsText)
                    .score(score)
                    .url(prUrl)
                    .reviewResult(reviewResult)
                    .additions(additions[0])
                    .deletions(deletions[0])
                    .build();
            reviewService.insertMrReviewLog(mrLog);

            // 发送通知
            notificationService.sendReviewNotification(
                    projectName, author, AiReviewConstants.REVIEW_TYPE_PULL_REQUEST, sourceBranch, targetBranch, reviewResult, score, prUrl,
                    slugifyUrl(githubUrl), null);

        } catch (Exception e) {
            log.error("Error handling GitHub PR webhook: {}", e.getMessage(), e);
            notificationService.sendErrorNotification(AiReviewConstants.MSG_ERROR_SERVICE_PREFIX + e.getMessage());
        }
    }

    @Async
    public void handlePush(JsonNode payload, String githubToken, String githubUrl) {
        try {
            log.info("Processing GitHub Push webhook");

            String projectName = payload.path(AiReviewConstants.REPOSITORY).path(AiReviewConstants.NAME).asText();
            String author = payload.path(AiReviewConstants.PUSHER).path(AiReviewConstants.NAME).asText();
            String ref = payload.path(AiReviewConstants.REF).asText();
            String branch = ref.replace(AiReviewConstants.GIT_REFS_HEADS_PREFIX, "");
            long updatedAt = System.currentTimeMillis() / 1000;
            JsonNode commits = payload.path(AiReviewConstants.COMMITS);

            // 提取 push 链接：优先使用 compare（变更对比页），fallback 到 head_commit.url
            String pushUrl = payload.path(AiReviewConstants.COMPARE).asText(null);
            if (pushUrl == null || pushUrl.isBlank()) {
                pushUrl = payload.path(AiReviewConstants.HEAD_COMMIT).path(AiReviewConstants.URL).asText(null);
            }
            pushUrl = normalizeUrl(pushUrl, githubUrl);

            StringBuilder commitsBuilder = new StringBuilder();
            for (JsonNode commit : commits) {
                commitsBuilder.append(commit.path(AiReviewConstants.MESSAGE).asText()).append("\n");
            }
            String commitsText = commitsBuilder.toString();

            String reviewResult = null;
            int score = 0;

            if (pushReviewEnabled) {
                StringBuilder changesBuilder = new StringBuilder();
                Set<String> supportedExts = getSupportedExtensions();
                for (JsonNode commit : commits) {
                    commit.path(AiReviewConstants.ADDED).forEach(f -> {
                        if (supportedExts.contains(getExtension(f.asText())))
                            changesBuilder.append("+++ b/").append(f.asText()).append("\n");
                    });
                    commit.path(AiReviewConstants.MODIFIED).forEach(f -> {
                        if (supportedExts.contains(getExtension(f.asText())))
                            changesBuilder.append("+++ b/").append(f.asText()).append("\n");
                    });
                }

                String changesText = changesBuilder.toString();
                if (changesText.isBlank()) {
                    log.info("No supported changes found in push");
                    reviewResult = AiReviewConstants.MSG_NO_WATCHED_FILES_CHANGED;
                } else {
                    log.info("Starting AI review for GitHub push: {}/{}", projectName, branch);
                    reviewResult = codeReviewService.reviewAndStripCode(changesText, commitsText);
                    score = codeReviewService.parseReviewScore(reviewResult);
                }
            }

            // 保存到数据库（无论是否开启push review，都记录push事件）
            PushReviewLog pushLog = PushReviewLog.builder()
                    .projectName(projectName)
                    .author(author)
                    .branch(branch)
                    .updatedAt(updatedAt)
                    .commitMessages(commitsText)
                    .score(score)
                    .url(pushUrl)
                    .reviewResult(reviewResult)
                    .additions(0)
                    .deletions(0)
                    .build();
            reviewService.insertPushReviewLog(pushLog);

            // 发送通知（仅当有审查结果时）
            if (reviewResult != null && !reviewResult.isBlank()) {
                notificationService.sendReviewNotification(
                        projectName, author, AiReviewConstants.REVIEW_TYPE_PUSH, branch, null, reviewResult, score, null,
                        slugifyUrl(githubUrl), null);
            }

        } catch (Exception e) {
            log.error("Error handling GitHub push webhook: {}", e.getMessage(), e);
            notificationService.sendErrorNotification(AiReviewConstants.MSG_ERROR_SERVICE_PREFIX + e.getMessage());
        }
    }

    private String fetchPrFiles(String filesUrl, String token) throws IOException {
        Set<String> supported = getSupportedExtensions();
        StringBuilder sb = new StringBuilder();
        int page = 1;

        while (true) {
            String url = filesUrl + "?per_page=100&page=" + page;
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader(AiReviewConstants.HEADER_AUTHORIZATION, AiReviewConstants.AUTH_TOKEN_PREFIX + token)
                    .addHeader(AiReviewConstants.HEADER_ACCEPT, AiReviewConstants.GITHUB_ACCEPT_HEADER)
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) break;
                String body = response.body() != null ? response.body().string() : "";
                JsonNode files = objectMapper.readTree(body);
                if (!files.isArray() || files.size() == 0) break;

                for (JsonNode file : files) {
                    String filename = file.path(AiReviewConstants.FILENAME).asText();
                    String ext = getExtension(filename);
                    if (!supported.contains(ext)) continue;

                    String patch = file.path(AiReviewConstants.PATCH).asText("");
                    if (!patch.isBlank()) {
                        sb.append("diff --git a/").append(filename).append(" b/").append(filename).append("\n");
                        sb.append("--- a/").append(filename).append("\n");
                        sb.append("+++ b/").append(filename).append("\n");
                        sb.append(patch).append("\n");
                    }
                }
                if (files.size() < 100) break;
                page++;
            }
        }
        return sb.toString();
    }

    private String fetchCommits(String commitsUrl, String token) throws IOException {
        Request request = new Request.Builder()
                .url(commitsUrl)
                .addHeader(AiReviewConstants.HEADER_AUTHORIZATION, AiReviewConstants.AUTH_TOKEN_PREFIX + token)
                .addHeader(AiReviewConstants.HEADER_ACCEPT, AiReviewConstants.GITHUB_ACCEPT_HEADER)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) return "";
            String body = response.body() != null ? response.body().string() : "";
            JsonNode commits = objectMapper.readTree(body);
            StringBuilder sb = new StringBuilder();
            for (JsonNode commit : commits) {
                sb.append(commit.path(AiReviewConstants.COMMIT).path(AiReviewConstants.MESSAGE).asText()).append("\n");
            }
            return sb.toString();
        }
    }

    private void postPrComment(String apiBase, String token, String repoFullName, int prNumber, String comment) {
        try {
            String url = apiBase.replaceAll("/+$", "") + "/repos/" + repoFullName + "/issues/" + prNumber + "/comments";
            ObjectNode body = objectMapper.createObjectNode();
            body.put(AiReviewConstants.BODY, comment);

            RequestBody requestBody = RequestBody.create(
                    objectMapper.writeValueAsString(body),
                    MediaType.parse(AiReviewConstants.MEDIA_TYPE_JSON)
            );

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader(AiReviewConstants.HEADER_AUTHORIZATION, AiReviewConstants.AUTH_TOKEN_PREFIX + token)
                    .addHeader(AiReviewConstants.HEADER_ACCEPT, AiReviewConstants.GITHUB_ACCEPT_HEADER)
                    .post(requestBody)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                log.info("Posted PR comment: {}", response.code());
            }
        } catch (Exception e) {
            log.error("Failed to post PR comment: {}", e.getMessage(), e);
        }
    }

    private Set<String> getSupportedExtensions() {
        Set<String> set = new HashSet<>();
        for (String ext : supportedExtensions.split(",")) {
            set.add(ext.trim());
        }
        return set;
    }

    private void calculateChanges(String diff, int[] additions, int[] deletions) {
        for (String line : diff.split("\n")) {
            if (line.startsWith("+") && !line.startsWith("+++")) additions[0]++;
            else if (line.startsWith("-") && !line.startsWith("---")) deletions[0]++;
        }
    }

    private String getExtension(String filePath) {
        if (filePath == null) return "";
        int lastDot = filePath.lastIndexOf('.');
        if (lastDot < 0) return "";
        return filePath.substring(lastDot);
    }

    private boolean isTargetBranchProtected(String token, String repoFullName, String targetBranch) {
        try {
            String url = AiReviewConstants.GITHUB_DEFAULT_API_URL + "/repos/" + repoFullName + "/branches?protected=true";
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader(AiReviewConstants.HEADER_AUTHORIZATION, AiReviewConstants.AUTH_TOKEN_PREFIX + token)
                    .addHeader(AiReviewConstants.HEADER_ACCEPT, AiReviewConstants.GITHUB_ACCEPT_HEADER)
                    .get()
                    .build();
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) return false;
                String body = response.body() != null ? response.body().string() : "[]";
                JsonNode branches = objectMapper.readTree(body);
                for (JsonNode branch : branches) {
                    String name = branch.path("name").asText();
                    if (fnmatch(targetBranch, name)) return true;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to check GitHub protected branches: {}", e.getMessage());
        }
        return false;
    }

    private boolean fnmatch(String name, String pattern) {
        String regex = pattern.replace(".", "\\.").replace("*", ".*").replace("?", ".");
        return name.matches(regex);
    }

    private String slugifyUrl(String url) {
        if (url == null || url.isBlank()) return "";
        String s = url.replaceAll("^https?://", "");
        s = s.replaceAll("[^a-zA-Z0-9]", "_");
        s = s.replaceAll("_+$", "");
        return s;
    }

    private String normalizeUrl(String payloadUrl, String platformUrl) {
        if (payloadUrl == null || payloadUrl.isBlank()
                || platformUrl == null || platformUrl.isBlank()) {
            return payloadUrl;
        }
        try {
            java.net.URI payloadUri = new java.net.URI(payloadUrl);
            java.net.URI platformUri = new java.net.URI(platformUrl.replaceAll("/+$", ""));
            if (payloadUri.getHost() != null
                    && payloadUri.getHost().equals(platformUri.getHost())
                    && platformUri.getPort() > 0
                    && payloadUri.getPort() == -1) {
                return new java.net.URI(
                        payloadUri.getScheme(), payloadUri.getUserInfo(),
                        payloadUri.getHost(), platformUri.getPort(),
                        payloadUri.getPath(), payloadUri.getQuery(),
                        payloadUri.getFragment()).toString();
            }
        } catch (Exception ignored) {}
        return payloadUrl;
    }
}

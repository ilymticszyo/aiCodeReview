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
 * @description GiteaWebhookHandler
 * @github https://github.com/binghe001
 * @copyright 公众号: 冰河技术
 */
@Slf4j
@Component
public class GiteaWebhookHandler {

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

    @Value("${gitea.use-issue-mode:true}")
    private boolean useIssueMode;

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GiteaWebhookHandler() {
        this.objectMapper = new ObjectMapper();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .hostnameVerifier((hostname, session) -> true)
                .build();
    }

    @Async
    public void handlePullRequest(JsonNode payload, String giteaToken, String giteaUrl) {
        try {
            log.info("Processing Gitea PR webhook");

            String action = payload.path(AiReviewConstants.ACTION).asText();
            if (!List.of(AiReviewConstants.ACTION_OPENED, AiReviewConstants.ACTION_SYNCHRONIZE).contains(action)) {
                log.info("Gitea PR action '{}' ignored", action);
                return;
            }

            JsonNode pr = payload.path(AiReviewConstants.PULL_REQUEST);
            String projectName = payload.path(AiReviewConstants.REPOSITORY).path(AiReviewConstants.NAME).asText();
            String repoFullName = payload.path(AiReviewConstants.REPOSITORY).path(AiReviewConstants.FULL_NAME).asText();
            String author = pr.path(AiReviewConstants.USER).path(AiReviewConstants.LOGIN).asText();
            String sourceBranch = pr.path(AiReviewConstants.HEAD).path(AiReviewConstants.LABEL).asText();
            String targetBranch = pr.path(AiReviewConstants.BASE).path(AiReviewConstants.LABEL).asText();
            long updatedAt = System.currentTimeMillis() / 1000;
            String prUrl = normalizeUrl(pr.path(AiReviewConstants.HTML_URL).asText(), giteaUrl);
            int prNumber = pr.path(AiReviewConstants.NUMBER).asInt();

            // 过滤目标分支（如果配置了仅审查指定分支，支持逗号分隔多分支）
            if (onlyBranchName != null && !onlyBranchName.isBlank()) {
                List<String> allowedBranches = Arrays.stream(onlyBranchName.split(","))
                        .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
                if (!allowedBranches.isEmpty() && !allowedBranches.contains(targetBranch)) {
                    log.info("Gitea PR target branch '{}' not in allowed branches {}, skipping", targetBranch, allowedBranches);
                    return;
                }
            }

            // 过滤受保护分支
            if (onlyProtectedBranches && !isTargetBranchProtected(giteaUrl, giteaToken, repoFullName, targetBranch)) {
                log.info("Gitea PR target branch '{}' not in protected branches, skipping", targetBranch);
                return;
            }

            // 获取PR的代码变更
            String changesText = fetchPrFiles(giteaUrl, giteaToken, repoFullName, prNumber);
            if (changesText == null || changesText.isBlank()) {
                log.info("No supported changes found in Gitea PR");
                return;
            }

            // 获取提交信息
            String commitsText = fetchPrCommits(giteaUrl, giteaToken, repoFullName, prNumber);

            // 执行AI代码审查
            log.info("Starting AI review for Gitea PR: {}/#{}", projectName, prNumber);
            String reviewResult = codeReviewService.reviewAndStripCode(changesText, commitsText);
            int score = codeReviewService.parseReviewScore(reviewResult);

            // 发布评论
            if (useIssueMode) {
                String issueTitle = AiReviewConstants.GITEA_REVIEW_ISSUE_PR_TITLE_PREFIX + prNumber;
                int issueNumber = createOrGetReviewIssue(giteaUrl, giteaToken, repoFullName, issueTitle);
                if (issueNumber > 0) {
                    addIssueComment(giteaUrl, giteaToken, repoFullName, issueNumber, reviewResult);
                } else {
                    postPrComment(giteaUrl, giteaToken, repoFullName, prNumber, reviewResult);
                }
            } else {
                postPrComment(giteaUrl, giteaToken, repoFullName, prNumber, reviewResult);
            }

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
                    slugifyUrl(giteaUrl), null);

        } catch (Exception e) {
            log.error("Error handling Gitea PR webhook: {}", e.getMessage(), e);
            notificationService.sendErrorNotification(AiReviewConstants.MSG_ERROR_SERVICE_PREFIX + e.getMessage());
        }
    }

    @Async
    public void handlePush(JsonNode payload, String giteaToken, String giteaUrl) {
        try {
            log.info("Processing Gitea Push webhook");

            String projectName = payload.path(AiReviewConstants.REPOSITORY).path(AiReviewConstants.NAME).asText();
            String repoFullName = payload.path(AiReviewConstants.REPOSITORY).path(AiReviewConstants.FULL_NAME).asText();
            String author = payload.path(AiReviewConstants.PUSHER).path(AiReviewConstants.LOGIN).asText();
            if (author == null || author.isBlank()) {
                author = payload.path(AiReviewConstants.SENDER).path(AiReviewConstants.LOGIN).asText();
            }
            String ref = payload.path(AiReviewConstants.REF).asText();
            String branch = ref.replace(AiReviewConstants.GIT_REFS_HEADS_PREFIX, "");
            long updatedAt = System.currentTimeMillis() / 1000;
            JsonNode commits = payload.path(AiReviewConstants.COMMITS);

            // 提取 push 链接：优先使用 compare_url（变更对比页），fallback 到第一个 commit URL
            String pushUrl = payload.path(AiReviewConstants.COMPARE_URL).asText(null);
            if (pushUrl == null || pushUrl.isBlank()) {
                pushUrl = payload.path(AiReviewConstants.COMPARE).asText(null);
            }
            if ((pushUrl == null || pushUrl.isBlank()) && commits.isArray() && commits.size() > 0) {
                pushUrl = commits.get(0).path(AiReviewConstants.URL).asText(null);
            }
            pushUrl = normalizeUrl(pushUrl, giteaUrl);

            StringBuilder commitsBuilder = new StringBuilder();
            for (JsonNode commit : commits) {
                commitsBuilder.append(commit.path(AiReviewConstants.MESSAGE).asText()).append("\n");
            }
            String commitsText = commitsBuilder.toString();

            String reviewResult = null;
            int score = 0;
            int[] additions = {0};
            int[] deletions = {0};

            if (pushReviewEnabled) {
                StringBuilder changesBuilder = new StringBuilder();
                for (JsonNode commit : commits) {
                    String commitId = commit.path(AiReviewConstants.ID).asText();
                    String commitChanges = fetchCommitChanges(giteaUrl, giteaToken, repoFullName, commitId);
                    if (commitChanges != null) {
                        changesBuilder.append(commitChanges).append("\n");
                    }
                }

                String changesText = changesBuilder.toString();
                if (changesText.isBlank()) {
                    log.info("No supported changes found in Gitea push");
                    reviewResult = AiReviewConstants.MSG_NO_WATCHED_FILES_CHANGED;
                } else {
                    log.info("Starting AI review for Gitea push: {}/{}", projectName, branch);
                    reviewResult = codeReviewService.reviewAndStripCode(changesText, commitsText);
                    score = codeReviewService.parseReviewScore(reviewResult);

                    // 发布评论（到最新commit）
                    if (!commits.isEmpty()) {
                        String latestCommitId = commits.get(0).path(AiReviewConstants.ID).asText();
                        String commitShort = latestCommitId.length() >= 7 ? latestCommitId.substring(0, 7) : latestCommitId;
                        if (useIssueMode) {
                            String issueTitle = AiReviewConstants.GITEA_REVIEW_ISSUE_TITLE_PREFIX + repoFullName + "@" + branch + ":" + commitShort;
                            int issueNumber = createOrGetReviewIssue(giteaUrl, giteaToken, repoFullName, issueTitle);
                            if (issueNumber > 0) {
                                addIssueComment(giteaUrl, giteaToken, repoFullName, issueNumber, reviewResult);
                            } else {
                                postCommitComment(giteaUrl, giteaToken, repoFullName, latestCommitId, reviewResult);
                            }
                        } else {
                            postCommitComment(giteaUrl, giteaToken, repoFullName, latestCommitId, reviewResult);
                        }
                    }

                    calculateChanges(changesText, additions, deletions);
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
                    .additions(additions[0])
                    .deletions(deletions[0])
                    .build();
            reviewService.insertPushReviewLog(pushLog);

            // 发送通知（仅当有审查结果时）
            if (reviewResult != null && !reviewResult.isBlank()) {
                notificationService.sendReviewNotification(
                        projectName, author, AiReviewConstants.REVIEW_TYPE_PUSH, branch, null, reviewResult, score, null,
                        slugifyUrl(giteaUrl), null);
            }

        } catch (Exception e) {
            log.error("Error handling Gitea push webhook: {}", e.getMessage(), e);
            notificationService.sendErrorNotification(AiReviewConstants.MSG_ERROR_SERVICE_PREFIX + e.getMessage());
        }
    }

    private String fetchPrFiles(String giteaUrl, String token, String repoFullName, int prNumber) throws IOException {
        String url = giteaUrl.replaceAll("/+$", "") + AiReviewConstants.GITEA_API_REPOS_PATH + repoFullName + "/pulls/" + prNumber + "/files";
        Set<String> supported = getSupportedExtensions();
        StringBuilder sb = new StringBuilder();

        Request request = new Request.Builder()
                .url(url)
                .addHeader(AiReviewConstants.HEADER_AUTHORIZATION, AiReviewConstants.AUTH_TOKEN_PREFIX + token)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.warn("Failed to fetch Gitea PR files: {}", response.code());
                return null;
            }
            String body = response.body() != null ? response.body().string() : "";
            JsonNode files = objectMapper.readTree(body);

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
        }
        return sb.toString();
    }

    private String fetchPrCommits(String giteaUrl, String token, String repoFullName, int prNumber) throws IOException {
        String url = giteaUrl.replaceAll("/+$", "") + AiReviewConstants.GITEA_API_REPOS_PATH + repoFullName + "/pulls/" + prNumber + "/commits";
        Request request = new Request.Builder()
                .url(url)
                .addHeader(AiReviewConstants.HEADER_AUTHORIZATION, AiReviewConstants.AUTH_TOKEN_PREFIX + token)
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

    private String fetchCommitChanges(String giteaUrl, String token, String repoFullName, String commitId) throws IOException {
        String url = giteaUrl.replaceAll("/+$", "") + AiReviewConstants.GITEA_API_REPOS_PATH + repoFullName + "/git/commits/" + commitId;
        Set<String> supported = getSupportedExtensions();
        Request request = new Request.Builder()
                .url(url)
                .addHeader(AiReviewConstants.HEADER_AUTHORIZATION, AiReviewConstants.AUTH_TOKEN_PREFIX + token)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) return null;
            String body = response.body() != null ? response.body().string() : "";
            JsonNode json = objectMapper.readTree(body);

            StringBuilder sb = new StringBuilder();
            JsonNode files = json.path(AiReviewConstants.FILES);
            for (JsonNode file : files) {
                String filename = file.path(AiReviewConstants.FILENAME).asText();
                String ext = getExtension(filename);
                if (!supported.contains(ext)) continue;

                String patch = file.path(AiReviewConstants.PATCH).asText("");
                if (!patch.isBlank()) {
                    sb.append("diff --git a/").append(filename).append(" b/").append(filename).append("\n");
                    sb.append("+++ b/").append(filename).append("\n");
                    sb.append(patch).append("\n");
                }
            }
            return sb.toString();
        }
    }

    private void postPrComment(String giteaUrl, String token, String repoFullName, int prNumber, String comment) {
        try {
            String url = giteaUrl.replaceAll("/+$", "") + AiReviewConstants.GITEA_API_REPOS_PATH + repoFullName + "/issues/" + prNumber + "/comments";
            ObjectNode body = objectMapper.createObjectNode();
            body.put(AiReviewConstants.BODY, comment);

            RequestBody requestBody = RequestBody.create(
                    objectMapper.writeValueAsString(body),
                    MediaType.parse(AiReviewConstants.MEDIA_TYPE_JSON)
            );

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader(AiReviewConstants.HEADER_AUTHORIZATION, AiReviewConstants.AUTH_TOKEN_PREFIX + token)
                    .post(requestBody)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                log.info("Posted Gitea PR comment: {}", response.code());
            }
        } catch (Exception e) {
            log.error("Failed to post Gitea PR comment: {}", e.getMessage(), e);
        }
    }

    private void postCommitComment(String giteaUrl, String token, String repoFullName, String commitId, String comment) {
        try {
            String url = giteaUrl.replaceAll("/+$", "") + AiReviewConstants.GITEA_API_REPOS_PATH + repoFullName + "/git/commits/" + commitId + "/notes";
            ObjectNode body = objectMapper.createObjectNode();
            body.put(AiReviewConstants.MESSAGE, comment);

            RequestBody requestBody = RequestBody.create(
                    objectMapper.writeValueAsString(body),
                    MediaType.parse(AiReviewConstants.MEDIA_TYPE_JSON)
            );

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader(AiReviewConstants.HEADER_AUTHORIZATION, AiReviewConstants.AUTH_TOKEN_PREFIX + token)
                    .post(requestBody)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                log.info("Posted Gitea commit comment: {}", response.code());
            }
        } catch (Exception e) {
            log.error("Failed to post Gitea commit comment: {}", e.getMessage(), e);
        }
    }

    /**
     * 查找或创建审查Issue（匹配Python的create_or_get_review_issue逻辑）
     * 通过标题查找已有Issue，不存在则创建新Issue，返回Issue编号
     */
    private int createOrGetReviewIssue(String giteaUrl, String token, String repoFullName, String title) {
        try {
            // 搜索已有Issue
            String searchUrl = giteaUrl.replaceAll("/+$", "") + AiReviewConstants.GITEA_API_REPOS_PATH + repoFullName
                    + "/issues?type=issues&state=open&limit=50";
            Request searchRequest = new Request.Builder()
                    .url(searchUrl)
                    .addHeader(AiReviewConstants.HEADER_AUTHORIZATION, AiReviewConstants.AUTH_TOKEN_PREFIX + token)
                    .get()
                    .build();

            try (Response response = httpClient.newCall(searchRequest).execute()) {
                if (response.isSuccessful()) {
                    String body = response.body() != null ? response.body().string() : "[]";
                    JsonNode issues = objectMapper.readTree(body);
                    for (JsonNode issue : issues) {
                        if (title.equals(issue.path(AiReviewConstants.TITLE).asText())) {
                            int existingNumber = issue.path(AiReviewConstants.NUMBER).asInt();
                            log.info("Found existing Gitea issue #{} for title: {}", existingNumber, title);
                            return existingNumber;
                        }
                    }
                }
            }

            // 创建新Issue
            String createUrl = giteaUrl.replaceAll("/+$", "") + AiReviewConstants.GITEA_API_REPOS_PATH + repoFullName + "/issues";
            ObjectNode issueBody = objectMapper.createObjectNode();
            issueBody.put(AiReviewConstants.TITLE, title);
            issueBody.put(AiReviewConstants.BODY, AiReviewConstants.GITEA_REVIEW_ISSUE_DEFAULT_BODY);

            RequestBody requestBody = RequestBody.create(
                    objectMapper.writeValueAsString(issueBody),
                    MediaType.parse(AiReviewConstants.MEDIA_TYPE_JSON)
            );

            Request createRequest = new Request.Builder()
                    .url(createUrl)
                    .addHeader(AiReviewConstants.HEADER_AUTHORIZATION, AiReviewConstants.AUTH_TOKEN_PREFIX + token)
                    .post(requestBody)
                    .build();

            try (Response response = httpClient.newCall(createRequest).execute()) {
                if (response.isSuccessful()) {
                    String body = response.body() != null ? response.body().string() : "{}";
                    JsonNode created = objectMapper.readTree(body);
                    int newNumber = created.path(AiReviewConstants.NUMBER).asInt();
                    log.info("Created new Gitea issue #{}: {}", newNumber, title);
                    return newNumber;
                } else {
                    log.error("Failed to create Gitea issue: {}", response.code());
                }
            }
        } catch (Exception e) {
            log.error("Failed to create or get Gitea review issue: {}", e.getMessage(), e);
        }
        return -1;
    }

    private void addIssueComment(String giteaUrl, String token, String repoFullName, int issueNumber, String comment) {
        try {
            String url = giteaUrl.replaceAll("/+$", "") + AiReviewConstants.GITEA_API_REPOS_PATH + repoFullName + "/issues/" + issueNumber + "/comments";
            ObjectNode body = objectMapper.createObjectNode();
            body.put(AiReviewConstants.BODY, comment);

            RequestBody requestBody = RequestBody.create(
                    objectMapper.writeValueAsString(body),
                    MediaType.parse(AiReviewConstants.MEDIA_TYPE_JSON)
            );

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader(AiReviewConstants.HEADER_AUTHORIZATION, AiReviewConstants.AUTH_TOKEN_PREFIX + token)
                    .post(requestBody)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                log.info("Added comment to Gitea issue #{}: {}", issueNumber, response.code());
            }
        } catch (Exception e) {
            log.error("Failed to add comment to Gitea issue: {}", e.getMessage(), e);
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

    private boolean isTargetBranchProtected(String giteaUrl, String token, String repoFullName, String targetBranch) {
        try {
            String url = giteaUrl.replaceAll("/+$", "") + AiReviewConstants.GITEA_API_REPOS_PATH + repoFullName + "/branches?protected=true";
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader(AiReviewConstants.HEADER_AUTHORIZATION, AiReviewConstants.AUTH_TOKEN_PREFIX + token)
                    .get()
                    .build();
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) return false;
                String body = response.body() != null ? response.body().string() : "[]";
                JsonNode branches = objectMapper.readTree(body);
                for (JsonNode branch : branches) {
                    String name = branch.path(AiReviewConstants.NAME).asText();
                    if (fnmatch(targetBranch, name)) return true;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to check Gitea protected branches: {}", e.getMessage());
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

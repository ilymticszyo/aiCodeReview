package io.binghe.ai.review.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.binghe.ai.review.constants.AiReviewConstants;
import io.binghe.ai.review.webhook.GitHubWebhookHandler;
import io.binghe.ai.review.webhook.GitLabWebhookHandler;
import io.binghe.ai.review.webhook.GiteaWebhookHandler;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author binghe(微信 : hacker_binghe)
 * @version 1.0.0
 * @description WebhookController
 * @github https://github.com/binghe001
 * @copyright 公众号: 冰河技术
 */
@Slf4j
@RestController
@RequestMapping("/review")
public class WebhookController {

    @Autowired
    private GitLabWebhookHandler gitLabWebhookHandler;

    @Autowired
    private GitHubWebhookHandler gitHubWebhookHandler;

    @Autowired
    private GiteaWebhookHandler giteaWebhookHandler;

    @Value("${gitlab.access-token:}")
    private String gitlabToken;

    @Value("${gitlab.url:https://gitlab.com}")
    private String gitlabUrl;

    @Value("${github.access-token:}")
    private String githubToken;

    @Value("${github.url:https://api.github.com}")
    private String githubUrl;

    @Value("${gitea.access-token:}")
    private String giteaToken;

    @Value("${gitea.url:}")
    private String giteaUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/")
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("<h2>AI Review Server is running.</h2>");
    }

    @PostMapping("/webhook")
    public ResponseEntity<Map<String, String>> handleWebhook(
            @RequestBody String body,
            HttpServletRequest request) {

        try {
            log.debug("Received webhook, headers: {}", request.getHeaderNames());

            JsonNode payload = objectMapper.readTree(body);

            // 识别webhook来源
            String giteaEvent = request.getHeader(AiReviewConstants.HEADER_GITEA_EVENT);
            String githubEvent = request.getHeader(AiReviewConstants.HEADER_GITHUB_EVENT);

            log.info("GitHub event: {}, Gitea event: {}", githubEvent, giteaEvent);

            // 优先识别Gitea（因为Gitea会同时发送两种header）
            if (giteaEvent != null && !giteaEvent.isBlank()) {
                return handleGiteaWebhook(giteaEvent, payload, request);
            } else if (githubEvent != null && !githubEvent.isBlank()) {
                return handleGitHubWebhook(githubEvent, payload, request);
            } else {
                return handleGitLabWebhook(payload, request);
            }

        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private ResponseEntity<Map<String, String>> handleGiteaWebhook(String eventType, JsonNode payload, HttpServletRequest request) {
        log.info("Processing Gitea webhook, event: {}", eventType);

        // 获取Gitea token
        String token = giteaToken;
        if (token == null || token.isBlank()) {
            token = request.getHeader(AiReviewConstants.HEADER_GITEA_TOKEN);
        }
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(AiReviewConstants.JSON_FIELD_ERROR, AiReviewConstants.WEBHOOK_ERR_MISSING_GITEA_TOKEN));
        }

        // 获取Gitea URL
        String url = giteaUrl;
        if (url == null || url.isBlank()) {
            url = request.getHeader(AiReviewConstants.HEADER_GITEA_INSTANCE);
        }
        if (url == null || url.isBlank()) {
            JsonNode repo = payload.path("repository");
            String htmlUrl = repo.path("html_url").asText("");
            if (!htmlUrl.isBlank()) {
                try {
                    java.net.URL parsedUrl = new java.net.URL(htmlUrl);
                    url = parsedUrl.getProtocol() + "://" + parsedUrl.getAuthority();
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body(Map.of(AiReviewConstants.JSON_FIELD_ERROR, AiReviewConstants.WEBHOOK_ERR_PARSE_GITEA_URL_FAILED));
                }
            }
        }
        if (url == null || url.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(AiReviewConstants.JSON_FIELD_ERROR, AiReviewConstants.WEBHOOK_ERR_MISSING_GITEA_URL));
        }

        final String finalToken = token;
        final String finalUrl = url;

        switch (eventType) {
            case AiReviewConstants.EVENT_PUSH -> {
                giteaWebhookHandler.handlePush(payload, finalToken, finalUrl);
                return ResponseEntity.ok(Map.of(AiReviewConstants.JSON_FIELD_MESSAGE, AiReviewConstants.WEBHOOK_MSG_GITEA_PUSH_RECEIVED));
            }
            case AiReviewConstants.EVENT_PULL_REQUEST -> {
                String action = payload.path("action").asText();
                if (!java.util.List.of(AiReviewConstants.ACTION_OPENED, AiReviewConstants.ACTION_SYNCHRONIZE).contains(action)) {
                    return ResponseEntity.ok(Map.of(AiReviewConstants.JSON_FIELD_MESSAGE, String.format(AiReviewConstants.WEBHOOK_MSG_GITEA_PR_ACTION_IGNORED, action)));
                }
                giteaWebhookHandler.handlePullRequest(payload, finalToken, finalUrl);
                return ResponseEntity.ok(Map.of("message", "Gitea PR event received, processing asynchronously"));
            }
            case AiReviewConstants.EVENT_ISSUE_COMMENT -> {
                return ResponseEntity.ok(Map.of("message", "Gitea issue_comment event ignored"));
            }
            default -> {
                return ResponseEntity.badRequest().body(Map.of("error", "Unsupported Gitea event: " + eventType));
            }
        }
    }

    private ResponseEntity<Map<String, String>> handleGitHubWebhook(String eventType, JsonNode payload, HttpServletRequest request) {
        log.info("Processing GitHub webhook, event: {}", eventType);

        String token = githubToken;
        if (token == null || token.isBlank()) {
            token = request.getHeader(AiReviewConstants.HEADER_GITHUB_TOKEN);
        }
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing GitHub access token"));
        }

        final String finalToken = token;

        switch (eventType) {
            case AiReviewConstants.EVENT_PULL_REQUEST -> {
                gitHubWebhookHandler.handlePullRequest(payload, finalToken, githubUrl);
                return ResponseEntity.ok(Map.of("message", "GitHub PR event received, processing asynchronously"));
            }
            case AiReviewConstants.EVENT_PUSH -> {
                gitHubWebhookHandler.handlePush(payload, finalToken, githubUrl);
                return ResponseEntity.ok(Map.of("message", "GitHub push event received, processing asynchronously"));
            }
            default -> {
                return ResponseEntity.badRequest().body(Map.of("error", "Unsupported GitHub event: " + eventType));
            }
        }
    }

    private ResponseEntity<Map<String, String>> handleGitLabWebhook(JsonNode payload, HttpServletRequest request) {
        String objectKind = payload.path("object_kind").asText();
        log.info("Processing GitLab webhook, object_kind: {}", objectKind);

        String token = gitlabToken;
        if (token == null || token.isBlank()) {
            token = request.getHeader(AiReviewConstants.HEADER_GITLAB_TOKEN);
        }
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing GitLab access token"));
        }

        String url = gitlabUrl;
        if (url == null || url.isBlank()) {
            url = request.getHeader(AiReviewConstants.HEADER_GITLAB_INSTANCE);
        }
        if (url == null || url.isBlank()) {
            JsonNode repository = payload.path("repository");
            if (!repository.isMissingNode()) {
                String homepage = repository.path("homepage").asText("");
                if (!homepage.isBlank()) {
                    try {
                        java.net.URL parsedUrl = new java.net.URL(homepage);
                        url = parsedUrl.getProtocol() + "://" + parsedUrl.getAuthority() + "/";
                    } catch (Exception e) {
                        return ResponseEntity.badRequest().body(Map.of("error", "Failed to parse homepage URL: " + e.getMessage()));
                    }
                }
            }
        }
        if (url == null || url.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing GitLab URL"));
        }

        final String finalToken = token;
        final String finalUrl = url;

        switch (objectKind) {
            case AiReviewConstants.EVENT_MERGE_REQUEST -> {
                gitLabWebhookHandler.handleMergeRequest(payload, finalToken, finalUrl);
                return ResponseEntity.ok(Map.of("message", "GitLab MR event received, processing asynchronously"));
            }
            case AiReviewConstants.EVENT_PUSH -> {
                gitLabWebhookHandler.handlePush(payload, finalToken, finalUrl);
                return ResponseEntity.ok(Map.of("message", "GitLab push event received, processing asynchronously"));
            }
            default -> {
                return ResponseEntity.badRequest().body(Map.of("error", "Unsupported GitLab event: " + objectKind));
            }
        }
    }
}

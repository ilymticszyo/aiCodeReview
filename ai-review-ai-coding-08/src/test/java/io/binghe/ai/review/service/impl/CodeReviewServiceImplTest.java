package io.binghe.ai.review.service.impl;

import io.binghe.ai.review.constants.AiReviewConstants;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CodeReviewServiceImplTest {

    private final CodeReviewServiceImpl codeReviewService = new CodeReviewServiceImpl();

    @Test
    void parseReviewScoreShouldSupportStandardTotalScore() {
        assertThat(codeReviewService.parseReviewScore("总结\n总分:80分")).isEqualTo(80);
    }

    @Test
    void parseReviewScoreShouldSupportChineseColonAndSpaces() {
        assertThat(codeReviewService.parseReviewScore("#### 总结\n总分： 80 分")).isEqualTo(80);
    }

    @Test
    void parseReviewScoreShouldSupportTotalScoreWithDenominator() {
        assertThat(codeReviewService.parseReviewScore("整体质量不错， 总评分：80/100")).isEqualTo(80);
    }

    @Test
    void parseReviewScoreShouldSupportMarkdownFormats() {
        assertThat(codeReviewService.parseReviewScore("| **总分** | 80分 |\n| --- | --- |")).isEqualTo(80);
    }

    @Test
    void parseReviewScoreShouldPreferMachineReadableMarker() {
        assertThat(codeReviewService.parseReviewScore("总分:120分\n<!-- AI_REVIEW_SCORE: 88 -->")).isEqualTo(88);
    }

    @Test
    void parseReviewScoreShouldReturnZeroWhenNoScoreExists() {
        assertThat(codeReviewService.parseReviewScore("没有明确分数字段")).isZero();
    }

    @Test
    void parseReviewScoreShouldIgnoreOutOfRangeScores() {
        assertThat(codeReviewService.parseReviewScore("总分:120分")).isZero();
    }

    @Test
    void loadPromptsShouldApplyHumorousStyleToLanguagePrompts() {
        assertHumorousPrompt(AiReviewConstants.PROMPT_KEY_JAVA);
        assertHumorousPrompt(AiReviewConstants.PROMPT_KEY_PYTHON);
        assertHumorousPrompt(AiReviewConstants.PROMPT_KEY_JAVASCRIPT);
    }

    @Test
    void loadPromptsShouldApplyProfessionalStyleToLanguagePrompts() {
        Map<String, String> prompts = loadPrompts(AiReviewConstants.PROMPT_KEY_JAVA, "professional");

        assertThat(prompts.get(AiReviewConstants.JSON_FIELD_SYSTEM_PROMPT))
                .contains("标准的工程术语")
                .doesNotContain("{%")
                .doesNotContain("{{ style }}");
    }

    private void assertHumorousPrompt(String promptKey) {
        Map<String, String> prompts = loadPrompts(promptKey, "humorous");

        assertThat(prompts.get(AiReviewConstants.JSON_FIELD_SYSTEM_PROMPT))
                .contains("适当幽默元素")
                .contains("AI_REVIEW_SCORE")
                .doesNotContain("{%")
                .doesNotContain("{{ style }}");
        assertThat(prompts.get(AiReviewConstants.JSON_FIELD_USER_PROMPT))
                .contains("humorous风格")
                .doesNotContain("{{ style }}");
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> loadPrompts(String promptKey, String style) {
        return (Map<String, String>) ReflectionTestUtils.invokeMethod(
                codeReviewService,
                "loadPrompts",
                promptKey,
                style
        );
    }
}

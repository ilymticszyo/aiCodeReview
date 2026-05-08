package io.binghe.ai.review.notifier;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FeishuNotifierTest {

    private final FeishuNotifier notifier = new FeishuNotifier();

    @Test
    void splitContentReturnsSingleChunkForShortContent() {
        String content = "short review result";

        List<String> chunks = notifier.splitContent(content, 8000);

        assertThat(chunks).containsExactly(content);
    }

    @Test
    void splitContentKeepsChineseCharactersIntact() {
        String content = "\u4f60\u597d\u4e16\u754c\u4ee3\u7801\u5ba1\u67e5\u7ed3\u679c\u5b8c\u6574\u4fdd\u7559";

        List<String> chunks = notifier.splitContent(content, 12);

        assertThat(String.join("", chunks)).isEqualTo(content);
        assertThat(chunks).allSatisfy(chunk ->
                assertThat(chunk.getBytes(StandardCharsets.UTF_8).length).isLessThanOrEqualTo(12));
    }

    @Test
    void splitContentHandlesLongEnglishWithoutNewlines() {
        String content = "abcdefghijklmnopqrstuvwxyz";

        List<String> chunks = notifier.splitContent(content, 5);

        assertThat(String.join("", chunks)).isEqualTo(content);
        assertThat(chunks).allSatisfy(chunk ->
                assertThat(chunk.getBytes(StandardCharsets.UTF_8).length).isLessThanOrEqualTo(5));
    }

    @Test
    void splitContentPrefersNewlineBoundary() {
        String content = "aaaa\nbbbb\ncccc";

        List<String> chunks = notifier.splitContent(content, 7);

        assertThat(chunks).containsExactly("aaaa\n", "bbbb\n", "cccc");
    }
}

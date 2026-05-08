package io.binghe.ai.review.service.impl;

import io.binghe.ai.review.constants.AiReviewConstants;
import io.binghe.ai.review.llm.LLMClient;
import io.binghe.ai.review.llm.LLMFactory;
import io.binghe.ai.review.service.CodeReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author binghe(微信 : hacker_binghe)
 * @version 1.0.0
 * @description CodeReview Servuce实现类
 * @github https://github.com/binghe001
 * @copyright 公众号: 冰河技术
 */
@Slf4j
@Service
public class CodeReviewServiceImpl implements CodeReviewService {

    @Autowired
    private LLMFactory llmFactory;

    @Value("${review.style:professional}")
    private String reviewStyle;

    @Value("${review.max-tokens:10000}")
    private int maxTokens;

    private static final Map<String, String> FILE_EXT_TO_LANG = new HashMap<>();
    private static final Map<String, String> LANG_TO_PROMPT_KEY = new HashMap<>();
    private static final List<Pattern> SCORE_PATTERNS = List.of(
            Pattern.compile("AI_REVIEW_SCORE\\s*[:：=]\\s*(\\d{1,3})", Pattern.CASE_INSENSITIVE),
            Pattern.compile("总\\s*评?分\\D{0,12}(\\d{1,3})(?:\\s*/\\s*100|\\s*分)?"),
            Pattern.compile("最终\\s*得分\\D{0,12}(\\d{1,3})(?:\\s*/\\s*100|\\s*分)?"),
            Pattern.compile("综合\\s*评分\\D{0,12}(\\d{1,3})(?:\\s*/\\s*100|\\s*分)?"),
            Pattern.compile("(?m)^\\s*[-*|>\\s`_]*(?:评分|得分)[`_\\s|：:=-]*(\\d{1,3})(?:\\s*/\\s*100|\\s*分)?")
    );

    static {
        FILE_EXT_TO_LANG.put(AiReviewConstants.FILE_EXT_PY, AiReviewConstants.LANG_PYTHON);
        FILE_EXT_TO_LANG.put(AiReviewConstants.FILE_EXT_JS, AiReviewConstants.LANG_JAVASCRIPT);
        FILE_EXT_TO_LANG.put(AiReviewConstants.FILE_EXT_TS, AiReviewConstants.LANG_TYPESCRIPT);
        FILE_EXT_TO_LANG.put(AiReviewConstants.FILE_EXT_JSX, AiReviewConstants.LANG_JAVASCRIPT);
        FILE_EXT_TO_LANG.put(AiReviewConstants.FILE_EXT_TSX, AiReviewConstants.LANG_TYPESCRIPT);
        FILE_EXT_TO_LANG.put(AiReviewConstants.FILE_EXT_VUE, AiReviewConstants.LANG_VUE);
        FILE_EXT_TO_LANG.put(AiReviewConstants.FILE_EXT_JAVA, AiReviewConstants.LANG_JAVA);
        FILE_EXT_TO_LANG.put(AiReviewConstants.FILE_EXT_GO, AiReviewConstants.LANG_GO);
        FILE_EXT_TO_LANG.put(AiReviewConstants.FILE_EXT_PHP, AiReviewConstants.LANG_PHP);
        FILE_EXT_TO_LANG.put(AiReviewConstants.FILE_EXT_CPP, AiReviewConstants.LANG_CPP);
        FILE_EXT_TO_LANG.put(AiReviewConstants.FILE_EXT_CC, AiReviewConstants.LANG_CPP);
        FILE_EXT_TO_LANG.put(AiReviewConstants.FILE_EXT_CXX, AiReviewConstants.LANG_CPP);
        FILE_EXT_TO_LANG.put(AiReviewConstants.FILE_EXT_C, AiReviewConstants.LANG_C);
        FILE_EXT_TO_LANG.put(AiReviewConstants.FILE_EXT_H, AiReviewConstants.LANG_CPP);
        FILE_EXT_TO_LANG.put(AiReviewConstants.FILE_EXT_HPP, AiReviewConstants.LANG_CPP);

        LANG_TO_PROMPT_KEY.put(AiReviewConstants.LANG_PYTHON, AiReviewConstants.PROMPT_KEY_PYTHON);
        LANG_TO_PROMPT_KEY.put(AiReviewConstants.LANG_JAVASCRIPT, AiReviewConstants.PROMPT_KEY_JAVASCRIPT);
        LANG_TO_PROMPT_KEY.put(AiReviewConstants.LANG_TYPESCRIPT, AiReviewConstants.PROMPT_KEY_TYPESCRIPT);
        LANG_TO_PROMPT_KEY.put(AiReviewConstants.LANG_JAVA, AiReviewConstants.PROMPT_KEY_JAVA);
        LANG_TO_PROMPT_KEY.put(AiReviewConstants.LANG_GO, AiReviewConstants.PROMPT_KEY_GO);
        LANG_TO_PROMPT_KEY.put(AiReviewConstants.LANG_PHP, AiReviewConstants.PROMPT_KEY_PHP);
        LANG_TO_PROMPT_KEY.put(AiReviewConstants.LANG_CPP, AiReviewConstants.PROMPT_KEY_CPP);
        LANG_TO_PROMPT_KEY.put(AiReviewConstants.LANG_C, AiReviewConstants.PROMPT_KEY_C);
        LANG_TO_PROMPT_KEY.put(AiReviewConstants.LANG_VUE3, AiReviewConstants.PROMPT_KEY_VUE3);
        LANG_TO_PROMPT_KEY.put(AiReviewConstants.FILE_EXT_JS, AiReviewConstants.PROMPT_KEY_JAVASCRIPT);
        LANG_TO_PROMPT_KEY.put(AiReviewConstants.FILE_EXT_TS, AiReviewConstants.PROMPT_KEY_TYPESCRIPT);
        LANG_TO_PROMPT_KEY.put(AiReviewConstants.FILE_EXT_PY, AiReviewConstants.PROMPT_KEY_PYTHON);
    }

    /**
     * 审查代码并去除markdown格式
     */
    @Override
    public String reviewAndStripCode(String changesText, String commitsText) {
        if (changesText == null || changesText.isBlank()) {
            log.info("代码为空");
            return AiReviewConstants.MSG_CODE_EMPTY;
        }

        // 检测语言
        String detectedLang = detectLanguageFromDiff(changesText);
        log.info("检测到的编程语言: {}", detectedLang);

        // Token限制截断（简单估算：1 token ≈ 4 chars）
        if (changesText.length() > maxTokens * 4) {
            log.info("代码过长，从 {} chars 截断到 {} tokens", changesText.length(), maxTokens);
            changesText = changesText.substring(0, maxTokens * 4);
            // 截断后重新检测语言
            String truncatedLang = detectLanguageFromDiff(changesText);
            if (!AiReviewConstants.LANG_DEFAULT.equals(truncatedLang)) {
                detectedLang = truncatedLang;
            }
        }

        String result = reviewCode(changesText, commitsText, detectedLang);
        result = result.strip();

        // 去掉markdown格式符号
        if (result.startsWith("```markdown") && result.endsWith("```")) {
            result = result.substring(11, result.length() - 3).strip();
        }

        return result;
    }


    /**
     * 执行代码审查
     */
    private String reviewCode(String diffsText, String commitsText, String language) {
        String promptKey = LANG_TO_PROMPT_KEY.getOrDefault(language, AiReviewConstants.PROMPT_KEY_DEFAULT);
        log.info("使用提示词: {}, 风格: {}", promptKey, reviewStyle);

        Map<String, String> prompts;
        try {
            prompts = loadPrompts(promptKey, reviewStyle);
        } catch (Exception e) {
            log.warn("加载语言特定提示词失败，使用通用提示词: {}", e.getMessage());
            try {
                prompts = loadPrompts(AiReviewConstants.PROMPT_KEY_FALLBACK, reviewStyle);
            } catch (Exception ex) {
                log.error("加载通用提示词也失败: {}", ex.getMessage());
                throw new RuntimeException("加载提示词失败", ex);
            }
        }

        String userContent = prompts.get(AiReviewConstants.JSON_FIELD_USER_PROMPT)
                .replace("{diffs_text}", diffsText)
                .replace("{commits_text}", commitsText != null ? commitsText : "");

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of(AiReviewConstants.LLM_FIELD_ROLE, AiReviewConstants.LLM_ROLE_SYSTEM,
                AiReviewConstants.LLM_FIELD_CONTENT, prompts.get(AiReviewConstants.JSON_FIELD_SYSTEM_PROMPT)));
        messages.add(Map.of(AiReviewConstants.LLM_FIELD_ROLE, AiReviewConstants.LLM_ROLE_USER,
                AiReviewConstants.LLM_FIELD_CONTENT, userContent));

        LLMClient client = llmFactory.getClient();
        return client.completions(messages);
    }

    /**
     * 从diff文本中检测主要编程语言
     */
    @Override
    public String detectLanguageFromDiff(String diffsText) {
        if (diffsText == null || diffsText.isBlank()) {
            return AiReviewConstants.LANG_DEFAULT;
        }

        Map<String, Integer> languageCounts = new HashMap<>();
        int vue3Indicators = 0;

        String[] filePatterns = {
                AiReviewConstants.REGEX_URL_ANY_CHAR + "\\+\\+\\+ b/(.+)$",
                AiReviewConstants.REGEX_URL_ANY_CHAR + "\\+\\+\\+ (.+)$",
                AiReviewConstants.REGEX_URL_ANY_CHAR + "--- a/(.+)$",
                AiReviewConstants.REGEX_URL_ANY_CHAR + "--- (.+)$"
        };

        String[] lines = diffsText.split(AiReviewConstants.DIFF_SEPARATOR_NEWLINE);
        for (String line : lines) {
            // 尝试所有文件路径模式
            for (String pattern : filePatterns) {
                Matcher matcher = Pattern.compile(pattern).matcher(line);
                if (matcher.find()) {
                    String filePath = matcher.group(1);
                    String ext = getExtension(filePath).toLowerCase();
                    String lang = FILE_EXT_TO_LANG.get(ext);
                    if (lang != null) {
                        languageCounts.merge(lang, 1, Integer::sum);
                    }
                    break;
                }
            }

            // diff --git格式
            Matcher diffGitMatcher = Pattern.compile("^diff --git a/(.+) b/(.+)$").matcher(line);
            if (diffGitMatcher.find()) {
                String filePath = diffGitMatcher.group(1);
                String ext = getExtension(filePath).toLowerCase();
                String lang = FILE_EXT_TO_LANG.get(ext);
                if (lang != null) {
                    languageCounts.merge(lang, 1, Integer::sum);
                }
            }

            // 检查Vue3特征
            String lineLower = line.toLowerCase();
            if (containsAny(lineLower, "setup()", "defineprops", "defineemits", "ref(", "reactive(",
                    "computed(", "watch(", "onmounted", "onunmounted", "script setup", "<script setup")) {
                vue3Indicators++;
            }
        }

        // 如果没有通过文件路径检测到语言，尝试从内容中检测
        if (languageCounts.isEmpty()) {
            String diffsLower = diffsText.toLowerCase();
            if (containsAny(diffsLower, "<template>", "<script>", "<style>", ".vue")) {
                languageCounts.put(AiReviewConstants.LANG_VUE, 1);
            } else if (containsAny(diffsLower, "function", "var ", "let ", "const ", "console.log", "document.")) {
                languageCounts.put(AiReviewConstants.LANG_JAVASCRIPT, 1);
            } else if (containsAny(diffsLower, "def ", "import ", "from ", "class ", "if __name__", "self.")) {
                languageCounts.put(AiReviewConstants.LANG_PYTHON, 1);
            }
        }

        if (!languageCounts.isEmpty()) {
            String primaryLang = languageCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(AiReviewConstants.LANG_DEFAULT);
            log.info("检测到主要编程语言: {}, Vue3指标: {}", primaryLang, vue3Indicators);
            return primaryLang;
        }

        return AiReviewConstants.LANG_DEFAULT;
    }

    /**
     * 解析AI返回的审查结果中的分数
     */
    @Override
    public int parseReviewScore(String reviewText) {
        if (reviewText == null || reviewText.isBlank()) {
            return 0;
        }
        for (Pattern pattern : SCORE_PATTERNS) {
            Matcher matcher = pattern.matcher(reviewText);
            while (matcher.find()) {
                int score = Integer.parseInt(matcher.group(1));
                if (score >= 0 && score <= 100) {
                    return score;
                }
                log.warn("解析到超出范围的AI审查评分: {}", score);
            }
        }
        log.warn("未能从AI审查结果中解析评分");
        return 0;
    }

    /**
     * 加载提示词配置
     */
    @SuppressWarnings("unchecked")
    private Map<String, String> loadPrompts(String promptKey, String style) {
        Yaml yaml = new Yaml();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(AiReviewConstants.PROMPT_TEMPLATES_FILE)) {
            if (is == null) {
                throw new RuntimeException("prompt_templates.yml not found in classpath");
            }
            Map<String, Object> allPrompts = yaml.load(is);
            Map<String, Object> promptData = (Map<String, Object>) allPrompts.get(promptKey);
            if (promptData == null) {
                throw new RuntimeException("Prompt key not found: " + promptKey);
            }

            String systemPrompt = (String) promptData.get(AiReviewConstants.JSON_FIELD_SYSTEM_PROMPT);
            String userPrompt = (String) promptData.get(AiReviewConstants.JSON_FIELD_USER_PROMPT);

            // 简单替换Jinja2模板变量
            systemPrompt = processTemplate(systemPrompt, style);
            userPrompt = processTemplate(userPrompt, style);

            Map<String, String> result = new HashMap<>();
            result.put(AiReviewConstants.JSON_FIELD_SYSTEM_PROMPT, systemPrompt);
            result.put(AiReviewConstants.JSON_FIELD_USER_PROMPT, userPrompt);
            return result;
        } catch (Exception e) {
            log.error("加载提示词配置失败: {}", e.getMessage(), e);
            throw new RuntimeException("加载提示词配置失败: " + e.getMessage(), e);
        }
    }

    /**
     * 简单处理Jinja2模板（替换变量和条件块）
     */
    private String processTemplate(String template, String style) {
        if (template == null) return "";

        // 替换 {{ style }} 变量
        template = template.replace("{{ style }}", style);

        // 处理 {% if style == 'xxx' %} ... {% elif ... %} ... {% endif %} 块
        template = processStyleConditions(template, style);

        return template;
    }

    /**
     * 处理Jinja2样式条件块
     */
    private String processStyleConditions(String template, String style) {
        // 处理多重条件块
        StringBuilder result = new StringBuilder();
        int pos = 0;

        while (pos < template.length()) {
            int ifStart = template.indexOf("{% if style ==", pos);
            if (ifStart == -1) {
                result.append(template.substring(pos));
                break;
            }

            // 添加if之前的内容
            result.append(template, pos, ifStart);

            // 找到对应的endif
            int endIfPos = template.indexOf("{% endif %}", ifStart);
            if (endIfPos == -1) {
                result.append(template.substring(ifStart));
                break;
            }

            // 提取整个条件块
            String condBlock = template.substring(ifStart, endIfPos + 11);
            String resolved = resolveConditionBlock(condBlock, style);
            result.append(resolved);

            pos = endIfPos + 11;
        }

        return result.toString();
    }

    /**
     * 解析条件块，返回匹配当前style的内容
     */
    private String resolveConditionBlock(String block, String style) {
        // 解析所有分支
        List<String[]> branches = new ArrayList<>(); // [condition, content]

        String current = block;
        // 找到第一个if
        int firstIfEnd = current.indexOf("%}");
        if (firstIfEnd == -1) return "";

        String firstCondLine = current.substring(0, firstIfEnd + 2);
        String firstCondValue = extractStyleValue(firstCondLine);
        current = current.substring(firstIfEnd + 2);

        // 寻找elif和else块
        String currentCond = firstCondValue;
        int searchPos = 0;
        int blockStart = 0;

        while (searchPos < current.length()) {
            int elifPos = current.indexOf("{%", searchPos);
            if (elifPos == -1) {
                branches.add(new String[]{currentCond, current.substring(blockStart)});
                break;
            }
            int blockEnd = current.indexOf("%}", elifPos);
            if (blockEnd == -1) break;

            String tag = current.substring(elifPos, blockEnd + 2).trim();

            if (tag.startsWith("{% elif") || tag.startsWith("{%elif") || tag.startsWith("{% else") || tag.startsWith("{%else")) {
                branches.add(new String[]{currentCond, current.substring(blockStart, elifPos)});

                if (tag.contains("style ==")) {
                    currentCond = extractStyleValue(tag);
                } else {
                    currentCond = "else";
                }
                blockStart = blockEnd + 2;
                searchPos = blockEnd + 2;
            } else if (tag.startsWith("{% endif") || tag.startsWith("{%endif")) {
                branches.add(new String[]{currentCond, current.substring(blockStart, elifPos)});
                break;
            } else {
                searchPos = blockEnd + 2;
            }
        }

        // 找到匹配的分支
        for (String[] branch : branches) {
            if (style.equals(branch[0]) || "else".equals(branch[0])) {
                return branch[1].strip();
            }
        }

        return "";
    }

    private String extractStyleValue(String condLine) {
        Pattern p = Pattern.compile("style\\s*==\\s*'([^']+)'");
        Matcher m = p.matcher(condLine);
        if (m.find()) {
            return m.group(1);
        }
        return "else";
    }

    private String getExtension(String filePath) {
        int lastDot = filePath.lastIndexOf('.');
        if (lastDot < 0 || lastDot == filePath.length() - 1) return "";
        int lastSlash = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
        if (lastDot < lastSlash) return "";
        return filePath.substring(lastDot);
    }

    private boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }
}

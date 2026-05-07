package io.binghe.ai.review.constants;

/**
 * AI Review 全局常量类
 * 统一管理项目中所有写死的字符串常量
 */
public final class AiReviewConstants {

    private AiReviewConstants() {}

    // ============================
    // HTTP 请求头名称
    // ============================

    /** Gitea Webhook 事件头 */
    public static final String HEADER_GITEA_EVENT = "X-Gitea-Event";
    /** GitHub Webhook 事件头 */
    public static final String HEADER_GITHUB_EVENT = "X-GitHub-Event";
    /** Gitea Token 请求头 */
    public static final String HEADER_GITEA_TOKEN = "X-Gitea-Token";
    /** GitHub Token 请求头 */
    public static final String HEADER_GITHUB_TOKEN = "X-GitHub-Token";
    /** GitLab Token 请求头 */
    public static final String HEADER_GITLAB_TOKEN = "X-Gitlab-Token";
    /** GitLab 实例地址请求头 */
    public static final String HEADER_GITLAB_INSTANCE = "X-Gitlab-Instance";
    /** Gitea 实例地址请求头 */
    public static final String HEADER_GITEA_INSTANCE = "X-Gitea-Instance";
    /** GitLab API 私有 Token 请求头 */
    public static final String HEADER_GITLAB_PRIVATE_TOKEN = "PRIVATE-TOKEN";
    /** HTTP Authorization 请求头 */
    public static final String HEADER_AUTHORIZATION = "Authorization";
    /** HTTP Accept 请求头 */
    public static final String HEADER_ACCEPT = "Accept";
    /** HTTP Content-Type 请求头 */
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    /** HTTP Charset 请求头 */
    public static final String HEADER_CHARSET = "Charset";
    public static final String AUTHOR = "author";
    public static final String COMMIT_MESSAGES = "commit_messages";
    public static final String SOURCE_BRANCH = "source_branch";
    public static final String TARGET_BRANCH = "target_branch";
    public static final String BRANCH = "branch";
    public static final String URL = "url";
    public static final String WEB_URL = "web_url";
    public static final String COMMITS_URL = "commits_url";
    public static final int ZERO = 0;
    public static final String MODEL = "model";
    public static final String ROLE = "role";
    public static final String CONTENT = "content";
    public static final String CHOICES = "choices";
    public static final String MESSAGES = "messages";
    public static final String MESSAGE = "message";
    public static final String ACTION = "action";
    public static final String PROJECT = "project";
    public static final String NAME = "name";
    public static final String USERNAME = "username";
    public static final String USER_NAME = "user_name";
    public static final String REF = "ref";
    public static final String BASE = "base";
    public static final String PUSHER = "pusher";
    public static final String HTML_URL = "html_url";
    public static final String USER = "user";
    public static final String SENDER = "sender";
    public static final String LOGIN = "login";
    public static final String HEAD = "head";
    public static final String LABEL = "label";
    public static final String ID = "id";
    public static final String BODY = "body";
    public static final String NOTE = "note";
    public static final String NEW_PATH = "new_path";
    public static final String CHANGES = "changes";
    public static final String COMPARE_URL = "compare_url";
    public static final String IID = "iid";
    public static final String PROJECT_ID = "project_id";
    public static final String COMMITS = "commits";
    public static final String COMMIT = "commit";
    public static final String HEAD_COMMIT = "head_commit";
    public static final String COMPARE = "compare";
    public static final String ADDED = "added";
    public static final String MODIFIED = "modified";
    public static final String PULL_REQUEST = "pull_request";
    public static final String REPOSITORY = "repository";
    public static final String FULL_NAME = "full_name";
    public static final String FILENAME = "filename";
    public static final String PATCH = "patch";
    public static final String NUMBER = "number";
    public static final String FILES = "files";
    public static final String TITLE = "title";
    public static final String TEMPERATURE = "temperature";
    public static final String APPLICATION_JSON = "application/json";
    public static final String OBJECT_ATTRIBUTES = "object_attributes";

    // ============================
    // HTTP 认证前缀
    // ============================

    /** GitHub/Gitea 认证前缀（token 认证） */
    public static final String AUTH_TOKEN_PREFIX = "token ";
    /** OpenAI 兼容 API 认证前缀（Bearer 认证） */
    public static final String AUTH_BEARER_PREFIX = "Bearer ";

    // ============================
    // HTTP Accept / Media Type 值
    // ============================

    /** GitHub API Accept 头 */
    public static final String GITHUB_ACCEPT_HEADER = "application/vnd.github.v3+json";
    /** JSON 媒体类型（含编码） */
    public static final String MEDIA_TYPE_JSON = "application/json; charset=utf-8";
    /** UTF-8 编码值 */
    public static final String CHARSET_UTF8 = "UTF-8";

    // ============================
    // Webhook 事件类型
    // ============================

    /** Push 事件 */
    public static final String EVENT_PUSH = "push";
    /** Pull Request 事件（GitHub/Gitea） */
    public static final String EVENT_PULL_REQUEST = "pull_request";
    /** Issue Comment 事件（Gitea） */
    public static final String EVENT_ISSUE_COMMENT = "issue_comment";
    /** Merge Request 事件（GitLab） */
    public static final String EVENT_MERGE_REQUEST = "merge_request";

    // ============================
    // PR / MR Action 类型
    // ============================

    /** PR/MR 操作：opened（GitHub/Gitea） */
    public static final String ACTION_OPENED = "opened";
    /** PR/MR 操作：synchronize（GitHub/Gitea） */
    public static final String ACTION_SYNCHRONIZE = "synchronize";
    /** PR/MR 操作：edited（GitHub） */
    public static final String ACTION_EDITED = "edited";
    /** MR 操作：open（GitLab） */
    public static final String ACTION_OPEN = "open";
    /** MR 操作：update（GitLab） */
    public static final String ACTION_UPDATE = "update";
    /** MR 操作：reopen（GitLab） */
    public static final String ACTION_REOPEN = "reopen";
    /** MR 操作：merge（GitLab） */
    public static final String ACTION_MERGE = "merge";

    // ============================
    // 审查类型标签
    // ============================

    /** 审查类型：Merge Request（GitLab） */
    public static final String REVIEW_TYPE_MERGE_REQUEST = "Merge Request";
    /** 审查类型：Pull Request（GitHub/Gitea） */
    public static final String REVIEW_TYPE_PULL_REQUEST = "Pull Request";
    /** 审查类型：Push */
    public static final String REVIEW_TYPE_PUSH = "Push";

    // ============================
    // Git 分支引用前缀
    // ============================

    /** Git 分支引用前缀 */
    public static final String GIT_REFS_HEADS_PREFIX = "refs/heads/";

    // ============================
    // API URL 路径
    // ============================

    /** GitLab API v4 projects 路径前缀 */
    public static final String GITLAB_API_PROJECTS_PATH = "/api/v4/projects/";
    /** Gitea API v1 repos 路径前缀 */
    public static final String GITEA_API_REPOS_PATH = "/api/v1/repos/";
    /** GitHub 默认 API 基础地址 */
    public static final String GITHUB_DEFAULT_API_URL = "https://api.github.com";
    /** GitHub API 主机名（用于判断） */
    public static final String GITHUB_API_HOSTNAME = "api.github.com";
    /** OpenAI 兼容 chat completions 路径 */
    public static final String LLM_CHAT_COMPLETIONS_PATH = "/chat/completions";
    public static final String GLM_CHAT_COMPLETIONS_PATH = "/api/anthropic";

    public static final String LLM_PROVIDER_DEEPSEEK = "deepseek";
    public static final String LLM_PROVIDER_QWEN = "qwen";
    public static final String LLM_PROVIDER_GLM = "glm";
    public static final String LLM_MODEL_GLM_47_FLASH = "glm-4.7-flash";

    // ============================
    // 环境变量 Key 前缀
    // ============================

    /** 钉钉 Webhook URL 环境变量前缀 */
    public static final String ENV_DINGTALK_WEBHOOK_URL_PREFIX = "DINGTALK_WEBHOOK_URL_";
    /** 企业微信 Webhook URL 环境变量前缀 */
    public static final String ENV_WECOM_WEBHOOK_URL_PREFIX = "WECOM_WEBHOOK_URL_";
    /** 飞书 Webhook URL 环境变量前缀 */
    public static final String ENV_FEISHU_WEBHOOK_URL_PREFIX = "FEISHU_WEBHOOK_URL_";

    // ============================
    // 业务消息字符串
    // ============================

    /** 服务异常通知前缀 */
    public static final String MSG_ERROR_SERVICE_PREFIX = "AI Code Review 服务出现未知错误: ";
    /** Push/PR 中无关注文件变更时的提示 */
    public static final String MSG_NO_WATCHED_FILES_CHANGED = "关注的文件没有修改";
    /** 今日无代码审查记录提示 */
    public static final String MSG_NO_DAILY_RECORDS = "今日暂无代码审查记录";
    /** 日报生成失败前缀（ReportService） */
    public static final String MSG_DAILY_REPORT_FAILED_PREFIX = "日报生成失败: ";
    /** 代码为空提示 */
    public static final String MSG_CODE_EMPTY = "代码为空";
    /** 代码审查通知标题格式（含占位符 %s） */
    public static final String MSG_REVIEW_NOTIFICATION_TITLE_FORMAT = "代码审查通知 - %s";
    /** 日报通知标题 */
    public static final String MSG_DAILY_REPORT_TITLE = "代码提交日报";
    /** 审查内容截断提示 */
    public static final String MSG_CONTENT_TRUNCATED = "...(内容过长已截断)";
    /** 日报兜底生成时的报告头部 */
    public static final String MSG_DAILY_REPORT_FALLBACK_HEADER = "## 今日代码提交日报\n\n";
    /** 生成日报失败前缀（ReportController） */
    public static final String MSG_GENERATE_DAILY_REPORT_FAILED_PREFIX = "生成日报失败: ";

    // ============================
    // Gitea Issue 相关
    // ============================

    /** Gitea Review Issue PR 标题前缀 */
    public static final String GITEA_REVIEW_ISSUE_PR_TITLE_PREFIX = "[AI Review] PR #";
    /** Gitea Review Issue Push 标题前缀 */
    public static final String GITEA_REVIEW_ISSUE_TITLE_PREFIX = "[AI Review] ";
    /** Gitea Review Issue 默认 body */
    public static final String GITEA_REVIEW_ISSUE_DEFAULT_BODY = "AI Code Review Issue";

    // ============================
    // LLM / 提示词相关
    // ============================

    /** 默认提示词 Key（无法检测到语言时使用） */
    public static final String PROMPT_KEY_DEFAULT = "vue3_review_prompt";
    /** 兜底提示词 Key（语言特定提示词加载失败时使用） */
    public static final String PROMPT_KEY_FALLBACK = "code_review_prompt";
    /** 提示词模板资源文件名 */
    public static final String PROMPT_TEMPLATES_FILE = "prompt_templates.yml";
    /** 语言检测默认值（未检测到语言时返回） */
    public static final String LANG_DEFAULT = "default";

    // ============================
    // 数字常量
    // ============================

    /** 通知中审查结果最大展示字符数 */
    public static final int MAX_REVIEW_RESULT_LENGTH = 3000;

    // ============================
    // 文件扩展名常量
    // ============================

    /** Python文件扩展名 */
    public static final String FILE_EXT_PY = ".py";
    /** JavaScript文件扩展名 */
    public static final String FILE_EXT_JS = ".js";
    /** TypeScript文件扩展名 */
    public static final String FILE_EXT_TS = ".ts";
    /** JSX文件扩展名 */
    public static final String FILE_EXT_JSX = ".jsx";
    /** TSX文件扩展名 */
    public static final String FILE_EXT_TSX = ".tsx";
    /** Vue文件扩展名 */
    public static final String FILE_EXT_VUE = ".vue";
    /** Java文件扩展名 */
    public static final String FILE_EXT_JAVA = ".java";
    /** Go文件扩展名 */
    public static final String FILE_EXT_GO = ".go";
    /** PHP文件扩展名 */
    public static final String FILE_EXT_PHP = ".php";
    /** C++文件扩展名 */
    public static final String FILE_EXT_CPP = ".cpp";
    /** C源文件扩展名 */
    public static final String FILE_EXT_CC = ".cc";
    /** C++源文件扩展名 */
    public static final String FILE_EXT_CXX = ".cxx";
    /** C文件扩展名 */
    public static final String FILE_EXT_C = ".c";
    /** C头文件扩展名 */
    public static final String FILE_EXT_H = ".h";
    /** C++头文件扩展名 */
    public static final String FILE_EXT_HPP = ".hpp";

    // ============================
    // 编程语言常量
    // ============================

    /** Python语言 */
    public static final String LANG_PYTHON = "python";
    /** JavaScript语言 */
    public static final String LANG_JAVASCRIPT = "javascript";
    /** TypeScript语言 */
    public static final String LANG_TYPESCRIPT = "typescript";
    /** Java语言 */
    public static final String LANG_JAVA = "java";
    /** Go语言 */
    public static final String LANG_GO = "go";
    /** PHP语言 */
    public static final String LANG_PHP = "php";
    /** C++语言 */
    public static final String LANG_CPP = "cpp";
    /** C语言 */
    public static final String LANG_C = "c";
    /** Vue语言 */
    public static final String LANG_VUE = "vue";
    /** Vue3语言 */
    public static final String LANG_VUE3 = "vue3";

    // ============================
    // 提示词Key常量
    // ============================

    /** Python提示词Key */
    public static final String PROMPT_KEY_PYTHON = "python_review_prompt";
    /** JavaScript提示词Key */
    public static final String PROMPT_KEY_JAVASCRIPT = "javascript_review_prompt";
    /** TypeScript提示词Key */
    public static final String PROMPT_KEY_TYPESCRIPT = "typescript_review_prompt";
    /** Java提示词Key */
    public static final String PROMPT_KEY_JAVA = "java_review_prompt";
    /** Go提示词Key */
    public static final String PROMPT_KEY_GO = "go_review_prompt";
    /** PHP提示词Key */
    public static final String PROMPT_KEY_PHP = "php_review_prompt";
    /** C++提示词Key */
    public static final String PROMPT_KEY_CPP = "cpp_review_prompt";
    /** C提示词Key */
    public static final String PROMPT_KEY_C = "c_review_prompt";
    /** Vue3提示词Key */
    public static final String PROMPT_KEY_VUE3 = "vue3_review_prompt";

    // ============================
    // LLM消息字段常量
    // ============================

    /** LLM消息字段：role */
    public static final String LLM_FIELD_ROLE = "role";
    /** LLM消息字段：content */
    public static final String LLM_FIELD_CONTENT = "content";
    /** LLM系统提示词字段 */
    public static final String LLM_FIELD_SYSTEM_PROMPT = "system_prompt";
    /** LLM用户提示词字段 */
    public static final String LLM_FIELD_USER_PROMPT = "user_prompt";
    /** LLM系统角色 */
    public static final String LLM_ROLE_SYSTEM = "system";
    /** LLM用户角色 */
    public static final String LLM_ROLE_USER = "user";

    // ============================
    // JSON字段常量
    // ============================

    /** JSON字段：author */
    public static final String JSON_FIELD_AUTHOR = "author";
    /** JSON字段：commit_messages */
    public static final String JSON_FIELD_COMMIT_MESSAGES = "commit_messages";
    /** JSON字段：project_name */
    public static final String JSON_FIELD_PROJECT_NAME = "project_name";
    /** JSON字段：score */
    public static final String JSON_FIELD_SCORE = "score";
    /** JSON字段：role */
    public static final String JSON_FIELD_ROLE = "role";
    /** JSON字段：content */
    public static final String JSON_FIELD_CONTENT = "content";
    /** JSON字段：system_prompt */
    public static final String JSON_FIELD_SYSTEM_PROMPT = "system_prompt";
    /** JSON字段：user_prompt */
    public static final String JSON_FIELD_USER_PROMPT = "user_prompt";

    // ============================
    // Diff处理常量
    // ============================

    /** 文件路径模式：diff --git前缀 */
    public static final String FILE_PATTERN_DIFF_GIT_PREFIX = "diff --git a/";
    /** Diff分隔符：换行 */
    public static final String DIFF_SEPARATOR_NEWLINE = "\n";
    /** 匹配任意字符的转义正则 */
    public static final String REGEX_URL_ANY_CHAR = ".*";

    // ============================
    // 代码审查过滤常量
    // ============================

    /** 推荐的代码审查文件扩展名（逗号分隔） */
    public static final String REVIEW_SUPPORTED_EXTENSIONS = ".java,.py,.php,.yml,.vue,.go,.c,.cpp,.h,.js,.css,.md,.sql,.ts,.tsx,.jsx";
    /** 推荐的文件扩展名（逗号分隔，用于过滤） */
    public static final String FILE_FILTER_SUPPORTED_EXTENSIONS = ".java,.py,.php,.yml,.vue,.go,.c,.cpp,.h,.js,.css,.md,.sql,.ts,.tsx,.jsx";

    // ============================
    // Webhook过滤常量
    // ============================

    /** 仅受保护分支开关 */
    public static final String WEBHOOK_FILTER_ONLY_PROTECTED_BRANCHES = "review.only-protected-branches";
    /** 仅指定分支开关 */
    public static final String WEBHOOK_FILTER_ONLY_BRANCH_NAME = "review.only-branch-name";
    /** Push审查开关 */
    public static final String WEBHOOK_FILTER_PUSH_ENABLED = "review.push-enabled";

    // ============================
    // 数据库字段常量
    // ============================

    /** 日报数据表名 */
    public static final String REPORT_DAILY_TABLE_NAME = "daily_reports";
    /** 日志数据表名 */
    public static final String REVIEW_LOG_TABLE_NAME = "review_logs";

    // ============================
    // API响应字段名常量
    // ============================

    /** API响应字段：data */
    public static final String JSON_FIELD_DATA = "data";
    /** API响应字段：total */
    public static final String JSON_FIELD_TOTAL = "total";
    /** API响应字段：average_score */
    public static final String JSON_FIELD_AVERAGE_SCORE = "average_score";
    /** API响应字段：error */
    public static final String JSON_FIELD_ERROR = "error";
    /** API响应字段：message */
    public static final String JSON_FIELD_MESSAGE = "message";
    /** API响应字段：name */
    public static final String JSON_FIELD_NAME = "name";
    /** API响应字段：authors */
    public static final String JSON_FIELD_AUTHORS = "authors";
    /** API响应字段：count */
    public static final String JSON_FIELD_COUNT = "count";
    /** API响应字段：id */
    public static final String JSON_FIELD_ID = "id";
    /** API响应字段：code_lines */
    public static final String JSON_FIELD_CODE_LINES = "code_lines";
    /** API响应字段：delta */
    public static final String JSON_FIELD_DELTA = "delta";
    /** API响应字段：branch */
    public static final String JSON_FIELD_BRANCH = "branch";
    /** API响应字段：source_branch */
    public static final String JSON_FIELD_SOURCE_BRANCH = "source_branch";
    /** API响应字段：target_branch */
    public static final String JSON_FIELD_TARGET_BRANCH = "target_branch";
    /** API响应字段：updated_at */
    public static final String JSON_FIELD_UPDATED_AT = "updated_at";
    /** API响应字段：url */
    public static final String JSON_FIELD_URL = "url";
    /** API响应字段：review_result */
    public static final String JSON_FIELD_REVIEW_RESULT = "review_result";
    /** API响应字段：additions */
    public static final String JSON_FIELD_ADDITIONS = "additions";
    /** API响应字段：deletions */
    public static final String JSON_FIELD_DELETIONS = "deletions";

    // ============================
    // API参数名常量
    // ============================

    /** API参数：type */
    public static final String API_PARAM_TYPE = "type";
    /** API参数：updated_at_gte */
    public static final String API_PARAM_UPDATED_AT_GTE = "updated_at_gte";
    /** API参数：updated_at_lte */
    public static final String API_PARAM_UPDATED_AT_LTE = "updated_at_lte";
    /** API参数：project_names */
    public static final String API_PARAM_PROJECT_NAMES = "project_names";

    // ============================
    // Markdown格式字符串常量
    // ============================

    /** Markdown章节前缀 */
    public static final String MARKDOWN_SECTION_PREFIX = "## ";
    /** Markdown章节分隔符 */
    public static final String MARKDOWN_SECTION_SEPARATOR = "\n---\n\n";
    /** Markdown粗体前缀 */
    public static final String MARKDOWN_BOLD_PREFIX = "**";
    /** Markdown粗体后缀 */
    public static final String MARKDOWN_BOLD_SUFFIX = "**";
    /** Markdown链接前缀 */
    public static final String MARKDOWN_LINK_PREFIX = "[查看详情](";
    /** Markdown链接后缀 */
    public static final String MARKDOWN_LINK_SUFFIX = ")";
    /** Markdown列表项前缀 */
    public static final String MARKDOWN_LIST_ITEM_PREFIX = "- ";
    /** Markdown编号前缀 */
    public static final String MARKDOWN_NUMBERED_PREFIX = "- **";
    /** Markdown编号后缀 */
    public static final String MARKDOWN_NUMBERED_SUFFIX = "** 提交到 ";
    /** Markdown编号引用前缀 */
    public static final String MARKDOWN_NUMBERED_QUOTE_PREFIX = "（评分：";
    /** Markdown编号引用后缀 */
    public static final String MARKDOWN_NUMBERED_QUOTE_SUFFIX = "分）\n";
    /** Markdown分支连接符 */
    public static final String MARKDOWN_BRANCH_SEPARATOR = " -> ";
    /** Markdown项目标签 */
    public static final String MARKDOWN_PROJECT_LABEL = "**项目**:";
    /** Markdown提交者标签 */
    public static final String MARKDOWN_AUTHOR_LABEL = "**提交者**:";
    /** Markdown类型标签 */
    public static final String MARKDOWN_TYPE_LABEL = "**类型**:";
    /** Markdown分支标签 */
    public static final String MARKDOWN_BRANCH_LABEL = "**分支**:";
    /** Markdown评分标签 */
    public static final String MARKDOWN_SCORE_LABEL = "**评分**:";
    /** Markdown评分单位 */
    public static final String MARKDOWN_SCORE_UNIT = "分";
    /** Markdown链接标签 */
    public static final String MARKDOWN_LINK_LABEL = "**链接**:";
    /** Markdown通知标题前缀 */
    public static final String MARKDOWN_NOTIFICATION_TITLE = "## 代码审查通知\n\n";

    /** 通知系统数据字段名 */
    public static final String NOTIFICATION_FIELD_CONTENT = "content";
    public static final String NOTIFICATION_FIELD_MSG_TYPE = "msg_type";
    public static final String NOTIFICATION_FIELD_TITLE = "title";
    public static final String NOTIFICATION_FIELD_IS_AT_ALL = "is_at_all";
    public static final String NOTIFICATION_FIELD_PROJECT_NAME = "project_name";
    public static final String NOTIFICATION_FIELD_URL_SLUG = "url_slug";

    // ============================
    // Feishu消息类型常量
    // ============================

    /** Feishu消息类型：interactive */
    public static final String FEISHU_MSG_TYPE_INTERACTIVE = "interactive";
    /** Feishu消息类型：text */
    public static final String FEISHU_MSG_TYPE_TEXT = "text";
    /** Feishu卡片schema版本 */
    public static final String FEISHU_CARD_SCHEMA = "2.0";
    /** Feishu配置：update_multi */
    public static final String FEISHU_CONFIG_UPDATE_MULTI = "update_multi";
    /** Feishu配置：normal_v2 */
    public static final String FEISHU_TEXT_SIZE_NORMAL_V2 = "normal_v2";
    /** Feishu配置：normal */
    public static final String FEISHU_TEXT_SIZE_NORMAL = "normal";
    /** Feishu配置：pc */
    public static final String FEISHU_TEXT_SIZE_PC = "pc";
    /** Feishu配置：mobile */
    public static final String FEISHU_TEXT_SIZE_MOBILE = "mobile";
    /** Feishu配置：heading */
    public static final String FEISHU_TEXT_SIZE_HEADING = "heading";
    /** Feishu卡片方向 */
    public static final String FEISHU_CARD_DIRECTION = "direction";
    /** Feishu卡片方向：vertical */
    public static final String FEISHU_CARD_DIRECTION_VERTICAL = "vertical";
    /** Feishu卡片padding */
    public static final String FEISHU_CARD_PADDING = "padding";
    /** Feishu元素tag */
    public static final String FEISHU_ELEMENT_TAG = "tag";
    /** Feishu元素：markdown */
    public static final String FEISHU_ELEMENT_MARKDOWN = "markdown";
    /** Feishu元素：plain_text */
    public static final String FEISHU_ELEMENT_PLAIN_TEXT = "plain_text";
    /** Feishu卡片template */
    public static final String FEISHU_CARD_TEMPLATE_BLUE = "blue";
    /** Feishu元素margin */
    public static final String FEISHU_ELEMENT_MARGIN = "margin";

    // ============================
    // 企业微信消息类型常量
    // ============================

    /** 企业微信消息类型：markdown */
    public static final String WECOM_MSG_TYPE_MARKDOWN = "markdown";
    /** 企业微信消息类型：text */
    public static final String WECOM_MSG_TYPE_TEXT = "text";
    /** 企业微信字段：mentioned_list */
    public static final String WECOM_MENTIONED_LIST = "mentioned_list";
    /** 企业微信字段：@all */
    public static final String WECOM_MENTION_ALL = "@all";

    // ============================
    // HTML字符串常量
    // ============================

    /** HTML：服务器运行消息 */
    public static final String HTML_SERVER_RUNNING = "<h2>AI Review Server is running.</h2>";
    /** HTML：前端加载错误消息 */
    public static final String HTML_ERROR_LOADING = "<h1>Error loading frontend</h1>";

    // ============================
    // 日期时间格式常量
    // ============================

    /** 默认日期时间格式 */
    public static final String DATE_FORMAT_DEFAULT = "yyyy-MM-dd HH:mm:ss";

    // ============================
    // 正则表达式常量
    // ============================

    /** 正则：diff --git格式 */
    public static final String REGEX_DIFF_GIT_PREFIX = "diff --git a/(.+) b/(.+)$";
    /** 正则：匹配任意字符 + diff前缀 */
    public static final String REGEX_PATTERN_ALL_NEWLINE = ".+\\+\\+\\+ b/(.+)$";
    /** 正则：匹配任意字符 + diff前缀变体 */
    public static final String REGEX_PATTERN_NEWLINE_2 = ".+\\+\\+\\+ (.+)$";
    /** 正则：匹配任意字符 + ---前缀 */
    public static final String REGEX_PATTERN_ALL_NEWLINE_2 = ".+--- a/(.+)$";
    /** 正则：匹配任意字符 + ---前缀变体 */
    public static final String REGEX_PATTERN_NEWLINE_3 = ".+--- (.+)$";

    // ============================
    // 编程语言特征检测常量
    // ============================

    // Vue3特征
    public static final String VUE3_SETUP = "setup()";
    public static final String VUE3_DEFINEPROPS = "defineprops";
    public static final String VUE3_DEFINEEMITS = "defineemits";
    public static final String VUE3_REF = "ref(";
    public static final String VUE3_REACTIVE = "reactive(";
    public static final String VUE3_COMPUTED = "computed(";
    public static final String VUE3_WATCH = "watch(";
    public static final String VUE3_ONMOUNTED = "onmounted";
    public static final String VUE4_ONUNMOUNTED = "onunmounted";
    public static final String VUE4_SCRIPT_SETUP = "script setup";
    public static final String VUE4_SCRIPT_SETUP_2 = "<script setup";

    // JavaScript特征
    public static final String JS_FUNCTION = "function";
    public static final String JS_VAR = "var ";
    public static final String JS_LET = "let ";
    public static final String JS_CONST = "const ";
    public static final String JS_CONSOLE_LOG = "console.log";
    public static final String JS_DOCUMENT = "document.";

    // Python特征
    public static final String PY_DEF = "def ";
    public static final String PY_IMPORT = "import ";
    public static final String PY_FROM = "from ";
    public static final String PY_CLASS = "class ";
    public static final String PY_IF_MAIN = "if __name__";
    public static final String PY_SELF = "self.";

    // 正则：分数提取
    public static final String REGEX_SCORE_EXTRACT = "总分[:：]\\s*(\\d+)分?";

    // ============================
    // LLM提示词常量
    // ============================

    /** 日报生成提示词 */
    public static final String LLM_DAILY_REPORT_PROMPT = "下面是以json格式记录员工代码提交信息。请总结这些信息，生成每个员工的工作日报摘要。员工姓名直接用json内容中的author属性值，不要进行转换。特别要求:以Markdown格式返回。\n";

    // ============================
    // Feishu消息配置常量
    // ============================

    /** Feishu字段：default */
    public static final String FEISHU_FIELD_DEFAULT = "default";
    /** Feishu字段：direction */
    public static final String FEISHU_FIELD_DIRECTION = "direction";
    /** Feishu字段：vertical */
    public static final String FEISHU_FIELD_VERTICAL = "vertical";
    /** Feishu字段：text_align */
    public static final String FEISHU_FIELD_TEXT_ALIGN = "text_align";
    /** Feishu字段：left */
    public static final String FEISHU_FIELD_LEFT = "left";
    /** Feishu字段：margin */
    public static final String FEISHU_FIELD_MARGIN = "margin";
    /** Feishu字段：padding值 */
    public static final String FEISHU_FIELD_PADDING_VALUE = "12px 12px 12px 12px";
    /** Feishu字段：0px值 */
    public static final String FEISHU_FIELD_MARGIN_ZERO = "0px 0px 0px 0px";
    /** Feishu字段：0px值（另一个） */
    public static final String FEISHU_FIELD_PADDING_ZERO = "0px 0px 0px 0px";
    /** Feishu字段：text */
    public static final String FEISHU_FIELD_TEXT = "text";
    /** Feishu字段：template */
    public static final String FEISHU_FIELD_TEMPLATE = "template";
    /** Feishu字段：blue */
    public static final String FEISHU_FIELD_BLUE = "blue";
    /** Feishu字段：pc */
    public static final String FEISHU_FIELD_PC = "pc";
    /** Feishu字段：mobile */
    public static final String FEISHU_FIELD_MOBILE = "mobile";

    // ============================
    // 额外Webhook字段常量
    // ============================

    /** 额外Webhook字段：ai_codereview_data */
    public static final String EXTRA_WEBHOOK_FIELD_AI_CODE_REVIEW_DATA = "ai_codereview_data";
    /** 额外Webhook字段：webhook_data */
    public static final String EXTRA_WEBHOOK_FIELD_WEBHOOK_DATA = "webhook_data";

    // ============================
    // Webhook响应消息常量
    // ============================

    /** Webhook响应：Gitea push事件已接收 */
    public static final String WEBHOOK_MSG_GITEA_PUSH_RECEIVED = "Gitea push event received, processing asynchronously";
    /** Webhook响应：Gitea PR action已忽略 */
    public static final String WEBHOOK_MSG_GITEA_PR_ACTION_IGNORED = "Gitea PR action '%s' ignored";
    /** Webhook响应：Gitea PR事件已接收 */
    public static final String WEBHOOK_MSG_GITEA_PR_RECEIVED = "Gitea PR event received, processing asynchronously";
    /** Webhook响应：Gitea issue_comment事件已忽略 */
    public static final String WEBHOOK_MSG_GITEA_COMMENT_IGNORED = "Gitea issue_comment event ignored";
    /** Webhook响应：不支持的Gitea事件 */
    public static final String WEBHOOK_MSG_UNSUPPORTED_GITEA_EVENT = "Unsupported Gitea event: %s";
    /** Webhook响应：GitHub PR事件已接收 */
    public static final String WEBHOOK_MSG_GITHUB_PR_RECEIVED = "GitHub PR event received, processing asynchronously";
    /** Webhook响应：GitHub push事件已接收 */
    public static final String WEBHOOK_MSG_GITHUB_PUSH_RECEIVED = "GitHub push event received, processing asynchronously";
    /** Webhook响应：不支持的GitHub事件 */
    public static final String WEBHOOK_MSG_UNSUPPORTED_GITHUB_EVENT = "Unsupported GitHub event: %s";
    /** Webhook响应：GitLab MR事件已接收 */
    public static final String WEBHOOK_MSG_GITLAB_MR_RECEIVED = "GitLab MR event received, processing asynchronously";
    /** Webhook响应：GitLab push事件已接收 */
    public static final String WEBHOOK_MSG_GITLAB_PUSH_RECEIVED = "GitLab push event received, processing asynchronously";
    /** Webhook响应：不支持的GitLab事件 */
    public static final String WEBHOOK_MSG_UNSUPPORTED_GITLAB_EVENT = "Unsupported GitLab event: %s";

    // ============================
    // Webhook错误消息常量
    // ============================

    /** Webhook错误：缺少Gitea token */
    public static final String WEBHOOK_ERR_MISSING_GITEA_TOKEN = "Missing Gitea access token";
    /** Webhook错误：解析Gitea URL失败 */
    public static final String WEBHOOK_ERR_PARSE_GITEA_URL_FAILED = "Failed to parse Gitea URL";
    /** Webhook错误：缺少Gitea URL */
    public static final String WEBHOOK_ERR_MISSING_GITEA_URL = "Missing Gitea URL";
    /** Webhook错误：缺少GitHub token */
    public static final String WEBHOOK_ERR_MISSING_GITHUB_TOKEN = "Missing GitHub access token";
    /** Webhook错误：解析homepage URL失败 */
    public static final String WEBHOOK_ERR_PARSE_HOMEPAGE_URL_FAILED = "Failed to parse homepage URL: %s";
    /** Webhook错误：缺少GitLab token */
    public static final String WEBHOOK_ERR_MISSING_GITLAB_TOKEN = "Missing GitLab access token";
    /** Webhook错误：缺少GitLab URL */
    public static final String WEBHOOK_ERR_MISSING_GITLAB_URL = "Missing GitLab URL";

    // ============================
    // LLM API字段常量
    // ============================

    /** LLM API：model */
    public static final String LLM_FIELD_MODEL = "model";
    /** LLM API：messages */
    public static final String LLM_FIELD_MESSAGES = "messages";
    /** LLM API：temperature */
    public static final String LLM_FIELD_TEMPERATURE = "temperature";
    /** LLM API：choices */
    public static final String LLM_PATH_CHOICES = "choices";
    /** LLM API：message */
    public static final String LLM_PATH_MESSAGE = "message";
    /** LLM API：content */
    public static final String LLM_PATH_CONTENT = "content";

    // ============================
    // 线程配置常量
    // ============================

    /** 异步执行器线程名前缀 */
    public static final String THREAD_NAME_PREFIX = "ai-review-";

    // ============================
    // 静态资源配置常量
    // ============================

    /** 静态资源目录 */
    public static final String STATIC_RESOURCE_LOCATION = "classpath:/static/";

    // ============================
    // Gitea API字段常量
    // ============================

    /** Gitea JSON字段：body */
    public static final String GITEA_JSON_FIELD_BODY = "body";
    /** Gitea JSON字段：message */
    public static final String GITEA_JSON_FIELD_MESSAGE = "message";
    /** Gitea JSON字段：title */
    public static final String GITEA_JSON_FIELD_TITLE = "title";

    // ============================
    // GitLab API字段常量
    // ============================

    /** GitLab JSON字段：body */
    public static final String GITLAB_JSON_FIELD_BODY = "body";
    /** GitLab JSON字段：note */
    public static final String GITLAB_JSON_FIELD_NOTE = "note";
}

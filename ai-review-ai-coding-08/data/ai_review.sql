SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for mr_review_log
-- ----------------------------
DROP TABLE IF EXISTS `mr_review_log`;
CREATE TABLE `mr_review_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '项目名称',
  `author` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '提交者',
  `source_branch` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '源分支',
  `target_branch` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '目标分支',
  `updated_at` bigint(20) NULL DEFAULT NULL COMMENT '更新时间（Unix 时间戳，秒）',
  `commit_messages` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '提交信息',
  `score` int(11) NULL DEFAULT 0 COMMENT 'AI 审查评分',
  `url` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'MR/PR 链接',
  `review_result` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'AI 审查结果',
  `additions` int(11) NULL DEFAULT 0 COMMENT '新增代码行数',
  `deletions` int(11) NULL DEFAULT 0 COMMENT '删除代码行数',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_mr_author`(`author`) USING BTREE,
  INDEX `idx_mr_project_name`(`project_name`) USING BTREE,
  INDEX `idx_mr_updated_at`(`updated_at`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'MR/PR 代码审查记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for push_review_log
-- ----------------------------
DROP TABLE IF EXISTS `push_review_log`;
CREATE TABLE `push_review_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '项目名称',
  `author` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '提交者',
  `branch` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '推送分支',
  `updated_at` bigint(20) NULL DEFAULT NULL COMMENT '更新时间（Unix 时间戳，秒）',
  `commit_messages` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '提交信息',
  `score` int(11) NULL DEFAULT 0 COMMENT 'AI 审查评分',
  `url` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'Push 链接',
  `review_result` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'AI 审查结果',
  `additions` int(11) NULL DEFAULT 0 COMMENT '新增代码行数',
  `deletions` int(11) NULL DEFAULT 0 COMMENT '删除代码行数',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_push_author`(`author`) USING BTREE,
  INDEX `idx_push_project_name`(`project_name`) USING BTREE,
  INDEX `idx_push_updated_at`(`updated_at`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Push 代码审查记录' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;

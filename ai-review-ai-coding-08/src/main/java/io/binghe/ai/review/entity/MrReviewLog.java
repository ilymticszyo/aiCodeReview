package io.binghe.ai.review.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author binghe(微信 : hacker_binghe)
 * @version 1.0.0
 * @description MR CR记录
 * @github https://github.com/binghe001
 * @copyright 公众号: 冰河技术
 */
@Data
@Entity
@Table(name = "mr_review_log")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MrReviewLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_name", length = 255)
    private String projectName;

    @Column(name = "author", length = 255)
    private String author;

    @Column(name = "source_branch", length = 255)
    private String sourceBranch;

    @Column(name = "target_branch", length = 255)
    private String targetBranch;

    @Column(name = "updated_at")
    private Long updatedAt;

    @Column(name = "commit_messages", columnDefinition = "TEXT")
    private String commitMessages;

    @Column(name = "score")
    private Integer score;

    @Column(name = "url", columnDefinition = "TEXT")
    private String url;

    @Column(name = "review_result", columnDefinition = "TEXT")
    private String reviewResult;

    @Column(name = "additions")
    @Builder.Default
    private Integer additions = 0;

    @Column(name = "deletions")
    @Builder.Default
    private Integer deletions = 0;
}

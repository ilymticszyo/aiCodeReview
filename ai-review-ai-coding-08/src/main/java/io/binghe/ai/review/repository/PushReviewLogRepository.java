package io.binghe.ai.review.repository;

import io.binghe.ai.review.entity.PushReviewLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
/**
 * @author binghe(微信 : hacker_binghe)
 * @version 1.0.0
 * @description Push CR记录
 * @github https://github.com/binghe001
 * @copyright 公众号: 冰河技术
 */
@Repository
public interface PushReviewLogRepository extends JpaRepository<PushReviewLog, Long> {

    @Query("SELECT p FROM PushReviewLog p WHERE " +
           "(:authors IS NULL OR p.author IN :authors) AND " +
           "(:projectNames IS NULL OR p.projectName IN :projectNames) AND " +
           "(:updatedAtGte IS NULL OR p.updatedAt >= :updatedAtGte) AND " +
           "(:updatedAtLte IS NULL OR p.updatedAt <= :updatedAtLte) " +
           "ORDER BY p.updatedAt DESC")
    List<PushReviewLog> findByFilters(
            @Param("authors") List<String> authors,
            @Param("projectNames") List<String> projectNames,
            @Param("updatedAtGte") Long updatedAtGte,
            @Param("updatedAtLte") Long updatedAtLte
    );

    @Query("SELECT DISTINCT p.author FROM PushReviewLog p ORDER BY p.author")
    List<String> findDistinctAuthors();

    @Query("SELECT DISTINCT p.projectName FROM PushReviewLog p ORDER BY p.projectName")
    List<String> findDistinctProjectNames();
}

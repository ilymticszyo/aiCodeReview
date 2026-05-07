package io.binghe.ai.review.service;

import io.binghe.ai.review.entity.MrReviewLog;
import io.binghe.ai.review.entity.PushReviewLog;

import java.util.List;

/**
 * @author binghe(微信 : hacker_binghe)
 * @version 1.0.0
 * @description ReviewService
 * @github https://github.com/binghe001
 * @copyright 公众号: 冰河技术
 */
public interface ReviewService {

    void insertMrReviewLog(MrReviewLog entity);

    void insertPushReviewLog(PushReviewLog entity);

    List<MrReviewLog> getMrReviewLogs(List<String> authors, List<String> projectNames,
                                      Long updatedAtGte, Long updatedAtLte);

    List<PushReviewLog> getPushReviewLogs(List<String> authors, List<String> projectNames,
                                          Long updatedAtGte, Long updatedAtLte);

    List<String> getMrDistinctAuthors();

    List<String> getMrDistinctProjectNames();

    List<String> getPushDistinctAuthors();

    List<String> getPushDistinctProjectNames();
}

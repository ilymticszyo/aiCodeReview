package io.binghe.ai.review.service.impl;

import io.binghe.ai.review.entity.MrReviewLog;
import io.binghe.ai.review.entity.PushReviewLog;
import io.binghe.ai.review.repository.MrReviewLogRepository;
import io.binghe.ai.review.repository.PushReviewLogRepository;
import io.binghe.ai.review.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author binghe(微信 : hacker_binghe)
 * @version 1.0.0
 * @description ReviewServiceImpl
 * @github https://github.com/binghe001
 * @copyright 公众号: 冰河技术
 */
@Slf4j
@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private MrReviewLogRepository mrReviewLogRepository;

    @Autowired
    private PushReviewLogRepository pushReviewLogRepository;

    @Override
    public void insertMrReviewLog(MrReviewLog entity) {
        try {
            mrReviewLogRepository.save(entity);
            log.info("MR review log saved: {} by {}", entity.getProjectName(), entity.getAuthor());
        } catch (Exception e) {
            log.error("Error saving MR review log: {}", e.getMessage(), e);
        }
    }

    @Override
    public void insertPushReviewLog(PushReviewLog entity) {
        try {
            pushReviewLogRepository.save(entity);
            log.info("Push review log saved: {} by {}", entity.getProjectName(), entity.getAuthor());
        } catch (Exception e) {
            log.error("Error saving push review log: {}", e.getMessage(), e);
        }
    }

    @Override
    public List<MrReviewLog> getMrReviewLogs(List<String> authors, List<String> projectNames,
                                             Long updatedAtGte, Long updatedAtLte) {
        return mrReviewLogRepository.findByFilters(
                authors != null && authors.isEmpty() ? null : authors,
                projectNames != null && projectNames.isEmpty() ? null : projectNames,
                updatedAtGte, updatedAtLte
        );
    }

    @Override
    public List<PushReviewLog> getPushReviewLogs(List<String> authors, List<String> projectNames,
                                                 Long updatedAtGte, Long updatedAtLte) {
        return pushReviewLogRepository.findByFilters(
                authors != null && authors.isEmpty() ? null : authors,
                projectNames != null && projectNames.isEmpty() ? null : projectNames,
                updatedAtGte, updatedAtLte
        );
    }

    @Override
    public List<String> getMrDistinctAuthors() {
        return mrReviewLogRepository.findDistinctAuthors();
    }

    @Override
    public List<String> getMrDistinctProjectNames() {
        return mrReviewLogRepository.findDistinctProjectNames();
    }

    @Override
    public List<String> getPushDistinctAuthors() {
        return pushReviewLogRepository.findDistinctAuthors();
    }

    @Override
    public List<String> getPushDistinctProjectNames() {
        return pushReviewLogRepository.findDistinctProjectNames();
    }
}

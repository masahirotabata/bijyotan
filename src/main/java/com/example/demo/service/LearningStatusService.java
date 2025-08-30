package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.domain.entity.LearningStatus;
import com.example.demo.domain.repository.LearningStatusRepository;

@Service
public class LearningStatusService {

    @Autowired
    private LearningStatusRepository learningStatusRepository;

    public void updateStatus(Long userId, Long wordId, String part, boolean learned) {
        LearningStatus status = learningStatusRepository
            .findByUserIdAndWordIdAndPart(userId, wordId, part)
            .orElseGet(() -> {
                LearningStatus s = new LearningStatus();
                s.setUserId(userId);
                s.setWordId(wordId);
                s.setPart(part);
                return s;
            });

        status.setLearned(learned); // 更新されたフラグだけ変更
        learningStatusRepository.save(status);
    }



    
    public int getLearningStatusCount(Long userId, String part) {
        return learningStatusRepository.countByUserIdAndPartAndLearnedTrue(userId, part);
    }



}

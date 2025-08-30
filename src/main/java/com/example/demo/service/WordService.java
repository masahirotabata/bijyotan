package com.example.demo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.api.dto.WordDto;
import com.example.demo.domain.entity.LearningStatus;
import com.example.demo.domain.entity.Word;
import com.example.demo.domain.repository.LearningStatusRepository;
import com.example.demo.domain.repository.WordRepository;

@Service
public class WordService {

    @Autowired
    private WordRepository wordRepository;
    
    @Autowired
    private LearningStatusRepository learningStatusRepository;

    // ✅ ユーザーIDとパートで単語リストを取得 → DTOに変換
    public List<WordDto> getWordsForUser(Long userId, String part) {
        List<Word> wordList = wordRepository.findByUserIdInAndPartIgnoreCase(List.of(0L, userId), part);
        return getWordDtos(wordList, userId, part);
    }

    // ✅ Word → WordDto に変換
    public List<WordDto> getWordDtos(List<Word> wordList, Long userId, String part) {
        return wordList.stream().map(word -> {
            boolean status = learningStatusRepository
                .findByUserIdAndWordIdAndPart(userId, word.getId(), part)
                .map(LearningStatus::isLearned)
                .orElse(false);

            return new WordDto(word, status);
        }).collect(Collectors.toList());
    }

    public List<Word> findWordsForUserId(Long userId) {
        return wordRepository.findByUserIdIn(List.of(0L, userId));
    }

    public void resetStatus() {
        wordRepository.resetStatus();
    }

    public Word findById(Long id) {
        return wordRepository.findById(id).orElse(null);
    }

    public Word saveWord(Word word) {
        return wordRepository.save(word);	
    }

    public void deleteById(Long id) {
        wordRepository.deleteById(id);
    }

    public List<Word> findWordsForUserIdAndPart(Long userId, String part) {
        return wordRepository.findByUserIdInAndPartIgnoreCase(List.of(0L, userId), part);
    }

    public List<Word> findAllWords() {
        return wordRepository.findAll();
    }

    public boolean updateStatusWithUserCheck(Long id, Long userId, boolean status) {
        Word word = wordRepository.findById(id).orElse(null);
        if (word != null && (word.getUserId().equals(userId) || word.getUserId() == 0L)) {
            word.setStatus(status);
            wordRepository.save(word);
            return true;
        }
        return false;
    }

    public boolean updateStatusOk(Long id) {
        Word word = wordRepository.findById(id).orElse(null);
        if (word != null) {
            word.setStatus(true);
            wordRepository.save(word);
            return true;
        }
        return false;
    }

    public boolean deleteWord(Long id) {
        if (wordRepository.existsById(id)) {
            wordRepository.deleteById(id);
            return true;
        }
        return false;
    }

}

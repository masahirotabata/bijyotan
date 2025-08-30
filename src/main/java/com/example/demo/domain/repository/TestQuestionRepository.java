package com.example.demo.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.domain.entity.TestQuestion;

public interface TestQuestionRepository extends JpaRepository<TestQuestion, Long> {

    // エンティティに Long userId フィールドがある場合
    @Query("SELECT q FROM TestQuestion q WHERE q.userId = :userId")
    List<TestQuestion> findByUserId(@Param("userId") Long userId);

    // もし TestQuestion に ManyToOne User user; の形で関連だけがあり、
    // user の主キーを見たい場合は上ではなくこちらを使う
    // @Query("SELECT q FROM TestQuestion q WHERE q.user.id = :userId")
    // List<TestQuestion> findByUserId(@Param("userId") Long userId);
}

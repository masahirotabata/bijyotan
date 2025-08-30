package com.example.demo.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.domain.entity.Word;

@Repository
public interface WordRepository extends JpaRepository<Word, Long> {

    // ▼ ステータス更新（期日を過ぎたものを false へ）
    @Modifying
    @Transactional
    @Query("""
        UPDATE Word w
           SET w.status = false
         WHERE w.status = true
           AND w.nextPresentation <= CURRENT_TIMESTAMP
    """)
    void resetStatus();

    // ▼ よく使う検索
    List<Word> findByUserId(Long userId);

    // Part（品詞など）を大文字小文字無視で検索
    List<Word> findByUserIdAndPartIgnoreCase(Long userId, String part);

    // 共通＆自分の単語（例：userIds = [0L, 自分]）
    List<Word> findByUserIdIn(List<Long> userIds);

    // 共通＆自分の単語＋part一致（ignore case）
    List<Word> findByUserIdInAndPartIgnoreCase(List<Long> userIds, String part);

    // “ユーザー個別 or 共通(null)” の取得が必要な場合
    List<Word> findByUserIdOrUserIdIsNull(Long userId);

    List<Word> findByUserIdAndPartIgnoreCaseOrUserIdIsNull(Long userId, String part);

    // 軽量プロジェクション
    interface WordLite {
        String getWord();
        String getMeaning();
    }

    // H2: ランダム取得（列名は実テーブルに合わせる）
    @Query(value = """
        SELECT WORD AS word, MEANING AS meaning
          FROM WORD
         ORDER BY RAND()
         LIMIT ?1
    """, nativeQuery = true)
    List<WordLite> findRandomLiteNative(int limit);

    // Controller 側の期待に合わせて Optional で返す
    Optional<Word> findByWordIgnoreCase(String word);

    // 必要なら非Ignore版も残す
    List<Word> findByUserIdAndPart(Long userId, String part);
    
 // 全体からランダムN件
//    @Query(value = "SELECT * FROM word ORDER BY RAND() LIMIT ?1", nativeQuery = true)
//    List<Word> pickRandom(int limit);
//
//    // 指定ID群を除き、同一partからランダムM件（ダミー用）
//    @Query(value = "SELECT * FROM word WHERE part = ?1 AND id NOT IN (?2) ORDER BY RAND() LIMIT ?3", nativeQuery = true)
//    List<Word> pickRandomByPartExcluding(String part, List<Long> excludeIds, int limit);
//
//    // adverb だけからランダムN件（副詞作問用の土台）
//    @Query(value = "SELECT * FROM word WHERE part = 'adverb' ORDER BY RAND() LIMIT ?1", nativeQuery = true)
//    List<Word> pickRandomAdverbs(int limit);
    
    // プール用に十分な件数を持ってくる。サイズは調整可
    List<Word> findTop2000ByOrderByIdAsc();
    List<Word> findTop1000ByPartIgnoreCaseOrderByIdAsc(String part);
}

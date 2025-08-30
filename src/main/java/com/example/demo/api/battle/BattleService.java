// src/main/java/com/example/demo/api/battle/BattleService.java
package com.example.demo.api.battle;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.example.demo.domain.repository.WordRepository;

@Service
public class BattleService {

  private final WordRepository wordRepository;
  public BattleService(WordRepository wordRepository){ this.wordRepository = wordRepository; }

  // バトル状態をメモリに保持
  private final Map<Long, BattleState> battles = new ConcurrentHashMap<>();
  private final AtomicLong seq = new AtomicLong(1);

  private static final int PLAYER_HP = 5;
  private static final Map<Difficulty, Integer> ENEMY_HP = Map.of(
      Difficulty.EASY, 10,
      Difficulty.NORMAL, 20,
      Difficulty.HARD, 30,
      Difficulty.BOSS, 50
  );

  // ---- API本体 -------------------------------------------------------------

  public StartBattleResponse start(StartBattleRequest req) {
    long id = seq.getAndIncrement();

    BattleState st = new BattleState();
    st.battleId = id;
    st.userId   = Optional.ofNullable(req.getUserId()).orElse(0L);
    st.mode     = req.getMode();
    st.diff     = req.getDifficulty();
    st.enemyHp  = ENEMY_HP.getOrDefault(st.diff, 10);
    st.playerHp = PLAYER_HP;
    st.queue    = generateQuestions(st.mode);
    st.current  = st.queue.poll();

    battles.put(id, st);

    return StartBattleResponse.builder()
        .battleId(id)
        .enemyHp(st.enemyHp)
        .playerHp(st.playerHp)
        .question(st.current)
        .build();
  }

  public NextQuestionResponse next(long battleId) {
    BattleState st = get(battleId);
    if (st.finished) return NextQuestionResponse.finished(st.toPublic());

    QuestionDto q = st.queue.poll();
    if (q == null) {
      st.queue = generateQuestions(st.mode);
      q = st.queue.poll();
    }
    st.current = q;

    return NextQuestionResponse.builder()
        .battleId(battleId)
        .enemyHp(st.enemyHp)
        .playerHp(st.playerHp)
        .question(q)
        .finished(false)
        .build();
  }

  public AnswerResponse answer(AnswerRequest req) {
    BattleState st = get(req.getBattleId());
    QuestionDto cur = st.current;
    boolean correct = cur != null && Objects.equals(cur.getAnswerChoiceId(), req.getChoiceId());

    if (correct) st.enemyHp--; else st.playerHp--;

    boolean win = st.enemyHp <= 0;
    boolean lose = st.playerHp <= 0;
    boolean finished = win || lose;

    QuestionDto nextQ = null;
    if (!finished) {
      nextQ = st.queue.poll();
      if (nextQ == null) {
        st.queue = generateQuestions(st.mode);
        nextQ = st.queue.poll();
      }
      st.current = nextQ;
    } else {
      st.finished = true;
    }

    RewardDto reward = finished ? calcReward(st, win) : null;

    return AnswerResponse.builder()
        .battleId(st.battleId)
        .correct(correct)
        .enemyHp(st.enemyHp)
        .playerHp(st.playerHp)
        .finished(finished)
        .win(win)
        .nextQuestion(nextQ)
        .reward(reward)
        .build();
  }

  // ---- 内部処理 -----------------------------------------------------------

  private RewardDto calcReward(BattleState st, boolean win) {
    int exp = 0, coin = 0;
    if (win) {
      switch (st.diff) {
        case EASY   -> { exp = 100;  coin = 50; }
        case NORMAL -> { exp = 250;  coin = 100; }
        case HARD   -> { exp = 500;  coin = 200; }
        case BOSS   -> { exp = 1000; coin = 500; }
      }
    } else {
      exp = 20; coin = 10;
    }
    int level = exp / 100;
    return new RewardDto(exp, coin, level);
  }

  private BattleState get(long id) {
    BattleState st = battles.get(id);
    if (st == null) throw new IllegalArgumentException("battle not found: " + id);
    return st;
  }

  // DBから10問作成
  private ArrayDeque<QuestionDto> generateQuestions(Mode mode) {
    List<WordRepository.WordLite> pool = wordRepository.findRandomLiteNative(20);
    if (pool == null || pool.isEmpty()) throw new IllegalStateException("WORD テーブルにデータがありません");

    ArrayDeque<QuestionDto> q = new ArrayDeque<>();
    Random r = new Random();
    int count = Math.min(10, pool.size());

    for (int i = 0; i < count; i++) {
      WordRepository.WordLite correct = pool.get(r.nextInt(pool.size()));

      // 4択（重複なし）
      Set<WordRepository.WordLite> picks = new LinkedHashSet<>();
      picks.add(correct);
      while (picks.size() < 4) picks.add(pool.get(r.nextInt(pool.size())));

      // 選択肢
      List<ChoiceDto> choices = new ArrayList<>();
      long cid = 1;
      for (var w : picks) choices.add(new ChoiceDto(cid++, w.getMeaning()));
      Collections.shuffle(choices, r);

      long answerId = choices.stream()
          .filter(c -> Objects.equals(c.getLabel(), correct.getMeaning()))
          .findFirst().map(ChoiceDto::getId).orElse(1L);

      QuestionDto dto = QuestionDto.builder()
          .questionId(Math.abs(r.nextLong()))
          .mode(mode)
          .prompt("\"" + correct.getWord() + "\" の意味は？")
          .ttsUrl(null)
          .choices(choices)
          .answerChoiceId(answerId)
          .build();

      q.add(dto);
    }
    return q;
  }

  // ---- メモリ上の状態クラス ----------------------------------------------
  private static class BattleState {
    long battleId;
    long userId;
    Mode mode;
    Difficulty diff;
    int enemyHp;
    int playerHp;
    ArrayDeque<QuestionDto> queue;
    QuestionDto current;
    boolean finished;

    NextQuestionResponse toPublic() {
      return NextQuestionResponse.builder()
          .battleId(battleId)
          .enemyHp(enemyHp)
          .playerHp(playerHp)
          .question(current)
          .finished(finished)
          .build();
    }
  }
}

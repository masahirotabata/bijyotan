// src/main/java/com/example/demo/pvp/core/PvpGameService.java
package com.example.demo.pvp.core;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import com.example.demo.pvp.dto.PvpDtos.Choice;
import com.example.demo.pvp.dto.PvpDtos.MatchEnd;
import com.example.demo.pvp.dto.PvpDtos.MatchReady;
import com.example.demo.pvp.dto.PvpDtos.Question;
import com.example.demo.pvp.dto.PvpDtos.RoundResult;
import com.example.demo.pvp.dto.PvpDtos.RoundStart;

@Service
public class PvpGameService {
  private final SimpMessagingTemplate broker;
  private final TaskScheduler scheduler;

  public PvpGameService(SimpMessagingTemplate broker, TaskScheduler scheduler) {
    this.broker = broker;
    this.scheduler = scheduler;
  }

  // ---- マッチ・状態（最小）
  static class Match {
    long id; String p1, p2;              // userId 文字列化でOK（まずはセッションIDでも可）
    int p1Hp=10, p2Hp=10;
    AtomicInteger seq = new AtomicInteger(0);
    Map<Integer, Ans> p1Ans = new ConcurrentHashMap<>();
    Map<Integer, Ans> p2Ans = new ConcurrentHashMap<>();
    volatile boolean done = false;
  }
  record Ans(long choiceId, int ms, boolean correct) {}

  private final Map<Long, Match> matches = new ConcurrentHashMap<>();
  private final Queue<String> queue = new LinkedBlockingQueue<>();
  private final Random rnd = new Random();
  private long nextId() { return Math.abs(rnd.nextLong()); }

  // ---- 外部公開
//src/main/java/com/example/demo/pvp/core/PvpGameService.java
//戻り値を「作れたらID(Long)・待機ならnull」に変更
public synchronized Long enqueue(String userId) {
   String peer = queue.poll();
   if (peer == null) { queue.offer(userId); return null; }

   Match m = new Match();
   m.id = nextId(); m.p1 = peer; m.p2 = userId;
   matches.put(m.id, m);

   broker.convertAndSend("/topic/pvp/match."+m.id, new MatchReady(m.id, "P1", m.p1Hp, m.p2Hp));
   broker.convertAndSendToUser(m.p2, "/queue/self",
       new MatchReady(m.id, "P2", m.p2Hp, m.p1Hp));

   startNextRound(m);
   return m.id; // ★ IDだけ返す
}

  public void submitAnswer(long matchId, String userId, int seq, long choiceId) {
    Match m = matches.get(matchId);
    if (m==null || m.done) return;
    if (seq != m.seq.get()) return; // 古い/不正

    var q = sampleQuestion(seq); // 今の実装では答えは固定的に分かる

    // 擬似：choiceId==answerで正解
    boolean correct = (choiceId == q.answerChoiceId());
    int ms = (int)(System.currentTimeMillis() - (deadlineMs(seq) - 10_000));

    if (Objects.equals(userId, m.p1)) m.p1Ans.put(seq, new Ans(choiceId, ms, correct));
    else if (Objects.equals(userId, m.p2)) m.p2Ans.put(seq, new Ans(choiceId, ms, correct));
  }

  // ---- ラウンド制御
  private void startNextRound(Match m){
    if (m.done) return;

    int seq = m.seq.incrementAndGet();
    var q = sampleQuestion(seq);
    long deadline = deadlineMs(seq);

    broker.convertAndSend("/topic/pvp/match."+m.id,
      new RoundStart(seq, q, deadline));

    // 10秒後に自動解決
    scheduler.schedule(() -> resolveRound(m, seq, q), Date.from(Instant.ofEpochMilli(deadline)));
  }

  private void resolveRound(Match m, int seq, Question q){
    if (m.done || seq != m.seq.get()) return;

    var a1 = m.p1Ans.remove(seq);
    var a2 = m.p2Ans.remove(seq);

    boolean c1 = a1 != null && a1.correct();
    boolean c2 = a2 != null && a2.correct();

    if (c1) m.p2Hp--;
    if (c2) m.p1Hp--;

    var res = new RoundResult(
      seq,
      new RoundResult.Side(c1, a1==null?null:a1.ms()),
      new RoundResult.Side(c2, a2==null?null:a2.ms()),
      m.p1Hp, m.p2Hp
    );
    broker.convertAndSend("/topic/pvp/match."+m.id, res);

    if (m.p1Hp<=0 || m.p2Hp<=0) {
      m.done = true;
      String winner = m.p1Hp<=0 && m.p2Hp<=0 ? "DRAW" : (m.p1Hp>0 ? "P1":"P2");
      broker.convertAndSend("/topic/pvp/match."+m.id, new MatchEnd(m.id, winner));
      return;
    }
    startNextRound(m);
  }

  private long deadlineMs(int seq){ return System.currentTimeMillis() + 10_000; }

  // ---- ダミー出題（単語プール連携は後で）
  private Question sampleQuestion(int seq){
    long qid = seq;
    var choices = List.of(
      new Choice(1,"apple"), new Choice(2,"orange"),
      new Choice(3,"grape"), new Choice(4,"banana"));
    long answer = 2; // ここでは固定：2が正解
    return new Question(qid, "“〇〇”の意味は？", choices, answer);
  }
}

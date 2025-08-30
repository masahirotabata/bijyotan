// src/main/java/com/example/demo/pvp/dto/PvpDtos.java
package com.example.demo.pvp.dto;

import java.util.List;

import com.example.demo.pvp.dto.PvpDtos.RoundResult.Side;

public class PvpDtos {
  // クライアント→サーバ
  public record Enqueue(String mode, String diff) {}
  public record Answer(long matchId, int seq, long choiceId) {}

  // サーバ→クライアント
  public record MatchReady(long matchId, String you, int yourHp, int enemyHp) {}
  public record RoundStart(int seq, Question question, long deadlineEpochMs) {}
  public record RoundResult(int seq, Side p1, Side p2, int p1Hp, int p2Hp) {
    public record Side(boolean correct, Integer ms) {}
  }
  public record MatchEnd(long matchId, String winner) {}

  // 出題
  public record Question(long id, String prompt, List<Choice> choices, long answerChoiceId) {}
  public record Choice(long id, String label) {}
}

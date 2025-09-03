package com.example.demo.api.battle;

import java.util.ArrayDeque;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
class StartBattleRequest {
    private Mode mode;
    private Difficulty difficulty;
    private Long userId;
}
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
class StartBattleResponse {
    private long battleId;
    private int enemyHp;
    private int playerHp;
    private QuestionDto question;
}

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
class NextQuestionResponse {
    private long battleId;
    private int enemyHp;
    private int playerHp;
    private boolean finished;
    private QuestionDto question;

    static NextQuestionResponse finished(NextQuestionResponse nextQuestionResponse){
        return NextQuestionResponse.builder()
                .battleId(nextQuestionResponse.getBattleId())
                .enemyHp(Math.max(nextQuestionResponse.getEnemyHp(),0))
                .playerHp(Math.max(nextQuestionResponse.getPlayerHp(),0))
                .finished(true)
                .question(null)
                .build();
    }
}

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
class AnswerRequest {
    private long battleId;
    private long questionId;
    private long choiceId;
}
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
class AnswerResponse {
    private long battleId;
    private boolean correct;
    private int enemyHp;
    private int playerHp;
    private boolean finished;
    private boolean win;
    private QuestionDto nextQuestion;
    private RewardDto reward;
}

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
class QuestionDto {
    private long questionId;
    private Mode mode;
    private String prompt;   // READING 用の出題文
    private String ttsUrl;   // LISTENING 用の音声URL（必要なら）
    private List<ChoiceDto> choices;
    private long answerChoiceId;
}

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
class ChoiceDto {
    private long id;
    private String label; // 表示テキスト（実際は美女画像でもOK）
}

record RewardDto(int exp, int coin, int level) {}
enum Mode { READING, LISTENING }
enum Difficulty { EASY, NORMAL, HARD, BOSS }

@Getter @Setter
class BattleState {
    private long battleId;
    private long userId;
    private Mode mode;
    private Difficulty difficulty;
    private int enemyHp;
    private int playerHp;
    private boolean finished;
    private ArrayDeque<QuestionDto> queue;
    private QuestionDto current;
}

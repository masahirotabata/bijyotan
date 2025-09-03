package com.example.demo.api.battle.dto;

import java.util.List;

public class Models {

  // ========== Request/Response ==========
  public static class StartBattleRequest {
    private String mode;
    private String difficulty;
    private Long userId;
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
  }

  public static class StartBattleResponse {
    private long battleId;
    private int enemyHp;
    private int playerHp;
    private QuestionDto question;
    public long getBattleId() { return battleId; }
    public void setBattleId(long battleId) { this.battleId = battleId; }
    public int getEnemyHp() { return enemyHp; }
    public void setEnemyHp(int enemyHp) { this.enemyHp = enemyHp; }
    public int getPlayerHp() { return playerHp; }
    public void setPlayerHp(int playerHp) { this.playerHp = playerHp; }
    public QuestionDto getQuestion() { return question; }
    public void setQuestion(QuestionDto question) { this.question = question; }
  }

  public static class NextQuestionResponse {
    private long battleId;
    private int enemyHp;
    private int playerHp;
    private boolean finished;
    private QuestionDto question;
    public long getBattleId() { return battleId; }
    public void setBattleId(long battleId) { this.battleId = battleId; }
    public int getEnemyHp() { return enemyHp; }
    public void setEnemyHp(int enemyHp) { this.enemyHp = enemyHp; }
    public int getPlayerHp() { return playerHp; }
    public void setPlayerHp(int playerHp) { this.playerHp = playerHp; }
    public boolean isFinished() { return finished; }
    public void setFinished(boolean finished) { this.finished = finished; }
    public QuestionDto getQuestion() { return question; }
    public void setQuestion(QuestionDto question) { this.question = question; }
  }

  public static class AnswerRequest {
    private long battleId;
    private long questionId;
    private long choiceId;
    public long getBattleId() { return battleId; }
    public void setBattleId(long battleId) { this.battleId = battleId; }
    public long getQuestionId() { return questionId; }
    public void setQuestionId(long questionId) { this.questionId = questionId; }
    public long getChoiceId() { return choiceId; }
    public void setChoiceId(long choiceId) { this.choiceId = choiceId; }
  }

  public static class AnswerResponse {
    private long battleId;
    private boolean correct;
    private int enemyHp;
    private int playerHp;
    private boolean finished;
    private boolean win;
    private QuestionDto nextQuestion;
    private RewardDto reward;
    public long getBattleId() { return battleId; }
    public void setBattleId(long battleId) { this.battleId = battleId; }
    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }
    public int getEnemyHp() { return enemyHp; }
    public void setEnemyHp(int enemyHp) { this.enemyHp = enemyHp; }
    public int getPlayerHp() { return playerHp; }
    public void setPlayerHp(int playerHp) { this.playerHp = playerHp; }
    public boolean isFinished() { return finished; }
    public void setFinished(boolean finished) { this.finished = finished; }
    public boolean isWin() { return win; }
    public void setWin(boolean win) { this.win = win; }
    public QuestionDto getNextQuestion() { return nextQuestion; }
    public void setNextQuestion(QuestionDto nextQuestion) { this.nextQuestion = nextQuestion; }
    public RewardDto getReward() { return reward; }
    public void setReward(RewardDto reward) { this.reward = reward; }
  }

  // ========== 問題 ==========
  public static class QuestionDto {
    private long questionId;
    private String mode;
    private String prompt;
    private String ttsUrl;
    private List<ChoiceDto> choices;
    private long answerChoiceId;
    public long getQuestionId() { return questionId; }
    public void setQuestionId(long questionId) { this.questionId = questionId; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    public String getTtsUrl() { return ttsUrl; }
    public void setTtsUrl(String ttsUrl) { this.ttsUrl = ttsUrl; }
    public List<ChoiceDto> getChoices() { return choices; }
    public void setChoices(List<ChoiceDto> choices) { this.choices = choices; }
    public long getAnswerChoiceId() { return answerChoiceId; }
    public void setAnswerChoiceId(long answerChoiceId) { this.answerChoiceId = answerChoiceId; }
  }

  public static class ChoiceDto {
    private long id;
    private String label;
    public ChoiceDto() {}
    public ChoiceDto(long id, String label) { this.id = id; this.label = label; }
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
  }

  public static class RewardDto {
    private int exp;
    private int coin;
    private int level;
    public RewardDto() {}
    public RewardDto(int exp, int coin, int level) { this.exp = exp; this.coin = coin; this.level = level; }
    public int getExp() { return exp; }
    public void setExp(int exp) { this.exp = exp; }
    public int getCoin() { return coin; }
    public void setCoin(int coin) { this.coin = coin; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
  }
}

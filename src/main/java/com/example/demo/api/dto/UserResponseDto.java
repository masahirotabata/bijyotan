package com.example.demo.api.dto;

public class UserResponseDto {
    private Long id;
    private String username;
    private boolean premium;
    private boolean canWatchVideo;
    private boolean casualSuitUnlocked;
    private int loginPoints;

    // ★ 追加項目
    private Integer level;             // null の可能性あり
    private Integer testCorrectTotal;  // null の可能性あり

    public UserResponseDto() {} // ←（任意）デフォルトコンストラクタ

    // ★ level / testCorrectTotal をちゃんと受け取って代入する
    public UserResponseDto(
            Long id,
            String username,
            boolean premium,
            boolean canWatchVideo,
            int loginPoints,
            Integer level,
            Integer testCorrectTotal
    ) {
        this.id = id;
        this.username = username;
        this.premium = premium;
        this.canWatchVideo = canWatchVideo;
        this.loginPoints = loginPoints;
        this.level = level;
        this.testCorrectTotal = testCorrectTotal;
    }

    // --- getters / setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public boolean isPremium() { return premium; }
    public void setPremium(boolean premium) { this.premium = premium; }

    public boolean isCanWatchVideo() { return canWatchVideo; }
    public void setCanWatchVideo(boolean canWatchVideo) { this.canWatchVideo = canWatchVideo; }

    public boolean isCasualSuitUnlocked() { return casualSuitUnlocked; }
    public void setCasualSuitUnlocked(boolean casualSuitUnlocked) { this.casualSuitUnlocked = casualSuitUnlocked; }

    public int getLoginPoints() { return loginPoints; }
    public void setLoginPoints(int loginPoints) { this.loginPoints = loginPoints; }

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }

    public Integer getTestCorrectTotal() { return testCorrectTotal; }
    public void setTestCorrectTotal(Integer testCorrectTotal) { this.testCorrectTotal = testCorrectTotal; }
}

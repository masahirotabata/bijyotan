package com.example.demo.api.dto;

import com.example.demo.domain.entity.Word;

public class WordDto {
    private Long id;
    private String word;
    private String meaning;
    private String pictUrlStatic;
    private String pictUrlAnimated;
    private boolean status;

    public WordDto(Word word, boolean status) {
        this.id = word.getId();
        this.word = word.getWord();
        this.meaning = word.getMeaning();
        this.pictUrlStatic = word.getPictUrlStatic();
        this.pictUrlAnimated = word.getPictUrlAnimated();
        this.status = status;
    }

    // getter / setter（省略可だが今は残してOK）
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }

    public String getMeaning() { return meaning; }
    public void setMeaning(String meaning) { this.meaning = meaning; }

    public String getPictUrlStatic() { return pictUrlStatic; }
    public void setPictUrlStatic(String pictUrlStatic) { this.pictUrlStatic = pictUrlStatic; }

    public String getPictUrlAnimated() { return pictUrlAnimated; }
    public void setPictUrlAnimated(String pictUrlAnimated) { this.pictUrlAnimated = pictUrlAnimated; }

    public boolean isStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }

}

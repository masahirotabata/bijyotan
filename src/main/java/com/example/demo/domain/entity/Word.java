package com.example.demo.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "WORD")
@Data
@NoArgsConstructor
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "WORD", nullable = false)
    private String word;

    @Column(name = "MEANING", nullable = false)
    private String meaning;

    @Column(name = "PICT_DESCRIPTION", length = 500)
    private String pictDescription;

    @Column(name = "STATUS", nullable = false)
    private boolean status;

    @Column(name = "NEXT_PRESENTATION")
    private LocalDateTime nextPresentation;

    @Column(name = "PICT_URL_STATIC")
    private String pictUrlStatic;

    @Column(name = "PICT_URL_ANIMATED")
    private String pictUrlAnimated;

    @Column(name = "USER_ID")
    private Long userId;
    
    @Column(name = "part")  // ←追加
    private String part;

    // getter/setterも忘れずに
    public String getPart() { return part; }
    public void setPart(String part) { this.part = part;
    
    }
}

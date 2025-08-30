package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.api.character.dto.CharacterDto;

@Service
public class CharacterRegistry {

    private final List<CharacterDto> list = List.of(
        new CharacterDto(
            1L, "sanjo", "三条リョウ（風）", 1,
            "/images/characters/sanjo/portrait.png",
            "/images/characters/sanjo/cutin.png",
            "/voice/sanjo",
            "linear-gradient(90deg,#4f46e5,#0ea5e9)",
            List.of("lost","eternal")
        ),
        new CharacterDto(
            2L, "kotobaba", "ことばば", 1,
            "/images/characters/kotobaba/portrait.png",
            "/images/characters/kotobaba/cutin.png",
            "/voice/kotobaba",
            "linear-gradient(90deg,#16a34a,#22c55e)",
            List.of("predict","make")
        )
    );

    public List<CharacterDto> all() { return list; }

    public CharacterDto byId(long id) {
        return list.stream().filter(c -> c.id() == id).findFirst().orElse(null);
    }
}

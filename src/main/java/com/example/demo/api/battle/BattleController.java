package com.example.demo.api.battle;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/battle")
@RequiredArgsConstructor
public class BattleController {

  private final BattleService battleService;

  @PostMapping("/start")
  public StartBattleResponse start(@RequestBody StartBattleRequest req) {
    return battleService.start(req);
  }

  @GetMapping("/next")
  public NextQuestionResponse next(@RequestParam long battleId) {
    return battleService.next(battleId);
  }

  @PostMapping("/answer")
  public AnswerResponse answer(@RequestBody AnswerRequest req) {
    return battleService.answer(req);
  }

  @GetMapping("/ping")
  public String ping(){ return "ok"; }
}

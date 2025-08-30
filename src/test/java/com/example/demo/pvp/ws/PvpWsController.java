// src/main/java/com/example/demo/pvp/ws/PvpWsController.java
package com.example.demo.pvp.ws;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import com.example.demo.pvp.core.PvpGameService;
import com.example.demo.pvp.dto.PvpDtos.Answer;

@Controller
public class PvpWsController {
  private final PvpGameService service;
  public PvpWsController(PvpGameService service){ this.service = service; }

  // WebSocketで自分宛に返す
  @MessageMapping("/pvp/queue.enqueue")
  @SendToUser("/queue/self") // ★ /queue/self に変更
  public EnqueueResp enqueue(@Payload Object req,
                             org.springframework.messaging.Message<?> msg) {
    String user = msg.getHeaders().get("simpSessionId", String.class);
    Long matchId = service.enqueue(user);  // ★ Long or null
    if (matchId == null) return new EnqueueResp("WAITING", null);
    return new EnqueueResp("MATCHED", matchId);
  }

  public static record EnqueueResp(String status, Long matchId) {}

  @MessageMapping("/pvp/answer")
  public void answer(@Payload Answer req, org.springframework.messaging.Message<?> msg) {
    String user = msg.getHeaders().get("simpSessionId", String.class);
    service.submitAnswer(req.matchId(), user, req.seq(), req.choiceId());
  }
}

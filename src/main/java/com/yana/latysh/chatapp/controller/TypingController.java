package com.yana.latysh.chatapp.controller;

import lombok.Data;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Data
public class TypingController {

    @MessageMapping("/chat/room/{roomId}/typing")
    @SendTo("/topic/room/{roomId}/typing")
    public Map<String, Object> typing(@DestinationVariable String roomId, Map<String, Object> data) {
        return data;
    }
}

package com.yana.latysh.chatapp.controller;

import com.yana.latysh.chatapp.entity.Message;
import com.yana.latysh.chatapp.service.ChatService;
import lombok.Data;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@Data
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    // WebSocket: Отправка сообщения в комнату
    @MessageMapping("/chat/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public Message sendMessage(@DestinationVariable String roomId, Message message) {
        message.setRoomId(roomId);
        var savedMessage =  chatService.saveMessage(message).block();  // Асинхронно сохраняем

        chatService.addUserOnline(roomId, message.getUserId())
                .thenMany(chatService.getOnlineUsers(roomId))
                .collectList()
                .subscribe(onlineUsers ->
                        simpMessagingTemplate.convertAndSend("/topic/online-users/" + roomId, onlineUsers)
                );

        return savedMessage;
    }

    @MessageMapping("/chat/message-delete/{roomId}")
    @SendTo("/topic/message-delete/{roomId}")
    public String deleteMessage(@DestinationVariable String roomId, String messageId) {
        chatService.deleteMessage(roomId, messageId).subscribe();
        return messageId;
    }

    // REST: Получить историю сообщений (для инициализации при подключении)
    @GetMapping("/messages/{roomId}")
    public Flux<Message> getMessages(@PathVariable String roomId) {
        var result = chatService.getMessages(roomId);
        return result;
    }
}

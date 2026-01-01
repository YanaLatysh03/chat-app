package com.yana.latysh.chatapp.repository;

import com.yana.latysh.chatapp.entity.Message;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface MessageRepository extends ReactiveMongoRepository<Message, String> {
    Flux<Message> findAllByRoomId(String roomId);
}

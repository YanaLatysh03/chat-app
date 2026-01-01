package com.yana.latysh.chatapp.service;

import com.yana.latysh.chatapp.entity.Message;
import com.yana.latysh.chatapp.entity.User;
import com.yana.latysh.chatapp.repository.MessageRepository;
import com.yana.latysh.chatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    private final ReactiveRedisOperations<String, Message> redisOperations;
    private final ReactiveRedisOperations<String, User> userRedisOperations;
    private static final String CACHE_PREFIX = "room:messages:";
    private static final String ONLINE_PREFIX = "room:online-users:";
    private static final long INACTIVITY_TIMEOUT_MINUTES = 1;
    private final SimpMessagingTemplate simpMessagingTemplate;

    // Сохранить сообщение в Mongo и Redis (кэш последних 50 сообщений с TTL 1 час)
    public Mono<Message> saveMessage(Message message) {
        return messageRepository.save(message)
                .flatMap(saved -> {
                    var key = CACHE_PREFIX + saved.getRoomId();
                    return redisOperations.opsForList().rightPush(key, saved)
                            .then(redisOperations.opsForList().trim(key, -50, -1))  // Оставить последние 50
                            .then(redisOperations.expire(key, Duration.ofHours(1)))
                            .thenReturn(saved);
                });
    }

    public Mono<Void> deleteMessage(String roomId, String messageId) {
        var key = CACHE_PREFIX + roomId;
        return messageRepository.findById(messageId)
                .flatMap(mess -> {
                    return redisOperations.opsForList().remove(key, 1, mess)
                            .then(messageRepository.deleteById(messageId));
                });
    }

    // Получить историю: сначала из Redis, если пусто — из Mongo и закешировать
    public Flux<Message> getMessages(String roomId) {
        var key = CACHE_PREFIX + roomId;
        return redisOperations.opsForList().range(key, 0, 1)
                .switchIfEmpty(
                        messageRepository.findAllByRoomId(roomId)
                                .collectList()
                                .flatMapMany(messages -> {
                                    if (messages.isEmpty()) return Flux.empty();
                                    return redisOperations.opsForList().rightPushAll(key, messages)
                                            .then(redisOperations.expire(key, Duration.ofHours(1)))
                                            .thenMany(Flux.fromIterable(messages));
                                })
                );
    }

    public Mono<Void> addUserOnline(String roomId, String userId) {
        var key = ONLINE_PREFIX + roomId;
        var currentTimeStamp = Instant.now().toEpochMilli();
        return userRepository.findById(userId)
                .flatMap(user -> userRedisOperations.opsForZSet().add(key, user, currentTimeStamp)
                        .then(redisOperations.expire(key, Duration.ofHours(2)))
                        .then());
    }

    public Flux<User> getOnlineUsers(String roomId) {
        var key = ONLINE_PREFIX + roomId;
        var cutoffTime = Double.valueOf(Instant.now().minusSeconds(INACTIVITY_TIMEOUT_MINUTES * 60).toEpochMilli());

        // Получаем только пользователей, активных за последние 5 минут
        return userRedisOperations.opsForZSet()
                .rangeByScore(key, Range.open(cutoffTime, Double.MAX_VALUE));
    }

    public Disposable cleanupInactiveUsers(String roomId) {
        var key = ONLINE_PREFIX + roomId;
        var cutoffTime = Double.valueOf(Instant.now().minusSeconds(INACTIVITY_TIMEOUT_MINUTES * 60).toEpochMilli());

        return userRedisOperations.opsForZSet()
                .removeRangeByScore(key, Range.open(Double.NEGATIVE_INFINITY, cutoffTime))
                .thenMany(userRedisOperations.opsForZSet().rangeByScore(key, Range.open(cutoffTime, Double.MAX_VALUE)))
                .collectList()
                .subscribe(users ->
                        simpMessagingTemplate.convertAndSend("/topic/online-users/" + roomId, users)
                );
    }

    @Scheduled(fixedRate = 60000)
    public void scheduledCleanup() {
        userRedisOperations.keys("room:online-users:*")
                        .map(key -> cleanupInactiveUsers(key.substring(ONLINE_PREFIX.length()))).subscribe();

    }
}

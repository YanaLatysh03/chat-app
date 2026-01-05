package com.yana.latysh.chatapp.service;

import com.yana.latysh.chatapp.entity.Room;
import com.yana.latysh.chatapp.repository.RoomRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    public Mono<Room> getRoomById(String id) {
        return roomRepository.findById(id);
    }

    public Flux<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Mono<Room> createRoom(Room room) {
        return roomRepository.save(room);
    }

    public Mono<Room> joinRoom(String roomId, String userId) {
        return roomRepository.findById(roomId)
                .flatMap(room -> {
                    if (!room.getUserIds().contains(userId)) {
                        room.getUserIds().add(userId);
                    }
                    return roomRepository.save(room);
                });
    }
}

package com.yana.latysh.chatapp.controller;

import com.yana.latysh.chatapp.entity.Room;
import com.yana.latysh.chatapp.service.RoomService;
import lombok.Data;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/rooms")
@Data
public class RoomController {

    private final RoomService roomService;

    @GetMapping("/{id}")
    public Mono<Room> getRoomById(@PathVariable String id) {
        return roomService.getRoomById(id);
    }

    @GetMapping("/")
    public Flux<Room> getAllRooms() {
        return roomService.getAllRooms();
    }

    @PostMapping("/")
    public Mono<Room> createRoom(@RequestBody Room room) {
        return roomService.createRoom(room);
    }

    @PostMapping("/{roomId}/join/{userId}")
    public Mono<Room> joinRoom(@PathVariable String roomId, @PathVariable String userId) {
        return roomService.joinRoom(roomId, userId);
    }

}

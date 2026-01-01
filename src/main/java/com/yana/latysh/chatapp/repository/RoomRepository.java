package com.yana.latysh.chatapp.repository;

import com.yana.latysh.chatapp.entity.Room;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface RoomRepository extends ReactiveMongoRepository<Room, String> {
}

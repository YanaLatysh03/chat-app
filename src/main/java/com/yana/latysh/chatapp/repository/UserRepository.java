package com.yana.latysh.chatapp.repository;

import com.yana.latysh.chatapp.entity.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface  UserRepository extends ReactiveMongoRepository<User, String> {

}

package com.yana.latysh.chatapp.service;

import com.yana.latysh.chatapp.entity.User;
import com.yana.latysh.chatapp.repository.UserRepository;
import lombok.Data;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Data
public class UserService {

    private final UserRepository userRepository;

    public Mono<User> createUser(User user) {
        return userRepository.save(user);
    }
}

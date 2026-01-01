package com.yana.latysh.chatapp.controller;

import com.yana.latysh.chatapp.entity.User;
import com.yana.latysh.chatapp.service.UserService;
import lombok.Data;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/users")
@Data
public class UserController {

    private final UserService userService;

    @PostMapping("/")
    public Mono<User> createUser(@RequestBody User user) {
        return userService.createUser(user);
    }
}

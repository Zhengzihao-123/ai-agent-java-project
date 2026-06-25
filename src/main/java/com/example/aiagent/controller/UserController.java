package com.example.aiagent.controller;

import com.example.aiagent.common.Result;
import com.example.aiagent.entity.User;
import com.example.aiagent.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<User> register(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        String realName = params.get("realName");
        String email = params.get("email");
        User user = userService.register(username, password, realName, email);
        return Result.success(user);
    }

    @PostMapping("/login")
    public Result<User> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        User user = userService.login(username, password);
        return Result.success(user);
    }

}

package com.example.aiagent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.aiagent.entity.User;

public interface UserService extends IService<User> {
    User register(String username, String password);
    User register(String username, String password, String realName, String email);
    User login(String username, String password);
}

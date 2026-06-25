package com.example.aiagent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.aiagent.entity.User;
import com.example.aiagent.mapper.UserMapper;
import com.example.aiagent.service.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User register(String username, String password) {
        return register(username, password, null, null);
    }

    @Override
    public User register(String username, String password, String realName, String email) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User existUser = getOne(wrapper);
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRealName(realName);
        user.setEmail(email);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        save(user);
        return user;
    }

    @Override
    public User login(String username, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        wrapper.eq(User::getPassword, password);
        User user = getOne(wrapper);
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        return user;
    }

}

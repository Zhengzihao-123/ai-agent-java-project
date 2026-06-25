package com.example.aiagent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.aiagent.entity.UserRole;

import java.util.List;

public interface UserRoleService extends IService<UserRole> {
    List<Long> getRoleIdsByUserId(Long userId);
    void assignRole(Long userId, Long roleId);
    void removeRole(Long userId, Long roleId);
    boolean hasRole(Long userId, String roleName);
}
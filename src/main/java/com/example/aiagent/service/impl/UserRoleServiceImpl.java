package com.example.aiagent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.aiagent.entity.Role;
import com.example.aiagent.entity.UserRole;
import com.example.aiagent.mapper.RoleMapper;
import com.example.aiagent.mapper.UserRoleMapper;
import com.example.aiagent.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public List<Long> getRoleIdsByUserId(Long userId) {
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, userId);
        return list(wrapper).stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());
    }

    @Override
    public void assignRole(Long userId, Long roleId) {
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, userId);
        wrapper.eq(UserRole::getRoleId, roleId);
        if (count(wrapper) == 0) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            save(userRole);
        }
    }

    @Override
    public void removeRole(Long userId, Long roleId) {
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, userId);
        wrapper.eq(UserRole::getRoleId, roleId);
        remove(wrapper);
    }

    @Override
    public boolean hasRole(Long userId, String roleName) {
        List<Long> roleIds = getRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            return false;
        }
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Role::getId, roleIds);
        wrapper.eq(Role::getRoleName, roleName);
        return roleMapper.selectCount(wrapper) > 0;
    }
}
package com.example.aiagent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.aiagent.entity.Role;
import com.example.aiagent.mapper.RoleMapper;
import com.example.aiagent.service.RoleService;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {
}

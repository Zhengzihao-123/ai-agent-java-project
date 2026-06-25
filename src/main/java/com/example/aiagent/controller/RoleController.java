package com.example.aiagent.controller;

import com.example.aiagent.common.Result;
import com.example.aiagent.entity.Role;
import com.example.aiagent.service.RoleService;
import com.example.aiagent.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserRoleService userRoleService;

    @GetMapping("/list")
    public Result<List<Role>> list() {
        return Result.success(roleService.list());
    }

    @PostMapping("/assign")
    public Result<Void> assignRole(@RequestBody Map<String, Long> params) {
        Long userId = params.get("userId");
        Long roleId = params.get("roleId");
        userRoleService.assignRole(userId, roleId);
        return Result.success(null);
    }

    @PostMapping("/remove")
    public Result<Void> removeRole(@RequestBody Map<String, Long> params) {
        Long userId = params.get("userId");
        Long roleId = params.get("roleId");
        userRoleService.removeRole(userId, roleId);
        return Result.success(null);
    }

    @GetMapping("/check/{userId}/{roleName}")
    public Result<Boolean> checkRole(@PathVariable Long userId, @PathVariable String roleName) {
        boolean hasRole = userRoleService.hasRole(userId, roleName);
        return Result.success(hasRole);
    }
}
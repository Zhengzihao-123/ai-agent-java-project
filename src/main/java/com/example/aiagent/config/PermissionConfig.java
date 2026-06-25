package com.example.aiagent.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class PermissionConfig {

    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_USER = "user";

    public static final String PERMISSION_USER_READ = "user:read";
    public static final String PERMISSION_USER_WRITE = "user:write";
    public static final String PERMISSION_CHAT_READ = "chat:read";
    public static final String PERMISSION_CHAT_WRITE = "chat:write";
    public static final String PERMISSION_KNOWLEDGE_READ = "knowledge:read";
    public static final String PERMISSION_KNOWLEDGE_WRITE = "knowledge:write";
    public static final String PERMISSION_ADMIN_READ = "admin:read";
    public static final String PERMISSION_ADMIN_WRITE = "admin:write";
}
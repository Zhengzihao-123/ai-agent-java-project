CREATE DATABASE IF NOT EXISTS ai_agent_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ai_agent_db;

-- 删除旧表
DROP TABLE IF EXISTS chat_history;
DROP TABLE IF EXISTS doc_chunk;
DROP TABLE IF EXISTS knowledge_doc;
DROP TABLE IF EXISTS role;
DROP TABLE IF EXISTS user;

-- 1. 用户表
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码（加密）',
    real_name VARCHAR(50) COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 角色权限表
CREATE TABLE role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名：admin/user',
    description VARCHAR(100) COMMENT '角色描述'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 3. 知识库文档表
CREATE TABLE knowledge_doc (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文档ID',
    user_id BIGINT NOT NULL COMMENT '上传用户ID',
    doc_name VARCHAR(100) NOT NULL COMMENT '文档名称',
    doc_type VARCHAR(20) COMMENT '文档类型：pdf/word/txt',
    file_path VARCHAR(200) NOT NULL COMMENT '文件存储路径',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    FOREIGN KEY (user_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文档表';

-- 4. 文本分块表（RAG用）
CREATE TABLE doc_chunk (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分块ID',
    doc_id BIGINT NOT NULL COMMENT '所属文档ID',
    chunk_text TEXT NOT NULL COMMENT '分块文本内容',
    chunk_index INT DEFAULT 0 COMMENT '分块索引',
    total_chunks INT DEFAULT 0 COMMENT '总分块数',
    vector BLOB COMMENT '向量数据（简化存储）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (doc_id) REFERENCES knowledge_doc(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档分块表';

-- 5. 聊天记录表
CREATE TABLE chat_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '聊天ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    conversation_id VARCHAR(100) NOT NULL DEFAULT 'default' COMMENT '会话ID',
    agent_role VARCHAR(50) NOT NULL COMMENT '智能体角色：course/training/cert/competition',
    user_message TEXT NOT NULL COMMENT '用户消息',
    ai_reply TEXT NOT NULL COMMENT 'AI回复',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '聊天时间',
    FOREIGN KEY (user_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天记录表';

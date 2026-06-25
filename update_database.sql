-- 为 chat_history 表添加 conversation_id 字段
USE ai_agent_db;

-- 检查字段是否已存在
SET @dbname = DATABASE();
SET @tablename = 'chat_history';
SET @columnname = 'conversation_id';

SET @preparedStatement = (SELECT IF(
                                         (
                                             SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
                                             WHERE
                                                 (table_schema = @dbname)
                                               AND (table_name = @tablename)
                                               AND (column_name = @columnname)
                                         ) > 0,
                                         'SELECT 1',
                                         CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' VARCHAR(100) NOT NULL DEFAULT ''default_conv_', UUID(), ''' COMMENT ''会话ID'' AFTER user_id')
                                 ));

PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 添加索引（如果不存在的话，通过存储过程实现）
DROP PROCEDURE IF EXISTS add_index_if_not_exists;
DELIMITER //
CREATE PROCEDURE add_index_if_not_exists()
BEGIN
    DECLARE index_exists INT DEFAULT 0;
    SELECT COUNT(*) INTO index_exists
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE table_schema = DATABASE()
      AND table_name = 'chat_history'
      AND index_name = 'idx_user_conversation';

    IF index_exists = 0 THEN
        ALTER TABLE chat_history ADD INDEX idx_user_conversation (user_id, conversation_id);
        SELECT 'Index idx_user_conversation added successfully' as message;
    ELSE
        SELECT 'Index idx_user_conversation already exists' as message;
    END IF;
END //
DELIMITER ;

CALL add_index_if_not_exists();
DROP PROCEDURE IF EXISTS add_index_if_not_exists;

-- 更新已有记录的 conversation_id
-- 按 user_id 和 agent_role 分组，为每组设置相同的 conversation_id
UPDATE chat_history t1
    JOIN (
        SELECT
            id,
            user_id,
            agent_role,
            create_time,
            CONCAT('legacy_', user_id, '_', agent_role) as new_conversation_id
        FROM chat_history
        WHERE conversation_id IS NULL OR conversation_id = ''
    ) t2 ON t1.id = t2.id
SET t1.conversation_id = t2.new_conversation_id;

SELECT 'Database update completed successfully!' as message;

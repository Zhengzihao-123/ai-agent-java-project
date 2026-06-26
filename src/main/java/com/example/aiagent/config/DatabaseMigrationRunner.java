package com.example.aiagent.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Component
public class DatabaseMigrationRunner implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            addColumnIfNotExists(connection, "doc_chunk", "chunk_index", "INT DEFAULT 0 COMMENT '分块索引'");
            addColumnIfNotExists(connection, "doc_chunk", "total_chunks", "INT DEFAULT 0 COMMENT '总分块数'");
            addColumnIfNotExists(connection, "chat_history", "conversation_id", "VARCHAR(100) NOT NULL DEFAULT 'default' COMMENT '会话ID'");
        }
    }

    private void addColumnIfNotExists(Connection connection, String tableName, String columnName, String columnDefinition) {
        try {
            String checkSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
                checkStmt.setString(1, tableName);
                checkStmt.setString(2, columnName);
                var resultSet = checkStmt.executeQuery();
                if (resultSet.next() && resultSet.getInt(1) > 0) {
                    return;
                }
            }

            String alterSql = "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition;
            try (PreparedStatement alterStmt = connection.prepareStatement(alterSql)) {
                alterStmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

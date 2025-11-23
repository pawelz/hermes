// Copyright 2025 Paweł Zuzelski <pawelz@execve.ch>
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package ch.execve.hermes;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Manages logging of dispatched emails to a SQLite database. */
class DatabaseLogger {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseLogger.class);
    private final Connection connection;

    private static final String CREATE_TABLE_SQL =
        "CREATE TABLE IF NOT EXISTS dispatch_log ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + "message_id TEXT,"
            + "return_path TEXT,"
            + "from_address TEXT,"
            + "subject TEXT,"
            + "matching_classifier TEXT,"
            + "returned_inbox_path TEXT"
            + ");";

    private static final String INSERT_LOG_SQL =
        "INSERT INTO dispatch_log(timestamp, message_id, return_path, from_address, subject, matching_classifier, returned_inbox_path) "
            + "VALUES(?,?,?,?,?,?,?);";

    @Inject
    DatabaseLogger(@Named("databasePath") String databasePath) {
        try {
            String url = "jdbc:sqlite:" + databasePath;
            this.connection = DriverManager.getConnection(url);
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(CREATE_TABLE_SQL);
            }
            logger.info("Successfully connected to SQLite database at {}", databasePath);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize SQLite database connection", e);
        }
    }

    public void log(
        String messageId,
        String returnPath,
        String from,
        String subject,
        String classifier,
        String inboxPath) {
        try (PreparedStatement pstmt = connection.prepareStatement(INSERT_LOG_SQL)) {
            pstmt.setTimestamp(1, Timestamp.from(Instant.now()));
            pstmt.setString(2, messageId);
            pstmt.setString(3, returnPath);
            pstmt.setString(4, from);
            
            // Truncate subject if it's too long
            String truncatedSubject = (subject != null && subject.length() > 255) ? subject.substring(0, 255) : subject;
            pstmt.setString(5, truncatedSubject);
            
            pstmt.setString(6, classifier);
            pstmt.setString(7, inboxPath);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to write to dispatch_log table", e);
        }
    }

    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.error("Failed to close SQLite connection", e);
        }
    }
}
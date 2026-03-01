package auto;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    // Формат URL для PostgreSQL: jdbc:postgresql://хост:порт/имя_базы  
    private static final String URL = "jdbc:postgresql://localhost:5432/test_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "secret";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
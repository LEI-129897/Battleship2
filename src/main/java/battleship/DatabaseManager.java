package battleship;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:battleship_moves.db";

    public DatabaseManager() {
        initDatabase();
    }

    private void initDatabase() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS moves ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, "
                + "mode TEXT, "
                + "remaining_ships INTEGER"
                + ");";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            System.err.println("Erro ao inicializar base de dados: " + e.getMessage());
        }
    }

    public void saveMove(String mode, int remainingShips) {
        String insertSQL = "INSERT INTO moves(mode, remaining_ships) VALUES(?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, mode);
            pstmt.setInt(2, remainingShips);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao guardar jogada: " + e.getMessage());
        }
    }
}
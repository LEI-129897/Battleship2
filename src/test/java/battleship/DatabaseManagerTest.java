package battleship;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class DatabaseManagerTest {

    @Test
    @DisplayName("Garante que a jogada é guardada na base de dados sem erros")
    void testSaveMove() {
        
        DatabaseManager dbManager = new DatabaseManager(); 
        String mode = "EASY";
        int remainingShips = 5;

        
        assertDoesNotThrow(() -> {
            dbManager.saveMove(mode, remainingShips);
        });
        
        
    }
}
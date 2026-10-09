package battleship;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class GUI extends Application {

    private static GUI instance;
    private static IGame currentGame;
    private GridPane grid;

    public static void setGame(IGame game) {
        currentGame = game;
    }

    @Override
    public void start(Stage primaryStage) {
        instance = this;
        grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(2);
        grid.setVgap(2);

        renderboard();

        Scene scene = new Scene(grid, 400, 400);
        primaryStage.setTitle("Visualização do Tabuleiro - Batalha Naval");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void renderboard(){
        // Se houver um jogo ativo, desenhamos a grelha com base no estado real
        if (currentGame != null) {
            IFleet fleet = currentGame.getMyFleet();

            for (int r = 0; r < Game.BOARD_SIZE; r++) {
                for (int c = 0; c < Game.BOARD_SIZE; c++) {
                    Rectangle cell = new Rectangle(35, 35);
                    Position pos = new Position(r, c);

                    // 1. Cor padrão de água
                    cell.setFill(Color.LIGHTBLUE);

                    // 2. Se a posição tem navio
                    IShip ship = fleet.shipAt(pos);
                    if (ship != null) {
                        cell.setFill(Color.GRAY);
                    }

                    // 3. Se houve tiros na posição (analisando os movimentos do alien)
                    for (IMove move : currentGame.getAlienMoves()) {
                        if (move.getShots().contains(pos)) {
                            if (ship != null) {
                                cell.setFill(Color.RED); // Tiro num navio
                            } else {
                                cell.setFill(Color.DARKBLUE); // Tiro na água
                            }
                        }
                    }

                    cell.setStroke(Color.WHITE);
                    grid.add(cell, c, r);
                }
            }
        }
    }

    public static void display(IGame currentGame) {

        setGame(currentGame);

        if (instance == null) {
            // PRIMEIRA VEZ: Lança a janela numa Thread isolada
            new Thread(() -> Application.launch(GUI.class)).start();
        } else {
            // PRÓXIMAS VEZES: Atualiza a interface existente sem chamar o launch() outra vez!
            Platform.runLater(() -> instance.renderboard());
        }
    }
}

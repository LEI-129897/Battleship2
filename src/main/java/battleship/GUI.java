package battleship;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/** Interactive board backed by the same session used by the console. */
public class GUI extends Application {

    private static final AtomicBoolean LAUNCHED = new AtomicBoolean();
    private static volatile GUI instance;
    private static volatile GamePlaySession session;
    private static volatile boolean standalone;
    private static volatile boolean closeRequested;

    private final List<IPosition> selected = new ArrayList<>();
    private final Button[][] cells = new Button[Game.BOARD_SIZE][Game.BOARD_SIZE];
    private final Label turn = new Label();
    private final Label fleet = new Label();
    private final Label targets = new Label();
    private final Label notice = new Label();
    private final Label result = new Label();
    private final Label elapsed = new Label();
    private final Label currentElapsed = new Label();
    private Timeline timerUpdates;
    private Button fire;
    private Button clear;
    private Button newGame;
    private CheckBox showShips;
    private Stage stage;
    private int generation = -1;
    private int moveNumber = -1;

    public static void play() {
        standalone = true;
        GamePlaySession game = new GamePlaySession();
        game.setGame(new Game(Fleet.createRandom()));
        display(game);
    }

    public static void setGame(IGame game) {
        if (session == null || !session.isGame(game)) {
            GamePlaySession replacement = new GamePlaySession();
            replacement.setGame(game);
            session = replacement;
        }
    }

    public static void display(IGame game) {
        setGame(game);
        display(session);
    }

    public static void display(GamePlaySession game) {
        session = Objects.requireNonNull(game);
        if (LAUNCHED.compareAndSet(false, true)) {
            Thread launcher = new Thread(() -> Application.launch(GUI.class), "battleship-gui-launcher");
            launcher.start();
        } else if (instance != null) {
            Platform.runLater(() -> {
                instance.renderboard();
                instance.stage.show();
            });
        }
    }

    public static void close() {
        closeRequested = true;
        if (instance != null) Platform.runLater(Platform::exit);
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        Platform.setImplicitExit(standalone);
        if (closeRequested) {
            Platform.exit();
            return;
        }

        Label title = new Label(text("guiTitle"));
        title.getStyleClass().add("title");
        Label subtitle = new Label(text("guiSelectShots", Game.NUMBER_SHOTS));
        subtitle.getStyleClass().add("muted");
        VBox header = new VBox(6, title, subtitle);
        header.setPadding(new Insets(0, 0, 20, 0));

        GridPane grid = new GridPane();
        grid.setHgap(4);
        grid.setVgap(4);
        grid.setAlignment(Pos.TOP_LEFT);
        for (int index = 0; index < Game.BOARD_SIZE; index++) {
            Label column = new Label(Integer.toString(index + 1));
            column.getStyleClass().add("coordinate");
            column.setMinWidth(38);
            column.setAlignment(Pos.CENTER);
            grid.add(column, index + 1, 0);
            Label row = new Label(Character.toString((char) ('A' + index)));
            row.getStyleClass().add("coordinate");
            row.setMinWidth(22);
            grid.add(row, 0, index + 1);
        }
        for (int row = 0; row < Game.BOARD_SIZE; row++) {
            for (int column = 0; column < Game.BOARD_SIZE; column++) {
                IPosition position = new Position(row, column);
                Button cell = new Button();
                cell.setId("cell-" + position);
                cell.setPrefSize(38, 38);
                cell.setMinSize(38, 38);
                cell.setMaxSize(38, 38);
                cell.setTooltip(new Tooltip(position.toString()));
                cell.setOnAction(event -> select(position));
                cells[row][column] = cell;
                grid.add(cell, column + 1, row + 1);
            }
        }

        turn.getStyleClass().add("section-title");
        fleet.getStyleClass().add("muted");
        targets.setWrapText(true);
        targets.setMinHeight(44);
        notice.setWrapText(true);
        notice.setMinHeight(48);
        notice.getStyleClass().add("muted");
        result.setWrapText(true);
        result.setMinHeight(60);
        elapsed.getStyleClass().add("time");
        elapsed.setId("last-move-time");
        currentElapsed.getStyleClass().add("time");
        currentElapsed.setId("current-move-time");

        fire = new Button(text("guiFire", Game.NUMBER_SHOTS));
        fire.setId("fire-shots");
        fire.getStyleClass().add("primary");
        fire.setMaxWidth(Double.MAX_VALUE);
        fire.setOnAction(event -> fire());
        clear = new Button(text("guiClear"));
        clear.setId("clear-selection");
        clear.setMaxWidth(Double.MAX_VALUE);
        clear.setOnAction(event -> {
            selected.clear();
            renderboard();
        });
        newGame = new Button(text("guiNewGame"));
        newGame.setId("new-game");
        newGame.setMaxWidth(Double.MAX_VALUE);
        newGame.setOnAction(event -> newGame());
        showShips = new CheckBox(text("guiShowShips"));
        showShips.setSelected(true);
        showShips.setOnAction(event -> renderboard());

        VBox controls = new VBox(8, turn, fleet, new Label(text("guiCurrentMove")), currentElapsed,
                targets, fire, clear, notice, newGame, showShips,
                new Label(text("guiLastMove")), result, elapsed);
        controls.getStyleClass().add("panel");
        controls.setPrefWidth(224);
        controls.setMinWidth(224);
        controls.setMaxWidth(224);
        HBox boardAndControls = new HBox(24, grid, controls);
        HBox legend = new HBox(20, legend("cell-ship", "guiShip"), legend("cell-hit", "guiHit"),
                legend("cell-miss", "guiMiss"), legend("cell-selected", "guiSelected"));
        legend.setPadding(new Insets(20, 0, 0, 0));
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(24));
        root.setTop(header);
        root.setCenter(boardAndControls);
        root.setBottom(legend);
        Scene scene = new Scene(root, 740, 780);
        scene.getStylesheets().add(Objects.requireNonNull(GUI.class.getResource("gui.css")).toExternalForm());
        stage.setTitle(text("guiTitle"));
        stage.setScene(scene);
        stage.setMinWidth(740);
        stage.setMinHeight(780);
        timerUpdates = new Timeline(new KeyFrame(Duration.millis(100), event -> refreshTimer()));
        timerUpdates.setCycleCount(Animation.INDEFINITE);
        stage.setOnShown(event -> {
            refreshTimer();
            timerUpdates.play();
        });
        stage.setOnHidden(event -> timerUpdates.stop());
        if (!standalone) {
            stage.setOnCloseRequest(event -> {
                event.consume();
                stage.hide();
            });
        }
        instance = this;
        renderboard();
        stage.show();
        session.beginMove();
        refreshTimer();
    }

    @Override
    public void stop() {
        if (timerUpdates != null) timerUpdates.stop();
    }

    private static HBox legend(String style, String key) {
        Region swatch = new Region();
        swatch.getStyleClass().addAll("swatch", style);
        swatch.setPrefSize(12, 12);
        Label label = new Label(text(key));
        label.getStyleClass().add("muted");
        HBox item = new HBox(7, swatch, label);
        item.setAlignment(Pos.CENTER_LEFT);
        return item;
    }

    private void select(IPosition position) {
        GamePlaySession.BoardSnapshot board = session.snapshot();
        if (board.generation() != generation || board.nextMove() != moveNumber) renderboard();
        if (board.finished() || board.simulating() || board.shots()[position.getRow()][position.getColumn()]) return;
        if (selected.contains(position)) {
            selected.remove(position);
        } else if (selected.size() < Game.NUMBER_SHOTS) {
            selected.add(position);
        }
        renderboard();
    }

    private void fire() {
        try {
            session.fire(List.copyOf(selected), generation, moveNumber);
            selected.clear();
            session.withGame(game -> {
                game.getMyFleet().printStatus();
                game.printMyBoard(true, false);
                if (game.getRemainingShips() == 0) game.over();
            });
            renderboard();
            session.beginMove();
            refreshTimer();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            renderboard();
            notice.setText(exception.getMessage());
        }
    }

    private void newGame() {
        synchronized (session) {
            if (session.snapshot().simulating()) return;
            session.setGame(new Game(Fleet.createRandom()));
        }
        selected.clear();
        renderboard();
        session.beginMove();
        refreshTimer();
    }

    public void renderboard() {
        GamePlaySession.BoardSnapshot board = session.snapshot();
        if (generation != board.generation() || moveNumber != board.nextMove()) selected.clear();
        generation = board.generation();
        moveNumber = board.nextMove();
        boolean playable = board.hasGame() && !board.finished() && !board.simulating();
        for (int row = 0; row < Game.BOARD_SIZE; row++) {
            for (int column = 0; column < Game.BOARD_SIZE; column++) {
                IPosition position = new Position(row, column);
                Button cell = cells[row][column];
                boolean shot = board.shots()[row][column];
                boolean ship = board.ships()[row][column];
                int selectedIndex = selected.indexOf(position);
                String style = shot ? (ship ? "cell-hit" : "cell-miss")
                        : selectedIndex >= 0 ? "cell-selected" : ship && showShips.isSelected() ? "cell-ship" : "cell-water";
                cell.getStyleClass().setAll("board-cell", style);
                cell.setText(shot ? (ship ? "×" : "•") : selectedIndex >= 0 ? Integer.toString(selectedIndex + 1) : "");
                cell.setDisable(!playable || shot);
                cell.setAccessibleText(position + (shot ? " — " + text(ship ? "guiHit" : "guiMiss") : ""));
            }
        }
        turn.setText(text("guiTurn", board.nextMove()));
        fleet.setText(text("guiRemaining", board.remainingShips()));
        String coordinates = selected.stream().map(Object::toString).collect(Collectors.joining("  "));
        targets.setText(text("guiTargets", selected.size(), Game.NUMBER_SHOTS) + "\n" + coordinates);
        fire.setDisable(!playable || selected.size() != Game.NUMBER_SHOTS);
        clear.setDisable(selected.isEmpty());
        newGame.setDisable(board.simulating());
        notice.setText(text(board.finished() ? "guiFinished" : board.simulating() ? "guiSimulating"
                : selected.size() == Game.NUMBER_SHOTS ? "guiReady" : "guiSelectShots", Game.NUMBER_SHOTS));
        GamePlaySession.MoveReport report = board.lastMove();
        if (report == null) {
            result.setText(text("guiNoMoves"));
            elapsed.setText("—");
        } else {
            result.setText(text("guiResult", report.number(), report.hits(), report.misses(), report.sunk()));
            elapsed.setText(text("guiTime", String.format(Locale.ROOT, "%.3f", report.seconds())));
        }
        refreshTimer();
    }

    private void refreshTimer() {
        GamePlaySession.TimerSnapshot timer = session.timerSnapshot();
        currentElapsed.setText(timer.running()
                ? text("guiTime", String.format(Locale.ROOT, "%.3f", timer.seconds())) : "—");
    }

    private static String text(String key, Object... arguments) {
        return GestorIdioma.getMensagemFormatada(key, arguments);
    }
}

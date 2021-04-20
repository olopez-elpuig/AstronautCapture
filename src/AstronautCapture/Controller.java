package AstronautCapture;

import javafx.fxml.FXML;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.application.Platform;
import java.util.Timer;
import java.util.TimerTask;

public class Controller implements EventHandler<KeyEvent> {
    final private static double FRAMES_PER_SECOND = 5.0;

    @FXML private Label scoreLabel;
    @FXML private Label levelLabel;
    @FXML private Label gameOverLabel;
    @FXML private GameView gameView;
    private GameModel gameModel;
    private static final String[] levelFiles = {"src/levels/level1.txt", "src/levels/level2.txt"};

    private Timer timer;
    private static int ovniEatingModeCounter;
    private boolean paused;

    public Controller() {
        this.paused = false;
    }

    /**
     * Inicializa y actualiza el modelo y visualiza desde el primer archivo txt e inicia el temporizador.
     * */
    public void initialize() {
        String file = this.getLevelFile(0);
        this.gameModel = new GameModel();
        this.update(GameModel.Direction.NONE);
        ovniEatingModeCounter = 25;
        this.startTimer();
    }

    /**
     * Programa la actualización del modelo en función del temporizador.
     */
    private void startTimer() {
        this.timer = new java.util.Timer();
        TimerTask timerTask = new TimerTask() {
            public void run() {
                Platform.runLater(new Runnable() {
                    public void run() {
                        update(gameModel.getCurrentDirection());
                    }
                });
            }
        };

        long frameTimeInMilliseconds = (long)(1000.0 / FRAMES_PER_SECOND);
        this.timer.schedule(timerTask, 0, frameTimeInMilliseconds);
    }

    /**
     * Sube el PersonajeModel, actualiza la vista, actualiza la puntuación y el nivel, muestra Game Over / Has Ganado e instrucciones de cómo jugar
     * @param direction la dirección ingresada más recientemente para que el personaje se mueva
     */
    private void update(GameModel.Direction direction) {
        this.gameModel.step(direction);
        this.gameView.update(gameModel);
        this.scoreLabel.setText(String.format("Score: %d", this.gameModel.getScore()));
        this.levelLabel.setText(String.format("Level: %d", this.gameModel.getLevel()));
        if (gameModel.isGameOver()) {
            this.gameOverLabel.setText(String.format("GAME OVER"));
            pause();
        }
        if (gameModel.isYouWon()) {
            this.gameOverLabel.setText(String.format("HAS GANADO!"));
        }
        //cuando el personaje está en ovniEatingMode, cuente hacia atrás el ovniEatingModeCounter para restablecer ovniEatingMode a falso cuando el contador es 0
        if (gameModel.isOvniEatingMode()) {
            ovniEatingModeCounter--;
        }
        if (ovniEatingModeCounter == 0 && gameModel.isOvniEatingMode()) {
            gameModel.setOvniEatingMode(false);
        }
    }

    /**
     * Toma la entrada del teclado del usuario para controlar el movimiento del personaje y comenzar nuevos juegos
     * @param keyEvent clic de tecla del usuario
     */
    @Override
    public void handle(KeyEvent keyEvent) {
        boolean keyRecognized = true;
        KeyCode code = keyEvent.getCode();
        GameModel.Direction direction = GameModel.Direction.NONE;
        if (code == KeyCode.LEFT) {
            direction = GameModel.Direction.LEFT;
        } else if (code == KeyCode.RIGHT) {
            direction = GameModel.Direction.RIGHT;
        } else if (code == KeyCode.UP) {
            direction = GameModel.Direction.UP;
        } else if (code == KeyCode.DOWN) {
            direction = GameModel.Direction.DOWN;
        } else if (code == KeyCode.G) {
            pause();
            this.gameModel.startNewGame();
            this.gameOverLabel.setText(String.format(""));
            paused = false;
            this.startTimer();
        } else {
            keyRecognized = false;
        }
        if (keyRecognized) {
            keyEvent.consume();
            gameModel.setCurrentDirection(direction);
        }
    }

    /**
     * Pausa el temporizador
     */
    public void pause() {
            this.timer.cancel();
            this.paused = true;
    }

    public double getBoardWidth() {
        return GameView.CELL_WIDTH * this.gameView.getColumnCount();
    }

    public double getBoardHeight() {
        return GameView.CELL_WIDTH * this.gameView.getRowCount();
    }

    public static void setOvniEatingModeCounter() {
        ovniEatingModeCounter = 25;
    }

    public static int getovniEatingModeCounter() {
        return ovniEatingModeCounter;
    }

    public static String getLevelFile(int x)
    {
        return levelFiles[x];
    }

    public boolean getPaused() {
        return paused;
    }
}

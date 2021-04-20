package AstronautCapture;

import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import AstronautCapture.GameModel.CellValue;

public class GameView extends Group {
    public final static double CELL_WIDTH = 20.0;

    @FXML private int rowCount;
    @FXML private int columnCount;
    private ImageView[][] cellViews;
    private Image astronautaImage;
    private Image ovniImage;
    private Image ovni2Image;
    private Image ovnidestructibleImage;
    private Image wallImage;
    private Image pistolaImage;
    private Image alienEggImage;

    /**
     * Inicializa los valores de las variables de instancia de imagen de archivos
     */
    public GameView() {
        this.astronautaImage = new Image(getClass().getResourceAsStream("/res/astronauta.gif"));
        this.ovniImage = new Image(getClass().getResourceAsStream("/res/ovni1.png"));
        this.ovni2Image = new Image(getClass().getResourceAsStream("/res/ovni1.png"));
        this.ovnidestructibleImage = new Image(getClass().getResourceAsStream("/res/ovnidestructible.png"));
        this.wallImage = new Image(getClass().getResourceAsStream("/res/wall.png"));
        this.pistolaImage = new Image(getClass().getResourceAsStream("/res/pistola.png"));
        this.alienEggImage = new Image(getClass().getResourceAsStream("/res/alienEgg.png"));
    }

    /**
     * Construye una cuadrícula vacía de ImageViews
     */
    private void initializeGrid() {
        if (this.rowCount > 0 && this.columnCount > 0) {
            this.cellViews = new ImageView[this.rowCount][this.columnCount];
            for (int row = 0; row < this.rowCount; row++) {
                for (int column = 0; column < this.columnCount; column++) {
                    ImageView imageView = new ImageView();
                    imageView.setX((double)column * CELL_WIDTH);
                    imageView.setY((double)row * CELL_WIDTH);
                    imageView.setFitWidth(CELL_WIDTH);
                    imageView.setFitHeight(CELL_WIDTH);
                    this.cellViews[row][column] = imageView;
                    this.getChildren().add(imageView);
                }
            }
        }
    }

    /**
     * Actualiza la vista para reflejar el estado del modelo.
     * @param model
     */
    public void update(GameModel model) {
        assert model.getRowCount() == this.rowCount && model.getColumnCount() == this.columnCount;
        //
        for (int row = 0; row < this.rowCount; row++){
            for (int column = 0; column < this.columnCount; column++){
                CellValue value = model.getCellValue(row, column);
                if (value == CellValue.WALL) {
                    this.cellViews[row][column].setImage(this.wallImage);
                }
                else if (value == CellValue.PISTOLA) {
                    this.cellViews[row][column].setImage(this.pistolaImage);
                }
                else if (value == CellValue.ALIENEGG) {
                    this.cellViews[row][column].setImage(this.alienEggImage);
                }
                else {
                    this.cellViews[row][column].setImage(null);
                }
                //verifique en qué dirección va astronauta y muestre la imagen correspondiente
                if (row == model.getAstronautaLocation().getX() && column == model.getAstronautaLocation().getY() && (GameModel.getLastDirection() == GameModel.Direction.RIGHT || GameModel.getLastDirection() == GameModel.Direction.NONE)) {
                    this.cellViews[row][column].setImage(this.astronautaImage);
                }
                else if (row == model.getAstronautaLocation().getX() && column == model.getAstronautaLocation().getY() && GameModel.getLastDirection() == GameModel.Direction.LEFT) {
                    this.cellViews[row][column].setImage(this.astronautaImage);
                }
                else if (row == model.getAstronautaLocation().getX() && column == model.getAstronautaLocation().getY() && GameModel.getLastDirection() == GameModel.Direction.UP) {
                    this.cellViews[row][column].setImage(this.astronautaImage);
                }
                else if (row == model.getAstronautaLocation().getX() && column == model.getAstronautaLocation().getY() && GameModel.getLastDirection() == GameModel.Direction.DOWN) {
                    this.cellViews[row][column].setImage(this.astronautaImage);
                }
                //hacer que los OVNIS "parpadeen" hacia el final de ovniEatingMode (mostrar imágenes OVNI regulares en actualizaciones alternas del contador)
                if (GameModel.isOvniEatingMode() && (Controller.getovniEatingModeCounter() == 6 ||Controller.getovniEatingModeCounter() == 4 || Controller.getovniEatingModeCounter() == 2)) {
                    if (row == model.getOvniLocation().getX() && column == model.getOvniLocation().getY()) {
                        this.cellViews[row][column].setImage(this.ovniImage);
                    }
                    if (row == model.getOvni2Location().getX() && column == model.getOvni2Location().getY()) {
                        this.cellViews[row][column].setImage(this.ovni2Image);
                    }
                }
                //mostrar OVNIS azules en ovniEatingMode
                else if (GameModel.isOvniEatingMode()) {
                    if (row == model.getOvniLocation().getX() && column == model.getOvniLocation().getY()) {
                        this.cellViews[row][column].setImage(this.ovnidestructibleImage);
                    }
                    if (row == model.getOvni2Location().getX() && column == model.getOvni2Location().getY()) {
                        this.cellViews[row][column].setImage(this.ovnidestructibleImage);
                    }
                }
                //display imágenes OVNIS regulares de lo contrario
                else {
                    if (row == model.getOvniLocation().getX() && column == model.getOvniLocation().getY()) {
                        this.cellViews[row][column].setImage(this.ovniImage);
                    }
                    if (row == model.getOvni2Location().getX() && column == model.getOvni2Location().getY()) {
                        this.cellViews[row][column].setImage(this.ovni2Image);
                    }
                }
            }
        }
    }

    public int getRowCount() {
        return this.rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
        this.initializeGrid();
    }

    public int getColumnCount() {
        return this.columnCount;
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
        this.initializeGrid();
    }
}

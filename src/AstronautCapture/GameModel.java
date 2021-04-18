package AstronautCapture;

import javafx.geometry.Point2D;

import javafx.fxml.FXML;
import java.io.*;

import java.util.*;

public class GameModel {
    public enum CellValue {
        EMPTY, ALIENEGG, PISTOLA, WALL, ovniHOME, ovni2HOME, ASTRONAUTAHOME
    };
    public enum Direction {
        UP, DOWN, LEFT, RIGHT, NONE
    };
    @FXML private int rowCount;
    @FXML private int columnCount;
    private CellValue[][] grid;
    private int score;
    private int level;
    private int dotCount;
    private static boolean gameOver;
    private static boolean youWon;
    private static boolean ovniEatingMode;
    private Point2D astronautaLocation;
    private Point2D astronautaVelocity;
    private Point2D ovniLocation;
    private Point2D ovniVelocity;
    private Point2D ovni2Location;
    private Point2D ovni2Velocity;
    private static Direction lastDirection;
    private static Direction currentDirection;

    /**
     * Iniciar un nuevo juego al inicializar
     */
    public GameModel() {
        this.startNewGame();
    }

    /**
     * Configure los CellValues de la cuadrícula según el archivo txt y coloque el personaje y el OVNI en sus ubicaciones iniciales.
     * "W" indica una pared, "E" indica un cuadrado vacío, "B" indica un punto grande, "S" indica
     * un pequeño punto, "1" o "2" indica el hogar del OVNI, y "P" indica la posición inicial del personaje.
     *
     * @param fileName txt archivo que contiene la configuración de la placa
     */
    public void initializeLevel(String fileName) {
        File file = new File(fileName);
        Scanner scanner = null;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            Scanner lineScanner = new Scanner(line);
            while (lineScanner.hasNext()) {
                lineScanner.next();
                columnCount++;
            }
            rowCount++;
        }
        columnCount = columnCount/rowCount;
        Scanner scanner2 = null;
        try {
            scanner2 = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        grid = new CellValue[rowCount][columnCount];
        int row = 0;
        int astronautaRow = 0;
        int astronautaColumn = 0;
        int ovniRow = 0;
        int ovniColumn = 0;
        int ovni2Row = 0;
        int ovni2Column = 0;
        while(scanner2.hasNextLine()){
            int column = 0;
            String line= scanner2.nextLine();
            Scanner lineScanner = new Scanner(line);
            while (lineScanner.hasNext()){
                String value = lineScanner.next();
                CellValue thisValue;
                if (value.equals("W")){
                    thisValue = CellValue.WALL;
                }
                else if (value.equals("S")){
                    thisValue = CellValue.ALIENEGG;
                    dotCount++;
                }
                else if (value.equals("B")){
                    thisValue = CellValue.PISTOLA;
                    dotCount++;
                }
                else if (value.equals("1")){
                    thisValue = CellValue.ovniHOME;
                    ovniRow = row;
                    ovniColumn = column;
                }
                else if (value.equals("2")){
                    thisValue = CellValue.ovni2HOME;
                    ovni2Row = row;
                    ovni2Column = column;
                }
                else if (value.equals("P")){
                    thisValue = CellValue.ASTRONAUTAHOME;
                    astronautaRow = row;
                    astronautaColumn = column;
                }
                else //(value.equals("E"))
                {
                    thisValue = CellValue.EMPTY;
                }
                grid[row][column] = thisValue;
                column++;
            }
            row++;
        }
        astronautaLocation = new Point2D(astronautaRow, astronautaColumn);
        astronautaVelocity = new Point2D(0,0);
        ovniLocation = new Point2D(ovniRow,ovniColumn);
        ovniVelocity = new Point2D(-1, 0);
        ovni2Location = new Point2D(ovni2Row,ovni2Column);
        ovni2Velocity = new Point2D(-1, 0);
        currentDirection = Direction.NONE;
        lastDirection = Direction.NONE;
    }

    /** Inicializar valores de variables de instancia e inicializar mapa de nivel
     */
    public void startNewGame() {
        this.gameOver = false;
        this.youWon = false;
        this.ovniEatingMode = false;
        dotCount = 0;
        rowCount = 0;
        columnCount = 0;
        this.score = 0;
        this.level = 1;
        this.initializeLevel(Controller.getLevelFile(0));
    }

    /** Inicializar el mapa de nivel para el siguiente nivel
     *
     */
    public void startNextLevel() {
        if (this.isLevelComplete()) {
            this.level++;
            rowCount = 0;
            columnCount = 0;
            youWon = false;
            ovniEatingMode = false;
            try {
                this.initializeLevel(Controller.getLevelFile(level - 1));
            }
            catch (ArrayIndexOutOfBoundsException e) {
                //si no quedan niveles en la matriz de niveles, el juego termina
                youWon = true;
                gameOver = true;
                level--;
            }
        }
    }

    /**
     * Mueva el personaje según la dirección indicada por el usuario (según la entrada del teclado desde el controlador)
     * @param direction la dirección ingresada más recientemente para que el personaje se mueva
     */
    public void moveAstronauta(Direction direction) {
        Point2D potentialastronautaVelocity = changeVelocity(direction);
        Point2D potentialastronautaLocation = astronautaLocation.add(potentialastronautaVelocity);
        //si el personaje sale de la pantalla, envuélvalo
        potentialastronautaLocation = setGoingOffscreenNewLocation(potentialastronautaLocation);
        //determinar si el personaje debe cambiar de dirección o continuar en su dirección más reciente
        //si la entrada de dirección más reciente es la misma que la entrada de dirección anterior, verifique si hay paredes
        if (direction.equals(lastDirection)) {
            //si moverse en la misma dirección daría como resultado golpear una pared, deje de moverse
            if (grid[(int) potentialastronautaLocation.getX()][(int) potentialastronautaLocation.getY()] == CellValue.WALL){
                astronautaVelocity = changeVelocity(Direction.NONE);
                setLastDirection(Direction.NONE);
            }
            else {
                astronautaVelocity = potentialastronautaVelocity;
                astronautaLocation = potentialastronautaLocation;
            }
        }
        //si la entrada de dirección más reciente no es la misma que la entrada anterior, verifique si hay paredes y esquinas antes de ir en una nueva dirección.
        else {
            //si el personaje choca contra una pared con la nueva entrada de dirección, verifique que no choca contra una pared diferente si continúa en su dirección anterior.
            if (grid[(int) potentialastronautaLocation.getX()][(int) potentialastronautaLocation.getY()] == CellValue.WALL){
                potentialastronautaVelocity = changeVelocity(lastDirection);
                potentialastronautaLocation = astronautaLocation.add(potentialastronautaVelocity);
                //si cambiar de dirección choca contra otra pared, deja de moverte
                if (grid[(int) potentialastronautaLocation.getX()][(int) potentialastronautaLocation.getY()] == CellValue.WALL){
                    astronautaVelocity = changeVelocity(Direction.NONE);
                    setLastDirection(Direction.NONE);
                }
                else {
                    astronautaVelocity = changeVelocity(lastDirection);
                    astronautaLocation = astronautaLocation.add(astronautaVelocity);
                }
            }
            //de lo contrario, cambia de dirección y sigue moviéndote
            else {
                astronautaVelocity = potentialastronautaVelocity;
                astronautaLocation = potentialastronautaLocation;
                setLastDirection(direction);
            }
        }
    }

    /**
     * Mueva el OVNI para seguir al personaje como se establece en el metodo moveOvni ()
     */
    public void moveOvni() {
        Point2D[] ovniData = moveAOvni(ovniVelocity, ovniLocation);
        Point2D[] ovni2Data = moveAOvni(ovni2Velocity, ovni2Location);
        ovniVelocity = ovniData[0];
        ovniLocation = ovniData[1];
        ovni2Velocity = ovni2Data[0];
        ovni2Location = ovni2Data[1];

    }

    /**
     * Mueva un OVNI para seguir al personaje si está en la misma fila o columna, o aléjese del personaje si está en ovniEatingMode; de lo contrario, muévase aleatoriamente cuando golpee una pared.
     * @param velocity la velocidad actual del fantasma especificado
     * @param location la ubicación actual del fantasma especificado
     * @return una array de Point2D que contiene una nueva velocidad y ubicación para el OVNI
     */
    public Point2D[] moveAOvni(Point2D velocity, Point2D location){
        Random generator = new Random();
        //si el OVNI está en la misma fila o columna que el personaje y no en ovniEatingMode,
        // ve en su dirección hasta que llegues a una pared, luego ve en una dirección diferente
        // de lo contrario, vaya en una dirección aleatoria, y si golpea una pared, vaya en una dirección aleatoria diferente
        if (!ovniEatingMode) {
            //check if ghost is in PacMan's column and move towards him
            if (location.getY() == astronautaLocation.getY()) {
                if (location.getX() > astronautaLocation.getX()) {
                    velocity = changeVelocity(Direction.UP);
                } else {
                    velocity = changeVelocity(Direction.DOWN);
                }
                Point2D potentialLocation = location.add(velocity);
                //si el OVNI saldría de la pantalla, envuélvete
                potentialLocation = setGoingOffscreenNewLocation(potentialLocation);
                //generar nuevas direcciones aleatorias hasta que el OVNI pueda moverse sin golpear una pared
                while (grid[(int) potentialLocation.getX()][(int) potentialLocation.getY()] == CellValue.WALL) {
                    int randomNum = generator.nextInt(4);
                    Direction direction = intToDirection(randomNum);
                    velocity = changeVelocity(direction);
                    potentialLocation = location.add(velocity);
                }
                location = potentialLocation;
            }
            //comprueba si el OVNI está en la fila del personaje y muévete hacia él
            else if (location.getX() == astronautaLocation.getX()) {
                if (location.getY() > astronautaLocation.getY()) {
                    velocity = changeVelocity(Direction.LEFT);
                } else {
                    velocity = changeVelocity(Direction.RIGHT);
                }
                Point2D potentialLocation = location.add(velocity);
                potentialLocation = setGoingOffscreenNewLocation(potentialLocation);
                while (grid[(int) potentialLocation.getX()][(int) potentialLocation.getY()] == CellValue.WALL) {
                    int randomNum = generator.nextInt(4);
                    Direction direction = intToDirection(randomNum);
                    velocity = changeVelocity(direction);
                    potentialLocation = location.add(velocity);
                }
                location = potentialLocation;
            }
            //muévase en una dirección aleatoria consistente hasta que golpee una pared, luego elija una nueva dirección aleatoria
            else{
                Point2D potentialLocation = location.add(velocity);
                potentialLocation = setGoingOffscreenNewLocation(potentialLocation);
                while(grid[(int) potentialLocation.getX()][(int) potentialLocation.getY()] == CellValue.WALL){
                    int randomNum = generator.nextInt( 4);
                    Direction direction = intToDirection(randomNum);
                    velocity = changeVelocity(direction);
                    potentialLocation = location.add(velocity);
                }
                location = potentialLocation;
            }
        }
        //si el OVNI está en la misma fila o columna que el personaje y en ovniEatingMode, ve en la dirección opuesta
        // hasta que golpee una pared, luego vaya en una dirección diferente
        // de lo contrario, vaya en una dirección aleatoria, y si golpea una pared, vaya en una dirección aleatoria diferente
        if (ovniEatingMode) {
            if (location.getY() == astronautaLocation.getY()) {
                if (location.getX() > astronautaLocation.getX()) {
                    velocity = changeVelocity(Direction.DOWN);
                } else {
                    velocity = changeVelocity(Direction.UP);
                }
                Point2D potentialLocation = location.add(velocity);
                potentialLocation = setGoingOffscreenNewLocation(potentialLocation);
                while (grid[(int) potentialLocation.getX()][(int) potentialLocation.getY()] == CellValue.WALL) {
                    int randomNum = generator.nextInt(4);
                    Direction direction = intToDirection(randomNum);
                    velocity = changeVelocity(direction);
                    potentialLocation = location.add(velocity);
                }
                location = potentialLocation;
            } else if (location.getX() == astronautaLocation.getX()) {
                if (location.getY() > astronautaLocation.getY()) {
                    velocity = changeVelocity(Direction.RIGHT);
                } else {
                    velocity = changeVelocity(Direction.LEFT);
                }
                Point2D potentialLocation = location.add(velocity);
                potentialLocation = setGoingOffscreenNewLocation(potentialLocation);
                while (grid[(int) potentialLocation.getX()][(int) potentialLocation.getY()] == CellValue.WALL) {
                    int randomNum = generator.nextInt(4);
                    Direction direction = intToDirection(randomNum);
                    velocity = changeVelocity(direction);
                    potentialLocation = location.add(velocity);
                }
                location = potentialLocation;
            }
            else{
                Point2D potentialLocation = location.add(velocity);
                potentialLocation = setGoingOffscreenNewLocation(potentialLocation);
                while(grid[(int) potentialLocation.getX()][(int) potentialLocation.getY()] == CellValue.WALL){
                    int randomNum = generator.nextInt( 4);
                    Direction direction = intToDirection(randomNum);
                    velocity = changeVelocity(direction);
                    potentialLocation = location.add(velocity);
                }
                location = potentialLocation;
            }
        }
        Point2D[] data = {velocity, location};
        return data;

    }


    /**
     * Envuelva el tablero de juego si la ubicación del objeto estaría fuera de la pantalla
     * @param objectLocation la ubicación del objeto especificado
     * @return Nueva ubicación envolvente de Point2D
     */
    public Point2D setGoingOffscreenNewLocation(Point2D objectLocation) {
        //si el objeto sale de la pantalla a la derecha
        if (objectLocation.getY() >= columnCount) {
            objectLocation = new Point2D(objectLocation.getX(), 0);
        }
        //si el objeto sale de la pantalla a la izquierda
        if (objectLocation.getY() < 0) {
            objectLocation = new Point2D(objectLocation.getX(), columnCount - 1);
        }
        return objectLocation;
    }

    /**
     * Conecta cada dirección a un número entero 0-3
     * @param x un integer
     * @return la dirección correspondiente
     */
    public Direction intToDirection(int x){
        if (x == 0){
            return Direction.LEFT;
        }
        else if (x == 1){
            return Direction.RIGHT;
        }
        else if(x == 2){
            return Direction.UP;
        }
        else{
            return Direction.DOWN;
        }
    }

    /**
     * Restablece la ubicación y la velocidad del OVNI a su estado de origen
     */
    public void sendovniHome() {
        for (int row = 0; row < this.rowCount; row++) {
            for (int column = 0; column < this.columnCount; column++) {
                if (grid[row][column] == CellValue.ovniHOME) {
                    ovniLocation = new Point2D(row, column);
                }
            }
        }
        ovniVelocity = new Point2D(-1, 0);
    }

    /**
     * Restablece la ubicación y la velocidad de OVNI2 a su estado de origen
     */
    public void sendovni2Home() {
        for (int row = 0; row < this.rowCount; row++) {
            for (int column = 0; column < this.columnCount; column++) {
                if (grid[row][column] == CellValue.ovni2HOME) {
                    ovni2Location = new Point2D(row, column);
                }
            }
        }
        ovni2Velocity = new Point2D(-1, 0);
    }

    /**
     * Actualiza el modelo para reflejar el movimiento del personaje y los OVNIS y el cambio en el estado de cualquier objeto comido
     * durante el curso de estos movimientos. Cambia el estado del juego desde el modo de comer fantasmas.
     * @param direction la dirección ingresada más recientemente para que el personaje se mueva
     */
    public void step(Direction direction) {
        this.moveAstronauta(direction);
        //si el personaje está en un punto pequeño, elimine el punto pequeño
        CellValue astronautaLocationCellValue = grid[(int) astronautaLocation.getX()][(int) astronautaLocation.getY()];
        if (astronautaLocationCellValue == CellValue.ALIENEGG) {
            grid[(int) astronautaLocation.getX()][(int) astronautaLocation.getY()] = CellValue.EMPTY;
            dotCount--;
            score += 10;
        }
        //si el personaje está en un punto grande, elimine el punto grande y cambie el estado del juego al modo de comer OVNI e inicialice el contador
        if (astronautaLocationCellValue == CellValue.PISTOLA) {
            grid[(int) astronautaLocation.getX()][(int) astronautaLocation.getY()] = CellValue.EMPTY;
            dotCount--;
            score += 50;
            ovniEatingMode = true;
            Controller.setOvniEatingModeCounter();
        }
        //enviar OVNI de vuelta a ovnihome si el personaje está en un OVNI en el modo de comer ONVI
        if (ovniEatingMode) {
            if (astronautaLocation.equals(ovniLocation)) {
                sendovniHome();
                score += 100;
            }
            if (astronautaLocation.equals(ovni2Location)) {
                sendovni2Home();
                score += 100;
            }
        }
        //el juego termina si el personaje es abducido por un OVNI
        else {
            if (astronautaLocation.equals(ovniLocation)) {
                gameOver = true;
                astronautaVelocity = new Point2D(0,0);
            }
            if (astronautaLocation.equals(ovni2Location)) {
                gameOver = true;
                astronautaVelocity = new Point2D(0,0);
            }
        }
        //mueve OVNI y comprueba de nuevo si se comen OVNI o el personaje (repetir estas comprobaciones ayuda a tener en cuenta los números pares/impares de cuadrados entre OVNI y el personaje)
        this.moveOvni();
        if (ovniEatingMode) {
            if (astronautaLocation.equals(ovniLocation)) {
                sendovniHome();
                score += 100;
            }
            if (astronautaLocation.equals(ovni2Location)) {
                sendovni2Home();
                score += 100;
            }
        }
        else {
            if (astronautaLocation.equals(ovniLocation)) {
                gameOver = true;
                astronautaVelocity = new Point2D(0,0);
            }
            if (astronautaLocation.equals(ovni2Location)) {
                gameOver = true;
                astronautaVelocity = new Point2D(0,0);
            }
        }
        //comenzar un nuevo nivel si el nivel está completo
        if (this.isLevelComplete()) {
            astronautaVelocity = new Point2D(0,0);
            startNextLevel();
        }
    }

    /**
     * Conecta cada dirección a los vectores de velocidad Point2D (Izquierda = (-1,0), Derecha = (1,0), Arriba = (0, -1), Abajo = (0,1))
     * @param direction
     * @return Vector de velocidad Point2D
     */
    public Point2D changeVelocity(Direction direction){
        if(direction == Direction.LEFT){
            return new Point2D(0,-1);
        }
        else if(direction == Direction.RIGHT){
            return new Point2D(0,1);
        }
        else if(direction == Direction.UP){
            return new Point2D(-1,0);
        }
        else if(direction == Direction.DOWN){
            return new Point2D(1,0);
        }
        else{
            return new Point2D(0,0);
        }
    }

    public static boolean isOvniEatingMode() {
        return ovniEatingMode;
    }

    public static void setOvniEatingMode(boolean ovniEatingModeBool) {
        ovniEatingMode = ovniEatingModeBool;
    }

    public static boolean isYouWon() {
        return youWon;
    }

    /**
     * Cuando se comen todos los puntos, el nivel está completo
     * @return boolean
     */
    public boolean isLevelComplete() {
        return this.dotCount == 0;
    }

    public static boolean isGameOver() {
        return gameOver;
    }

    public CellValue[][] getGrid() {
        return grid;
    }

    /**
     * @param row
     * @param column
     * @return el valor de celda de la celda (fila, columna)
     */
    public CellValue getCellValue(int row, int column) {
        assert row >= 0 && row < this.grid.length && column >= 0 && column < this.grid[0].length;
        return this.grid[row][column];
    }

    public static Direction getCurrentDirection() {
        return currentDirection;
    }

    public void setCurrentDirection(Direction direction) {
        currentDirection = direction;
    }

    public static Direction getLastDirection() {
        return lastDirection;
    }

    public void setLastDirection(Direction direction) {
        lastDirection = direction;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    /**
     * Agregar nuevos puntos a la puntuación
     * @param points
     */
    public void addToScore(int points) {
        this.score += points;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * @return el número total de puntos que quedan (grandes y pequeños)
     */
    public int getDotCount() {
        return dotCount;
    }

    public void setDotCount(int dotCount) {
        this.dotCount = dotCount;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }

    public Point2D getAstronautaLocation() {
        return astronautaLocation;
    }

    public void setAstronautaLocation(Point2D astronautaLocation) {
        this.astronautaLocation = astronautaLocation;
    }

    public Point2D getOvniLocation() {
        return ovniLocation;
    }

    public void setOvniLocation(Point2D ovniLocation) {
        this.ovniLocation = ovniLocation;
    }

    public Point2D getOvni2Location() {
        return ovni2Location;
    }

    public void setOvni2Location(Point2D ovni2Location) {
        this.ovni2Location = ovni2Location;
    }

    public Point2D getAstronautaVelocity() {
        return astronautaVelocity;
    }

    public void setAstronautaVelocity(Point2D velocity) {
        this.astronautaVelocity = velocity;
    }

    public Point2D getOvniVelocity() {
        return ovniVelocity;
    }

    public void setOvniVelocity(Point2D ovniVelocity) {
        this.ovniVelocity = ovniVelocity;
    }

    public Point2D getOvni2Velocity() {
        return ovni2Velocity;
    }

    public void setOvni2Velocity(Point2D ovni2Velocity) {
        this.ovni2Velocity = ovni2Velocity;
    }
}

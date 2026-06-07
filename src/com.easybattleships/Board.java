package com.easybattleships;

public class Board {
    public static final int SIZE = 16;

    // На своём поле видно корабли
    private char[][] ownGrid;

    // На чужом поле их естественно видно не будет (
    private char[][] hiddenGrid;

    public Board(){
        // Инициализация полей
        ownGrid = new char[SIZE][SIZE];
        hiddenGrid = new char[SIZE][SIZE];
        for (int row = 0; row < SIZE; row++){
            for (int col = 0; col < SIZE; col++){
                ownGrid[row][col] = '.';
                hiddenGrid[row][col] = '.';
            }
        }
    }

    // Отметить клетку корабля на поле
    public void placeShipCell(int row, int col) {
        ownGrid[row][col] = 'O';
    }

    // Выстрелили по нашему полю, получили true при попадении
    public boolean receiveShot(int row, int col) {
        if (ownGrid[row][col] == 'O') {
            ownGrid[row][col] = 'X';
            return true;
        } else {
            ownGrid[row][col] = '*';
            return false;
        }
    }

    // Отмечаем результат выстрела
    public void markHiddenGrid(int row, int col, boolean hit) {
        hiddenGrid[row][col] = hit ? 'X' : '*';
    }

    // Проверить была ли клетка уже отмечена выстрелом
    public boolean isAlreadyShot(int row, int col) {
        return ownGrid[row][col] == 'X' || ownGrid[row][col] == '*';
    }

    // Хз что здесь можно комментировать
    public char getCell(int row, int col) {
        return ownGrid[row][col];
    }

    // Печатаем своё поле
    public void printOwn() {
        printHeader();
        for (int row = 0; row < SIZE; row++) {
            System.out.printf("%2d ", row + 1);
            for (int col = 0; col < SIZE; col++) {
                System.out.print(ownGrid[row][col] + "  ");
            }
            System.out.println();
        }
    }

    // Печатает поле противника
    public void printHidden() {
        printHeader();
        for (int row = 0; row < SIZE; row++) {
            System.out.printf("%2d ", row + 1);
            for (int col = 0; col < SIZE; col++) {
                System.out.print(hiddenGrid[row][col] + "  ");
            }
            System.out.println();
        }
    }

    // Пусть пока функции отдельных полей тоже останутся но наверное лучше печатать одной этой
    public void printBothGrids(){
        System.out.println("Ваше поле" + " ".repeat((46)) + "Поле противника");
        printDoubleHeader();
        for (int row = 0; row < SIZE; row++) {
            System.out.printf("%2d ", row + 1);
            for (int col = 0; col < SIZE; col++) {
                System.out.print(ownGrid[row][col] + "  ");
            }
            System.out.print("    ");
            System.out.printf("%2d ", row + 1);
            for (int col = 0; col < SIZE; col++) {
                System.out.print(hiddenGrid[row][col] + "  ");
            }
            System.out.println();
        }
    }

    // Шапка с буквами A-P
    private void printHeader() {
        System.out.print("   ");
        for (int col = 0; col < SIZE; col++) {
            // Жёсткая хитрость для перебора буков
            System.out.print((char)('A' + col) + "  ");
        }
        System.out.println();
    }

    // Шапка для горизонтального представления двух полей
    private void printDoubleHeader(){
        System.out.print("   ");
        for (int col = 0; col < SIZE; col++) {
            System.out.print((char)('A' + col) + "  ");
        }
        System.out.print("       ");
        for (int col = 0; col < SIZE; col++) {
            // Вторая часть шапки
            System.out.print((char)('A' + col) + "  ");
        }
        System.out.println();
    }

    // Размещаем корабль (вернёт false если такое размещение запрещено)
    public boolean placeShip(Ship ship) {
        if (!isValidPlacement(ship)) {
            return false;
        }
        // Отмечаем все клетки корабля на поле
        for (int[] cell : ship.getCells()) {
            placeShipCell(cell[0], cell[1]);
        }
        return true;
    }

    // Проверяем может ли корабль выходить за границы поля
    public boolean isValidPlacement(Ship ship) {
        for (int[] cell : ship.getCells()) {
            int row = cell[0];
            int col = cell[1];

            // Выход за границы поля
            if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) {
                return false;
            }

            // Проверяем все 8 соседних клеток вокруг каждой клетки корабля
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    int nr = row + dr;
                    int nc = col + dc;

                    // Выход за поле уже прочитан
                    if (nr < 0 || nr >= SIZE || nc < 0 || nc >= SIZE) continue;

                    // Какая-то из соседних клеток пересеклась с уже установленным кораблём, так нельзя
                    if (ownGrid[nr][nc] == 'O') return false;
                }
            }
        }
        return true;
    }




}

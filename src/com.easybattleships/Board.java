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



}

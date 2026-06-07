package com.easybattleships;

public class Game {
    public void start() {
        // Тестирование самых простых обработок на поле
        System.out.println("Добро пожаловать в Морской бой!");

        Board board = new Board();
        board.placeShipCell(0, 0); // A1
        board.placeShipCell(0, 1); // B1

        board.receiveShot(3, 5);   // промах
        board.receiveShot(0, 0);   // попадание

        board.printBothGrids();
    }
}


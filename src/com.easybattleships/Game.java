package com.easybattleships;

public class Game {
    public void start() {
        Board board = new Board();

        Ship ship1 = new Ship(0, 0, 3, Ship.Direction.HORIZONTAL);
        Ship ship2 = new Ship(0, 2, 2, Ship.Direction.VERTICAL); // нарушает зону

        System.out.println("Корабль 1: " + board.placeShip(ship1)); // должно отдать true
        System.out.println("Корабль 2: " + board.placeShip(ship2)); // размещение харам
        board.printBothGrids();
    }
}


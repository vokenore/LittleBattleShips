package com.easybattleships;

import java.util.ArrayList;
import java.util.List;

public class Player {

    private String name;
    private Board board;
    private List<Ship> ships;

    public Player(String name) {
        this.name = name;
        this.board = new Board();
        this.ships = new ArrayList<>();
    }

    public void addShip(Ship ship) {
        ships.add(ship);
    }

    // Все корабли потоплены - грустность
    public boolean isDefeated() {
        for (Ship ship : ships) {
            if (!ship.isSunk()) return false;
        }
        return true;
    }

    // Обработка выстрела по игроку - возвращает "miss", "hit", "sunk"
    public String receiveShot(int row, int col) {
        for (Ship ship : ships) {
            if (ship.hit(row, col)) {
                board.receiveShot(row, col);
                if (ship.isSunk()) return "sunk";
                return "hit";
            }
        }
        board.receiveShot(row, col);
        return "miss";
    }

    public String getName() { return name; }
    public Board getBoard() { return board; }
    public List<Ship> getShips() { return ships; }

}

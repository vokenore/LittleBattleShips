package com.easybattleships;

import java.util.ArrayList;
import java.util.List;

public class Ship {

    // Направление корабля
    public enum Direction {
        HORIZONTAL, VERTICAL
    }

    private int startRow;
    private int startCol;
    private int size;
    private Direction direction;

    // Клетки корабля, каждая int[]{row, col}
    private List<int[]> cells;

    // Поражённые клетки
    private List<int[]> hitCells;

    public Ship(int startRow, int startCol, int size, Direction direction) {
        this.startRow = startRow;
        this.startCol = startCol;
        this.size = size;
        this.direction = direction;
        this.cells = new ArrayList<>();
        this.hitCells = new ArrayList<>();
        buildShip();
    }

    // Строим список клеток корабля по стартовой точке и направлению
    private void buildShip() {
        for (int i = 0; i < size; i++) {
            if (direction == Direction.HORIZONTAL) {
                cells.add(new int[]{startRow, startCol + i});
            } else {
                cells.add(new int[]{startRow + i, startCol});
            }
        }
    }

    // Попытка выстрела в клетку (возвращает true если попали)
    public boolean hit(int row, int col) {
        for (int[] cell : cells) {
            if (cell[0] == row && cell[1] == col) {
                hitCells.add(new int[]{row, col});
                return true;
            }
        }
        return false;
    }

    // Все клетки поражены = корабль всё = ура
    public boolean isSunk() {
        return hitCells.size() == size;
    }

    // Проверка занимает ли кто-то эту клетку
    public boolean occupies(int row, int col) {
        for (int[] cell : cells) {
            if (cell[0] == row && cell[1] == col) return true;
        }
        return false;
    }

    public List<int[]> getCells() { return cells; }
    public int getSize() { return size; }
}
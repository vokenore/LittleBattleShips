package com.littlebattleships;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class BotPlayer {

    private enum Mode {
        SEARCH,   // режим для поиска новых кораблей
        HUNT      // режим добивания найденного
    }

    private Mode mode = Mode.SEARCH;

    // Список клеток для поиска
    private List<int[]> searchCells;

    // Очередь целей для добивания
    private Queue<int[]> huntQueue;

    // Первая клетка в которую попал бот
    private int[] firstHit;

    // Все поражённые клетки текущего корабля который надо добить
    private List<int[]> currentHits;

    public BotPlayer() {
        searchCells = buildSearchPattern();
        huntQueue = new LinkedList<>();
        currentHits = new ArrayList<>();
    }

    //Будем стрелять в каждую вторую клетку, по "шахматному паттерну"
    private List<int[]> buildSearchPattern() {
        List<int[]> cells = new ArrayList<>();
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                if ((row + col) % 2 == 0) {
                    cells.add(new int[]{row, col});
                }
            }
        }
        // Немного сильнее рандомизируем
        Collections.shuffle(cells);
        return cells;
    }

    // Возвращает координаты слдующего выстрела
    public int[] nextShot(Board enemyBoard) {
        if (mode == Mode.HUNT) {
            return huntShot(enemyBoard);
        } else {
            return searchShot(enemyBoard);
        }
    }

    // Следующий выстрел по шахматному паттерну
    private int[] searchShot(Board enemyBoard) {
        while (!searchCells.isEmpty()) {
            int[] cell = searchCells.remove(0);
            if (!enemyBoard.isAlreadyShot(cell[0], cell[1])) {
                return cell;
            }
        }
        // Шахматный паттерн закончился, стреляем в любую свободную
        return fallbackShot(enemyBoard);
    }

    // Выстрел в первую свободную клетку
    private int[] fallbackShot(Board enemyBoard) {
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                if (!enemyBoard.isAlreadyShot(row, col)) {
                    return new int[]{row, col};
                }
            }
        }
        return null;
    }

    // Режим добивания
    private int[] huntShot(Board enemyBoard) {
        // Из очереди убираем уже обстреленные клетки
        while (!huntQueue.isEmpty()) {
            int[] cell = huntQueue.peek();
            if (enemyBoard.isAlreadyShot(cell[0], cell[1])) {
                huntQueue.poll();
            } else {
                break;
            }
        }

        if (!huntQueue.isEmpty()) {
            return huntQueue.poll();
        }

        // Если очередь по какой-то причине опустела - возвращаемся к поиску
        mode = Mode.SEARCH;
        return searchShot(enemyBoard);
    }

    // Вызываем после каждого выстрела, обновляем состояние бота
    public void processResult(int row, int col, String result, Board enemyBoard) {
        if (result.equals("miss")) {
            return;
        }

        if (result.equals("hit")) {
            currentHits.add(new int[]{row, col});

            if (mode == Mode.SEARCH) {
                // Первое попадание, переходим в режим добивания
                firstHit = new int[]{row, col};
                mode = Mode.HUNT;
                addNeighbors(row, col, enemyBoard);
            } else {
                // Уже идёт добивание - уточняем направление
                refineHuntDirection(row, col, enemyBoard);
            }
        }

        if (result.equals("sunk")) {
            // Корабль затоплен - возвращаемся к поиску
            currentHits.clear();
            huntQueue.clear();
            firstHit = null;
            mode = Mode.SEARCH;
        }
    }

    // Добавляем 4 соседних клетки в очередь добивания
    private void addNeighbors(int row, int col, Board enemyBoard) {
        int[][] directions = {{-1,0},{1,0},{0,-1},{0,1}};
        for (int[] dir : directions) {
            int nr = row + dir[0];
            int nc = col + dir[1];
            if (isValid(nr, nc) && !enemyBoard.isAlreadyShot(nr, nc)) {
                huntQueue.add(new int[]{nr, nc});
            }
        }
    }

    // Получили второе попадание - знаем направление корабля (держим курс в этом направлении)
    private void refineHuntDirection(int row, int col, Board enemyBoard) {
        huntQueue.clear();

        // Определяем направление по первому и текущему попаданию
        boolean horizontal = (row == firstHit[0]);

        if (horizontal) {
            // Ищем минимальную и максимальную колонку среди попаданий
            int minCol = col, maxCol = col;
            for (int[] hit : currentHits) {
                minCol = Math.min(minCol, hit[1]);
                maxCol = Math.max(maxCol, hit[1]);
            }
            // Добавляем клетки слева и справа от цепочки
            if (minCol - 1 >= 0 && !enemyBoard.isAlreadyShot(row, minCol - 1)) {
                huntQueue.add(new int[]{row, minCol - 1});
            }
            if (maxCol + 1 < Board.SIZE && !enemyBoard.isAlreadyShot(row, maxCol + 1)) {
                huntQueue.add(new int[]{row, maxCol + 1});
            }
        } else {
            // Теперь вертикально
            int minRow = row, maxRow = row;
            for (int[] hit : currentHits) {
                minRow = Math.min(minRow, hit[0]);
                maxRow = Math.max(maxRow, hit[0]);
            }
            if (minRow - 1 >= 0 && !enemyBoard.isAlreadyShot(minRow - 1, col)) {
                huntQueue.add(new int[]{minRow - 1, col});
            }
            if (maxRow + 1 < Board.SIZE && !enemyBoard.isAlreadyShot(maxRow + 1, col)) {
                huntQueue.add(new int[]{maxRow + 1, col});
            }
        }
    }

    private boolean isValid(int row, int col) {
        return row >= 0 && row < Board.SIZE && col >= 0 && col < Board.SIZE;
    }
}
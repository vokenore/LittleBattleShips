package com.easybattleships;

import java.util.Scanner;

public class ShipPlacer {

    // Сколько кораблей каждого размера нужно расставить
    // Индекс = размер корабля (индекс 0 просто скипаем)
    private static final int[] SHIP_COUNTS = {0, 6, 5, 4, 3, 2, 1};

    public static void placeShips(Player player, Scanner scanner) {
        Board board = player.getBoard();
        System.out.println("\n=== Расстановка кораблей: " + player.getName() + " ===");
        System.out.println("Формат ввода: [колонка][строка] [H/V]");
        System.out.println("(Для корабля длинной 1 не нужно укзаывать направление)");
        System.out.println("Пример: A1 H (корабль установить горизонтально от A1)");
        System.out.println("Введите 'auto' для автоматической расстановки");
        System.out.println("Введите 'allauto' для автоматической расстановки всех кораблей\n");

        boolean autoPlacement = false;

        // Идём от большого корабля к маленькому
        for (int size = 6; size >= 1; size--) {
            int count = SHIP_COUNTS[size];

            for (int i = 0; i < count; i++) {

                // Автоматическая расстановка всех оставшихся кораблей
                if (autoPlacement){
                    autoPlace(player, size);
                    continue;
                }

                board.printOwn();
                System.out.printf("\nРасставь корабль размером %d (%d из %d):\n",
                        size, i + 1, count);

                boolean placed = false;
                // Пытаемся разместить текущий корабль
                while (!placed) {

                    System.out.print("> ");
                    String input = scanner.nextLine().trim();

                    if (input.equalsIgnoreCase("auto")) {
                        autoPlace(player, size);
                        placed = true;
                        System.out.println("Корабль размещён автоматически.");
                        continue;
                    }

                    // При вводе allauto - автоматическое размещение всех кораблей
                    if (input.equalsIgnoreCase("allauto")) {
                        autoPlace(player, size);
                        placed = true;
                        autoPlacement = true;
                        System.out.println("Начато автоматическое размещение.");
                        continue;
                    }



                    InputParser.ParseResult result;

                    // Чел скажет спасибо за то что ему не придётся выбирать в какую сторону смотрит однопалубник...
                    if (size == 1){
                        result = InputParser.parse(input  + " " + "v" + " " + size);
                    }else {
                        result = InputParser.parse(input + " " + size);
                    }

                    if (result == null) {
                        if (size == 1) {
                            System.out.println("Неверный формат. Пример: A1");
                        } else {
                            System.out.println("Неверный формат. Пример: A1 H");
                        }
                        continue;
                    }

                    if (result.size != size) {
                        System.out.printf("Сейчас нужен корабль размером %d, а не %d\n",
                                size, result.size);
                        continue;
                    }

                    Ship ship = new Ship(result.row, result.col, result.size, result.direction);

                    if (!board.placeShip(ship)) {
                        System.out.println("Нельзя разместить здесь - клетки заняты или слишком близко к другому кораблю.");
                        continue;
                    }

                    player.addShip(ship);
                    placed = true;
                    System.out.println("Корабль размещён");
                }
            }
        }

        board.printOwn();
        System.out.println("\nВсе корабли расставлены!");
    }

    // Автоматическое размещение одного корабля
    public static void autoPlace(Player player, int size) {
        Board board = player.getBoard();
        java.util.Random random = new java.util.Random();

        while (true) {
            int row = random.nextInt(Board.SIZE);
            int col = random.nextInt(Board.SIZE);
            Ship.Direction dir = random.nextBoolean()
                    ? Ship.Direction.HORIZONTAL
                    : Ship.Direction.VERTICAL;

            Ship ship = new Ship(row, col, size, dir);

            if (board.placeShip(ship)) {
                player.addShip(ship);
                return;
            }
        }
    }

    // Расстановка всех кораблей бота
    public static void autoPlaceAll(Player player) {
        for (int size = 6; size >= 1; size--) {
            int count = SHIP_COUNTS[size];
            for (int i = 0; i < count; i++) {
                autoPlace(player, size);
            }
        }
    }
}
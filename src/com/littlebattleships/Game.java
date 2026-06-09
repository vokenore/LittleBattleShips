package com.littlebattleships;

import java.util.Scanner;

public class Game {
    public static final String PASSTOTEST = "bothasonlyoneshipplease";

    private Scanner scanner = new Scanner(System.in);

    public void start() {
        clearConsole();
        System.out.println("=== МОРСКОЙ БОЙ ===\n");

        System.out.print("Введите ваше имя: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) name = "Игрок";

        System.out.println("\nВыберите режим:");
        System.out.println("1. Игра против Чёрой бороды (бот)");
        System.out.println("2. Игра по сети (WebSocket)");
        System.out.print("> ");

        String mode = scanner.nextLine().trim();

        if (mode.equals("1")) {
            startSinglePlayer(name);
        } else if (mode.equals("2")) {
            WebSocketMultiplayerGame ws = new WebSocketMultiplayerGame(scanner);
            ws.start(name);
        } else {
            System.out.println("Неверный выбор.");
        }
    }

    private void startSinglePlayer(String playerName) {
        Player human = new Player(playerName);
        Player bot = new Player("Чёрная борода");

        // Расстановка кораблей игрока
        ShipPlacer.placeShips(human, scanner);

        // Бот расставляет свои корабли
        clearConsole();
        System.out.println("Бот расставляет корабли...");
        ShipPlacer.autoPlaceAll(bot, human.getName().equals(PASSTOTEST));

        gameLoop(human, bot);
    }

    // Главный игровой цикл
    private void gameLoop(Player human, Player bot) {
        // true = ход человека, false = ход бота
        boolean humanTurn = true;

        while (true) {
            clearConsole();
            human.getBoard().printBothGrids();

            if (humanTurn) {
                String result = humanTurn(human, bot);

                // Проверяем не победилил ли мы ещё
                if (bot.isDefeated()) {
                    clearConsole();
                    human.getBoard().printBothGrids();
                    System.out.println("\nВы победили! Все корабли противника потоплены!");
                    break;
                }

                // Промах, передача хода боту
                if (result.equals("miss")) {
                    humanTurn = false;
                }

            } else {
                String result = botTurn(human, bot);

                // Проверяем не проиграли ли мы ещё
                if (human.isDefeated()) {
                    clearConsole();
                    human.getBoard().printBothGrids();
                    System.out.println("\nВы проиграли...");
                    break;
                }

                // Промах, возвращаем ход игроку
                if (result.equals("miss")) {
                    humanTurn = true;
                }
            }
        }
    }

    private String humanTurn(Player human, Player bot) {
        System.out.print("\nВаш удар, задайте цель! (Пример - A5): ");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            int[] coords = InputParser.parseShot(input);

            if (coords == null) {
                System.out.println("Бойцы переглянулись, так и поняв цель... Перепроверьтесь! (Пример: A5)");
                continue;
            }

            int row = coords[0];
            int col = coords[1];

            if (bot.getBoard().isAlreadyShot(row, col)) {
                System.out.println("Что-то со зрением, юнга? Сюда снаряд уже был отправлен!");
                continue;
            }

            // Стреляем
            String result = bot.receiveShot(row, col);
            human.getBoard().markHiddenGrid(row, col, !result.equals("miss"));

            clearConsole();
            human.getBoard().printBothGrids();

            if (result.equals("sunk")) {
                System.out.println("\nУбил!");
            } else if (result.equals("hit")) {
                System.out.println("\nРанил! Давай ещё.");
            } else {
                System.out.println("\nМимо... Готовимся к ответу Чёрной бороды...");
            }

            return result;
        }
    }

    private BotPlayer botPlayer = new BotPlayer();

    private String botTurn(Player human, Player bot) {
        System.out.println("\nХод бота...");
        try { Thread.sleep(1000); } catch (InterruptedException e) {}

        // Бот выбирает клетку
        int[] shot = botPlayer.nextShot(human.getBoard());
        int row = shot[0];
        int col = shot[1];

        String result = human.receiveShot(row, col);

        // Обновляем состояние бота
        botPlayer.processResult(row, col, result, human.getBoard());

        char colChar = (char)('A' + col);
        System.out.printf("Бот выстрелил в %c%d - ", colChar, row + 1);

        if (result.equals("sunk")) {
            System.out.println("убил!");
        } else if (result.equals("hit")) {
            System.out.println("ранил!");
        } else {
            System.out.println("мимо.");
        }

        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        return result;
    }


    public static void clearConsole() {
        // В реальном терминале работает
        System.out.print("\033[H\033[2J");
        System.out.flush();

        // ПЕРЕРАБОТАТЬ!!!!!!!!
    }
}
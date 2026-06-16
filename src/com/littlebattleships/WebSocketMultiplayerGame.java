package com.littlebattleships;

import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class WebSocketMultiplayerGame {

    private static final int DEFAULT_PORT = 8887;
    private Scanner scanner;
    private Player localPlayer;
    private Board opponentBoard;
    private boolean isServer;
    private GameLogger logger;
    private String opponentName;

    // Очередь входящих сообщений
    private BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

    private WebSocketGameServer server;
    private WebSocketGameClient client;

    public WebSocketMultiplayerGame(Scanner scanner) {
        this.scanner = scanner;
        this.opponentBoard = new Board();
    }

    public void start(String playerName) {
        localPlayer = new Player(playerName);

        System.out.println("\nРежим игры по сети:");
        System.out.println("1 - Создать игру");
        System.out.println("2 - Подключиться к игре");
        System.out.print("> ");

        String choice = scanner.nextLine().trim();

        if (choice.equals("1")) {
            startAsServer(playerName);
        } else if (choice.equals("2")) {
            startAsClient(playerName);
        } else {
            System.out.println("Вы ввели что-то не то.");
        }
    }

    // СЕРВЕР

    private void startAsServer(String playerName) {
        isServer = true;

        System.out.print("Введите порт (Enter = " + DEFAULT_PORT + "): ");
        String portInput = scanner.nextLine().trim();
        int port = portInput.isEmpty() ? DEFAULT_PORT : Integer.parseInt(portInput);

        server = new WebSocketGameServer(port, new WebSocketGameServer.MessageHandler() {
            @Override
            public void onPlayerJoined(String name) {
                messageQueue.add("JOINED:" + name);
            }

            @Override
            public void onMessageReceived(String message, boolean fromPlayer1) {
                // Сервер получает сообщения только от player2
                if (!fromPlayer1) {
                    messageQueue.add(message);
                }
            }
        });

        server.start();
        System.out.println("\nСервер запущен на порту " + port);
        System.out.println("Сообщи второму игроку: хост = твой IP, порт = " + port);
        System.out.println("Ждём подключения...\n");

        // Ждём JOIN от второго игрока
        String joinMsg = waitForMessage("JOIN");
        String opponentName = joinMsg.split(":")[1];
        System.out.println("Подключился: " + opponentName);

        // Подтверждаем
        server.sendToPlayer2("JOIN_OK:" + playerName);
        notifyObservers("PLAYER1:" + playerName);
        notifyObservers("PLAYER2:" + opponentName);

        playGame(playerName, opponentName, true);
    }

    // КЛИЕНТ

    private void startAsClient(String playerName) {
        isServer = false;

        System.out.print("Введите хост (Enter = localhost): ");
        String host = scanner.nextLine().trim();
        if (host.isEmpty()) host = "localhost";

        System.out.print("Введите порт (Enter = " + DEFAULT_PORT + "): ");
        String portInput = scanner.nextLine().trim();
        int port = portInput.isEmpty() ? DEFAULT_PORT : Integer.parseInt(portInput);

        // Пробуем подключиться, смотрим кого впускаем
        final boolean[] isObserver = {false};

        try {
            client = new WebSocketGameClient(host, port, new WebSocketGameClient.MessageHandler() {
                @Override
                public void onMessageReceived(String message) {
                    if (message.startsWith("OBSERVER:")) {
                        isObserver[0] = true;
                    }
                    messageQueue.add(message);
                }

                @Override
                public void onConnected() {
                    System.out.println("Подключились к серверу!");
                }

                @Override
                public void onDisconnected() {
                    System.out.println("Игра завершена.");
                }
            });

            client.connectBlocking();

        } catch (Exception e) {
            System.out.println("Не удалось подключиться: " + e.getMessage());
            return;
        }

        try { Thread.sleep(500); } catch (InterruptedException e) {}

        if (isObserver[0]) {
            try { client.closeBlocking(); } catch (Exception e) {}
            System.out.println("Игра уже идёт - подключаетесь как наблюдатель.");
            ObserverMode observer = new ObserverMode(scanner);
            observer.start(host, port);
            return;
        }

        // Второй игрок
        sendMessage("JOIN:" + playerName);
        String joinOk = waitForMessage("JOIN_OK");
        String opponentName = joinOk.split(":")[1];
        System.out.println("Подключились к игре. Соперник: " + opponentName);

        playGame(playerName, opponentName, false);
    }

    // ИГРОВОЙ ПРОЦЕСС

    private void playGame(String myName, String opponentName, boolean myTurnFirst) {
        this.opponentName = opponentName;

        // Расстановка кораблей
        ShipPlacer.placeShips(localPlayer, scanner);
        sendMessage("READY:" + myName);
        System.out.println("Ждём пока противник расставит корабли...");
        waitForMessage("READY");
        logger = new GameLogger(myName, opponentName);
        System.out.println("Оба готовы! Начинаем.\n");

        boolean myTurn = myTurnFirst;

        while (true) {
            Game.clearConsole();
            printBoards(myName, opponentName);

            if (myTurn) {
                String result = doMyShot(myName);

                if (result.equals("sunk") && checkIfOpponentDefeated()) {
                    sendMessage("DEFEATED:" + opponentName);
                    notifyObservers("DEFEATED:" + opponentName);
                    Game.clearConsole();
                    printBoards(myName, opponentName);
                    System.out.println("\nВы победили!");
                    closeConnection();
                    break;
                }

                myTurn = result.equals("miss") ? false : true;

            } else {
                System.out.println("\nХод противника...");
                String result = handleOpponentShot();

                if (localPlayer.isDefeated()) {
                    Game.clearConsole();
                    printBoards(myName, opponentName);
                    System.out.println("\nВы проиграли!");
                    notifyObservers("DEFEATED:" + myName);
                    closeConnection();
                    break;
                }

                myTurn = result.equals("miss") ? true : false;
            }
        }
    }

    private String doMyShot(String myName) {
        while (true) {
            System.out.print("\nВаш ход. Введите координату (Пример - A5): ");
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            int[] coords = InputParser.parseShot(input);

            if (coords == null) {
                System.out.println("Неверный формат.");
                continue;
            }

            int row = coords[0];
            int col = coords[1];

            if (opponentBoard.isAlreadyShotHiddenGrid(row, col)) {
                System.out.println("Вы уже стреляли в эту клетку.");
                continue;
            }

            // Отправляем выстрел
            sendMessage("SHOT:" + row + "," + col);

            // Ждём результата
            String resultMsg = waitForMessage("RESULT");
            String result = resultMsg.split(":")[1];

            opponentBoard.markHiddenGrid(row, col, !result.equals("miss"));

            Game.clearConsole();
            printBoards(myName, "Противник");

            if (result.equals("sunk")) System.out.println("\nУбил!");
            else if (result.equals("hit")) System.out.println("\nРанил!");
            else System.out.println("\nМимо.");

            logger.logMove(myName, row, col, result);
            notifyObservers("SHOT:" + row + "," + col);
            notifyObservers("RESULT:" + result);

            return result;
        }
    }

    private String handleOpponentShot() {
        // Ждём выстрела
        String shotMsg = waitForMessage("SHOT");
        String[] parts = shotMsg.split(":")[1].split(",");
        int row = Integer.parseInt(parts[0]);
        int col = Integer.parseInt(parts[1]);

        // Обрабатываем
        String result = localPlayer.receiveShot(row, col);
        logger.logMove(opponentName, row, col, result);

        // Отправляем результат обратно
        sendMessage("RESULT:" + result);

        char colChar = (char)('A' + col);
        System.out.printf("Противник выстрелил в %c%d - %s%n",
                colChar, row + 1, translateResult(result));

        notifyObservers("SHOT:" + row + "," + col);
        notifyObservers("RESULT:" + result);

        return result;
    }

    // Несколько нужных функций

    // Ждём сообщение с нужным префиксом (блокирует поток до получения)
    private String waitForMessage(String prefix) {
        while (true) {
            try {
                String msg = messageQueue.take(); // блокирует пока очередь пуста
                if (msg.startsWith(prefix)) {
                    return msg;
                }
                // Не наше сообщение - кладём обратно
                messageQueue.put(msg);
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void sendMessage(String message) {
        if (isServer) {
            server.sendToPlayer2(message);
        } else {
            client.send(message);
        }
    }

    private boolean checkIfOpponentDefeated() {
        // Считаем потопленные клетки на чужом поле
        int sunkCells = 0;
        int totalShipCells = 1*6 + 2*5 + 3*4 + 4*3 + 5*2 + 6*1; // = 56
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                // ПРОВЕРИТЬ
                if (opponentBoard.getHiddenCell(r, c) == 'X') sunkCells++;
            }
        }
        return sunkCells >= totalShipCells;
    }

    private void printBoards(String myName, String opponentName) {
        System.out.println("  Ваше поле (" + myName + ")" +
                "                    Поле противника (" + opponentName + ")");
        String header = "   ";
        for (int i = 0; i < Board.SIZE; i++) header += (char)('A' + i) + " ";
        System.out.println(header + "    " + header);

        for (int row = 0; row < Board.SIZE; row++) {
            String line = String.format("%2d ", row + 1);
            for (int col = 0; col < Board.SIZE; col++) {
                line += localPlayer.getBoard().getCell(row, col) + " ";
            }
            line += "    " + String.format("%2d ", row + 1);
            for (int col = 0; col < Board.SIZE; col++) {
                line += opponentBoard.getHiddenCell(row, col) + " ";
            }
            System.out.println(line);
        }
        System.out.println();
    }

    private void closeConnection() {
        try {
            if (isServer && server != null) server.stop();
            if (!isServer && client != null) client.closeBlocking();
        } catch (Exception e) {
            System.out.println("Ошибка закрытия соединения: " + e.getMessage());
        }
    }

    private String translateResult(String result) {
        switch (result) {
            case "hit": return "Ранил";
            case "sunk": return "Убил";
            default: return "Мимо";
        }
    }

    private void notifyObservers(String message) {
        if (isServer && server != null) {
            server.notifyObservers(message);
        }
    }
}
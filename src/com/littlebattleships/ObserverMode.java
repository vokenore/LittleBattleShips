package com.littlebattleships;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;
import java.util.Scanner;

public class ObserverMode {

    private Scanner scanner;
    private WebSocketClient wsClient;

    private char[][] field1 = new char[Board.SIZE][Board.SIZE];
    private char[][] field2 = new char[Board.SIZE][Board.SIZE];
    private String player1Name = "Игрок 1";
    private String player2Name = "Игрок 2";
    private boolean running = true;

    public ObserverMode(Scanner scanner) {
        this.scanner = scanner;
        initFields();
    }

    private void initFields() {
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                field1[r][c] = '.';
                field2[r][c] = '.';
            }
        }
    }

    public void start(String host, int port) {
        System.out.println("Подключаемся как наблюдатель...");

        try {
            wsClient = new WebSocketClient(new URI("ws://" + host + ":" + port)) {

                @Override
                public void onOpen(ServerHandshake handshake) {
                    System.out.println("Подключились! Наблюдаем за игрой.\n");
                    System.out.println("Нажми Enter в любой момент для выхода.");
                }

                @Override
                public void onMessage(String message) {
                    handleMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("\nСоединение закрыто. Игра завершена.");
                    running = false;
                }

                @Override
                public void onError(Exception ex) {
                    System.out.println("Ошибка: " + ex.getMessage());
                }
            };

            wsClient.connectBlocking();

        } catch (Exception e) {
            System.out.println("Не удалось подключиться: " + e.getMessage());
            return;
        }

        // Ждём Enter для выхода
        scanner.nextLine();
        try {
            wsClient.closeBlocking();
        } catch (Exception e) {
            System.out.println("Ошибка закрытия: " + e.getMessage());
        }
    }

    private void handleMessage(String raw) {
        // Сервер присылает "OBSERVE:SHOT:5,3" или "OBSERVER:текст"
        if (raw.startsWith("OBSERVER:")) {
            System.out.println("\n" + raw.replace("OBSERVER:", ""));
            return;
        }

        if (!raw.startsWith("OBSERVE:")) return;

        String message = raw.substring("OBSERVE:".length());

        if (message.startsWith("SHOT:")) {
            // SHOT:row,col - выстрел, ждём RESULT следующим сообщением
            // Сохраняем координаты для отображения
            String[] parts = message.substring(5).split(",");
            lastShotRow = Integer.parseInt(parts[0]);
            lastShotCol = Integer.parseInt(parts[1]);

        } else if (message.startsWith("RESULT:")) {
            String result = message.substring(7);
            // Отмечаем на поле противника стрелявшего
            // Чередуем - нечётный ход player1 стреляет, чётный - player2
            shotCount++;
            char mark = result.equals("miss") ? '*' : 'X';

            if (shotCount % 2 == 1) {
                // player1 стрелял по полю player2
                field2[lastShotRow][lastShotCol] = mark;
            } else {
                // player2 стрелял по полю player1
                field1[lastShotRow][lastShotCol] = mark;
            }

            char colChar = (char)('A' + lastShotCol);
            String shooter = shotCount % 2 == 1 ? player1Name : player2Name;
            System.out.printf("\n%s -> %c%d - %s%n",
                    shooter, colChar, lastShotRow + 1, translateResult(result));

            Game.clearConsole();
            printObserverBoards();

        } else if (message.startsWith("DEFEATED:")) {
            String loser = message.substring(9);
            System.out.println("\nИгра окончена! Проиграл: " + loser);

        } else if (message.startsWith("PLAYER1:")) {
            player1Name = message.substring(8);
            System.out.println("Игрок 1: " + player1Name);
        } else if (message.startsWith("PLAYER2:")) {
            player2Name = message.substring(8);
            System.out.println("Игрок 2: " + player2Name);
        }
    }

    // Счётчик ходов и последний выстрел - нужны между вызовами handleMessage
    private int shotCount = 0;
    private int lastShotRow = 0;
    private int lastShotCol = 0;

    private void printObserverBoards() {
        System.out.println("  Поле " + player1Name +
                "                       Поле " + player2Name);

        String header = "   ";
        for (int i = 0; i < Board.SIZE; i++) header += (char)('A' + i) + " ";
        System.out.println(header + "    " + header);

        for (int row = 0; row < Board.SIZE; row++) {
            String line = String.format("%2d ", row + 1);
            for (int col = 0; col < Board.SIZE; col++) {
                line += field1[row][col] + " ";
            }
            line += "    " + String.format("%2d ", row + 1);
            for (int col = 0; col < Board.SIZE; col++) {
                line += field2[row][col] + " ";
            }
            System.out.println(line);
        }
        System.out.println("\nНажми Enter для выхода.");
    }

    private String translateResult(String result) {
        switch (result) {
            case "hit":  return "Ранил";
            case "sunk": return "Убил";
            default:     return "Мимо";
        }
    }
}
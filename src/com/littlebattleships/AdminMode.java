package com.littlebattleships;


import java.io.*;
import java.util.*;
import java.util.zip.*;

public class AdminMode {

    private Scanner scanner;
    private File logsDir;

    public AdminMode(Scanner scanner) {
        this.scanner = scanner;
        this.logsDir = new File("logs");
    }

    public void start() {
        System.out.println("\n=~- РЕЖИМ АДМИНИСТРАТОРА\n");

        while (true) {
            List<File> games = getGameFiles();

            if (games.isEmpty()) {
                System.out.println("Сыгранных игр нет.");
                return;
            }

            printGameList(games);
            printMenu();

            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Выход из режима администратора.");
                return;
            }

            String[] parts = input.split("\\s+");
            if (parts.length != 2) {
                System.out.println("Неверная команда. Пример: view 1");
                continue;
            }

            String command = parts[0].toLowerCase();
            int index;

            try {
                index = Integer.parseInt(parts[1]) - 1;
            } catch (NumberFormatException e) {
                System.out.println("Неверный номер.");
                continue;
            }

            if (index < 0 || index >= games.size()) {
                System.out.println("Игры с таким номером нет.");
                continue;
            }

            File gameFile = games.get(index);

            switch (command) {
                case "view": viewGame(gameFile); break;
                case "delete": deleteGame(gameFile); break;
                case "archive": archiveGame(gameFile); break;
                default: System.out.println("Неизвестная команда.");
            }
        }
    }

    private List<File> getGameFiles() {
        List<File> files = new ArrayList<>();
        if (!logsDir.exists()) return files;

        File[] all = logsDir.listFiles(
                f -> f.getName().startsWith("game_") && f.getName().endsWith(".txt")
        );

        if (all != null) {
            // Сортируем — новые сверху
            Arrays.sort(all, (a, b) -> b.getName().compareTo(a.getName()));
            files.addAll(Arrays.asList(all));
        }
        return files;
    }

    private void printGameList(List<File> games) {
        System.out.println("Сыгранные игры:");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        for (int i = 0; i < games.size(); i++) {
            String name = games.get(i).getName()
                    .replace("game_", "")
                    .replace(".txt", "")
                    .replace("_", " ");
            System.out.printf("%3d. %s%n", i + 1, name);
        }
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
    }

    private void printMenu() {
        System.out.println("Команды:");
        System.out.println("\tview N    - просмотр игры");
        System.out.println("\tdelete N  - удалить игру");
        System.out.println("\tarchive N - архивировать в zip");
        System.out.println("\texit      - выйти");
    }

    // Просмотр

    private void viewGame(File file) {
        List<String> lines = readAllLines(file);
        if (lines.isEmpty()) {
            System.out.println("Файл пустой.");
            return;
        }

        // Читаем имена игроков из файла
        String player1Name = "Игрок 1";
        String player2Name = "Игрок 2";
        String winnerName = "";

        for (String line : lines) {
            if (line.startsWith("Победитель:")) {
                winnerName = line.replace("Победитель:", "").trim();
            }
        }

        int nameCount = 0;
        for (String line : lines) {
            if (line.startsWith("--- ") && line.endsWith(" ---")) {
                String name = line.replace("--- ", "").replace(" ---", "").trim();
                if (nameCount == 0) player1Name = name;
                else player2Name = name;
                nameCount++;
            }
        }

        int movesStart = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("ВСЕ ХОДЫ")) {
                movesStart = i + 2;
                break;
            }
        }

        if (movesStart == -1) {
            System.out.println("Ходы не найдены.");
            waitEnter();
            return;
        }

        List<String[]> moves = new ArrayList<>();
        for (int i = movesStart; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;

            try {
                // Парсим имя игрока
                int bracketEnd = line.indexOf(']');
                if (bracketEnd == -1) continue;
                String afterTime = line.substring(bracketEnd + 1).trim();


                int arrowIdx = afterTime.indexOf("->");
//                if (arrowIdx == -1) arrowIdx = afterTime.indexOf("-");
                if (arrowIdx == -1) continue;

                String playerPart = afterTime.substring(0, arrowIdx).trim();
                String rest = afterTime.substring(arrowIdx + 2).trim();

                int dashIdx = rest.indexOf("-");
//                if (dashIdx == -1) dashIdx = rest.indexOf("-");
                if (dashIdx == -1) continue;

                String coord = rest.substring(0, dashIdx).trim();
                String resultRu = rest.substring(dashIdx + 1).trim();

                // Координата вида "A5"
                if (coord.length() < 2) continue;
                char colChar = coord.charAt(0);
                int col = colChar - 'A';
                int row = Integer.parseInt(coord.substring(1)) - 1;

                // Переводим результат обратно
                String result;
                if (resultRu.contains("бил") || resultRu.contains("убил") ||
                        resultRu.contains("Убил")) {
                    result = "sunk";
                } else if (resultRu.contains("анил") || resultRu.contains("Ранил")) {
                    result = "hit";
                } else {
                    result = "miss";
                }

                moves.add(new String[]{playerPart, String.valueOf(col),
                        String.valueOf(row), result});

            } catch (Exception e) {
            }
        }

        char[][] field1 = new char[Board.SIZE][Board.SIZE];
        char[][] field2 = new char[Board.SIZE][Board.SIZE];
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                field1[r][c] = '.';
                field2[r][c] = '.';
            }
        }

        Game.clearConsole();
        System.out.println("=- ПРОСМОТР: " + file.getName());
        System.out.println("Победитель: " + winnerName);
        System.out.println("Ходов всего: " + moves.size());
        System.out.println("\nEnter - следующий ход  |  q - выход\n");
        printReplayBoards(field1, field2, player1Name, player2Name);

        // Воспроизводим ход за ходом
        for (int i = 0; i < moves.size(); i++) {
            System.out.print("[Enter - ход " + (i + 1) + "/" + moves.size() + ", q - выход]: ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("q")) break;

            String[] move = moves.get(i);
            String moverName = move[0];
            int col = Integer.parseInt(move[1]);
            int row = Integer.parseInt(move[2]);
            String result = move[3];
            char mark = result.equals("miss") ? '*' : 'X';

            if (moverName.equals(player1Name)) {
                field2[row][col] = mark;
            } else {
                field1[row][col] = mark;
            }

            Game.clearConsole();
            System.out.println("=- ПРОСМОТР: " + file.getName());

            char colChar = (char)('A' + col);
            String resultRu = result.equals("sunk") ? "Убил" :
                    result.equals("hit")  ? "Ранил" : "Мимо";
            System.out.printf("Ход %d: %s -> %c%d - %s%n",
                    i + 1, moverName, colChar, row + 1, resultRu);
            System.out.println();

            printReplayBoards(field1, field2, player1Name, player2Name);
        }

        System.out.println("\nПросмотр завершён.");
        waitEnter();
    }

    private void printReplayBoards(char[][] field1, char[][] field2,
                                   String name1, String name2) {
        System.out.println("  Поле " + name1 +
                "                       Поле " + name2);

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
        System.out.println();
    }

    // Удаление

    private void deleteGame(File file) {
        System.out.printf("Удалить '%s'? (yes/no): ", file.getName());
        String confirm = scanner.nextLine().trim();

        if (confirm.equalsIgnoreCase("yes")) {
            if (file.delete()) {
                System.out.println("Игра удалена.");
            } else {
                System.out.println("Не удалось удалить.");
            }
        } else {
            System.out.println("Отменено.");
        }
    }

    // Архивирование

    private void archiveGame(File file) {
        String zipPath = file.getPath().replace(".txt", ".zip");
        File zipFile = new File(zipPath);

        // Если архив уже есть - спрашиваем
        if (zipFile.exists()) {
            System.out.print("Архив уже существует. Перезаписать? (yes/no): ");
            String confirm = scanner.nextLine().trim();
            if (!confirm.equalsIgnoreCase("yes")) {
                System.out.println("Отменено.");
                return;
            }
        }

        try (
                FileInputStream fis = new FileInputStream(file);
                FileOutputStream fos = new FileOutputStream(zipFile);
                ZipOutputStream zos = new ZipOutputStream(fos)
        ) {
            ZipEntry entry = new ZipEntry(file.getName());
            zos.putNextEntry(entry);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }

            zos.closeEntry();
            System.out.println("Архив создан: " + zipPath);

        } catch (IOException e) {
            System.out.println("Ошибка архивации: " + e.getMessage());
        }
    }


    private List<String> readAllLines(File file) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            System.out.println("Ошибка чтения: " + e.getMessage());
        }
        return lines;
    }

    private void waitEnter() {
        System.out.print("\n[Enter для продолжения]");
        scanner.nextLine();
    }
}
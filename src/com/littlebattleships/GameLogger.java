package com.littlebattleships;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class GameLogger {

    public static class MoveRecord {
        public String playerName;
        public int row;
        public int col;
        public String result;
        public LocalDateTime time;

        public MoveRecord(String playerName, int row, int col, String result) {
            this.playerName = playerName;
            this.row = row;
            this.col = col;
            this.result = result;
            this.time = LocalDateTime.now();
        }
    }

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FILE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<MoveRecord> moves;
    private String player1Name;
    private String player2Name;

    // Финальные поля — сохраняем снимок в конце игры
    private char[][] finalField1;
    private char[][] finalField2;

    public GameLogger(String player1Name, String player2Name) {
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.moves = new ArrayList<>();
        this.startTime = LocalDateTime.now();
    }

    public void logMove(String playerName, int row, int col, String result) {
        moves.add(new MoveRecord(playerName, row, col, result));
    }

    // Сохраняем поля пеоед зеписью лога
    public void captureFields(Board field1, Board field2) {
        finalField1 = new char[Board.SIZE][Board.SIZE];
        finalField2 = new char[Board.SIZE][Board.SIZE];
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                finalField1[r][c] = field1.getCell(r, c);
                finalField2[r][c] = field2.getCell(r, c);
            }
        }
    }

    public void finish(String winnerName, Board field1, Board field2) {
        this.endTime = LocalDateTime.now();
        captureFields(field1, field2);
        save(winnerName);
    }

    private void save(String winnerName) {
        File logsDir = new File("logs");
        if (!logsDir.exists()) logsDir.mkdirs();

        String fileName = "logs/game_" + startTime.format(FILE_FORMATTER) + ".txt";

        try (PrintWriter w = new PrintWriter(new FileWriter(fileName))) {
            writeReport(w, winnerName);
            System.out.println("Лог сохранён: " + fileName);
        } catch (IOException e) {
            System.out.println("Не удалось сохранить лог: " + e.getMessage());
        }
    }

    private void writeReport(PrintWriter w, String winnerName) {
        w.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        w.println("         МОРСКОЙ БОЙ — ОТЧЁТ");
        w.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        w.println("Начало : " + startTime.format(FORMATTER));
        w.println("Конец  : " + endTime.format(FORMATTER));
        w.println("Победитель: " + winnerName);
        w.println();

        writePlayerStats(w, player1Name, winnerName);
        writePlayerStats(w, player2Name, winnerName);

        w.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        w.println("ПОЛЯ НА МОМЕНТ ФИНИША");
        w.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        w.println();
        writeBoardToFile(w, player1Name, finalField1);
        writeBoardToFile(w, player2Name, finalField2);

        w.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        w.println("ВСЕ ХОДЫ");
        w.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        w.println();

        for (int i = 0; i < moves.size(); i++) {
            MoveRecord m = moves.get(i);
            char colChar = (char)('A' + m.col);
            w.printf("%3d. [%s] %-12s -> %c%d - %s%n",
                    i + 1,
                    m.time.format(FORMATTER),
                    m.playerName,
                    colChar,
                    m.row + 1,
                    translateResult(m.result));
        }
    }

    private void writePlayerStats(PrintWriter w, String name, String winnerName) {
        w.println("--- " + name + " ---");
        w.println("Результат : " + (name.equals(winnerName) ? "ПОБЕДА" : "ПОРАЖЕНИЕ"));

        int total = 0, hits = 0;
        for (MoveRecord m : moves) {
            if (m.playerName.equals(name)) {
                total++;
                if (!m.result.equals("miss")) hits++;
            }
        }

        double accuracy = total > 0 ? (hits * 100.0 / total) : 0;
        w.println("Ходов    : " + total);
        w.println("Попаданий: " + hits);
        w.printf( "Точность : %.1f%%%n", accuracy);
        w.println();
    }

    private void writeBoardToFile(PrintWriter w, String name, char[][] field) {
        if (field == null) return;
        w.println("Поле " + name + ":");
        w.print("   ");
        for (int i = 0; i < Board.SIZE; i++) w.print((char)('A' + i) + " ");
        w.println();
        for (int row = 0; row < Board.SIZE; row++) {
            w.printf("%2d ", row + 1);
            for (int col = 0; col < Board.SIZE; col++) {
                w.print(field[row][col] + " ");
            }
            w.println();
        }
        w.println();
    }

    private String translateResult(String result) {
        switch (result) {
            case "hit":  return "Ранил";
            case "sunk": return "Убил";
            default:     return "Мимо";
        }
    }
}
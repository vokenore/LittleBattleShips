package com.easybattleships;


public class InputParser {

    public static class ParseResult {
        public int row;
        public int col;
        public Ship.Direction direction;
        public int size;
    }

    // Парсим строку вида "A1 H 3" (начиная с A1 по горизонтали 3-палубнник)
    // Возвращает null если формат неверный
    public static ParseResult parse(String input) {
        if (input == null || input.trim().isEmpty()) return null;

        // Разбиваем по пробелам, убираем лишние пробелы
        String[] parts = input.trim().toUpperCase().split("\\s+");

        if (parts.length != 3) {
            return null;
        }

        try {
            ParseResult result = new ParseResult();

            // Обрабатываем первую часть (координата начала)
            String coord = parts[0];

            // Первый символ - колонка (буква)
            char colChar = coord.charAt(0);
            if (colChar < 'A' || colChar > 'P') return null;
            result.col = colChar - 'A';

            // Остаток строки - строка (номер)
            int row = Integer.parseInt(coord.substring(1));
            if (row < 1 || row > 16) return null;
            result.row = row - 1;

            // Обрабатываем направление
            String dir = parts[1];
            if (dir.equals("H")) {
                result.direction = Ship.Direction.HORIZONTAL;
            } else if (dir.equals("V")) {
                result.direction = Ship.Direction.VERTICAL;
            } else {
                return null;
            }

            // Обрабатываем размер
            result.size = Integer.parseInt(parts[2]);
            if (result.size < 1 || result.size > 6) return null;

            return result;

        } catch (NumberFormatException e) {
            // Ввод неверный
            return null;
        }
    }

    // Парсим координату выстрела (типо "A1")
    public static int[] parseShot(String input) {
        if (input == null || input.trim().isEmpty()) return null;

        try {
            String coord = input.trim().toUpperCase();
            char colChar = coord.charAt(0);
            if (colChar < 'A' || colChar > 'P') return null;

            int col = colChar - 'A';
            int row = Integer.parseInt(coord.substring(1));
            if (row < 1 || row > 16) return null;

            return new int[]{row - 1, col};

        } catch (NumberFormatException e) {
            return null;
        }
    }
}
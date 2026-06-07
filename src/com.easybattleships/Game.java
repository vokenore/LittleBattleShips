package com.easybattleships;

import java.util.Scanner;

public class Game {
    private Scanner scanner = new Scanner(System.in);

    public void start() {
        System.out.print("Ваше имя: ");
        String name = scanner.nextLine().trim();

        Player player = new Player(name);
        ShipPlacer.placeShips(player, scanner);

        System.out.println("\nИгра скоро начнётся!");
        System.out.println("Или нет...");
    }
}

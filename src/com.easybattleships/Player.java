package com.easybattleships;

public class Player {

    private String name;
    private Board board;

    public Player(String name) {
        this.name = name;
        this.board = new Board();
    }

    public String getName() { return name; }
    public Board getBoard() { return board; }

}

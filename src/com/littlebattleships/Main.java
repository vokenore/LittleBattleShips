package com.littlebattleships;

import java.io.UnsupportedEncodingException;

public class Main {
    public static void main(String[] args) throws UnsupportedEncodingException {
        System.setOut(new java.io.PrintStream(System.out, true, "UTF-8"));
        System.setIn(new java.io.DataInputStream(System.in));
        Game game = new Game();
        game.start();
    }
}


//Передача сообщений по локальной сети

//JOIN:Имя              второй игрок подключился
//READY:Имя             игрок расставил корабли
//SHOT:row,col          выстрел
//RESULT:hit/miss/sunk  результат выстрела
//DEFEATED:Имя          игрок проиграл

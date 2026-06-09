package com.littlebattleships;

public class Main {
    public static void main(String[] args) {
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

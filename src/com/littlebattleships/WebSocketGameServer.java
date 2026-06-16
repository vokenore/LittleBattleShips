package com.littlebattleships;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class WebSocketGameServer extends WebSocketServer {

    private WebSocket player2Conn;
    // Наблюдатели (после подключения 2 игроков)
    private List<WebSocket> observers = new ArrayList<>();

    // Уведомления о событиях
    private MessageHandler handler;

    public interface MessageHandler {
        void onPlayerJoined(String name);
        void onMessageReceived(String message, boolean fromPlayer1);
    }

    public WebSocketGameServer(int port, MessageHandler handler) {
        super(new InetSocketAddress(port));
        this.handler = handler;
        setReuseAddr(true);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        if (player2Conn == null) {
            // Подключение второго игрока
            player2Conn = conn;
            System.out.println("Второй игрок подключился: " + conn.getRemoteSocketAddress());
        } else {
            // Дальше уже наблюдатели
            observers.add(conn);
            conn.send("OBSERVER:Вы подключены как наблюдатель");
            System.out.println("Наблюдатель подключился.");
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {

//        // Пересылаем наблюдателям все сообщения
//        for (WebSocket obs : observers) {
//            if (obs.isOpen()) obs.send(message);
//        }
        handler.onMessageReceived(message, false);
    }

    // Есть ли наблюдатели
    public boolean hasObservers() {
        return !observers.isEmpty();
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        if (conn == player2Conn) {
            System.out.println("Каррамба! Второй игрок покинул бой.");
            player2Conn = null;
        }
        observers.remove(conn);
    }

    public void notifyObservers(String message) {
        for (WebSocket obs : new ArrayList<>(observers)) {
            if (obs.isOpen()) {
                obs.send("OBSERVE:" + message);
            }
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.out.println("Ошибка WebSocket: " + ex.getMessage());
    }

    @Override
    public void onStart() {
        System.out.println("Сервер запущен.");
    }

    // Отправляем сообщение второму игроку
    public void sendToPlayer2(String message) {
        if (player2Conn != null && player2Conn.isOpen()) {
            player2Conn.send(message);
        }
    }

    // Отправляем сообщение всем наблюдателям
    public void broadcastToObservers(String message) {
        for (WebSocket obs : observers) {
            if (obs.isOpen()) obs.send(message);
        }
    }
}
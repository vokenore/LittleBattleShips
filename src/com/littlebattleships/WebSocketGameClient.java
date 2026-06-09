package com.littlebattleships;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;

public class WebSocketGameClient extends WebSocketClient {

    private MessageHandler handler;

    public interface MessageHandler {
        void onMessageReceived(String message);
        void onConnected();
        void onDisconnected();
    }

    public WebSocketGameClient(String host, int port, MessageHandler handler) throws Exception {
        super(new URI("ws://" + host + ":" + port));
        this.handler = handler;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        handler.onConnected();
    }

    @Override
    public void onMessage(String message) {
        handler.onMessageReceived(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        handler.onDisconnected();
    }

    @Override
    public void onError(Exception ex) {
        System.out.println("Ошибка клиента: " + ex.getMessage());
    }
}
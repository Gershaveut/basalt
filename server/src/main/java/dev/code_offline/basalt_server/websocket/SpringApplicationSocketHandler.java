package dev.code_offline.basalt_server.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class SpringApplicationSocketHandler extends TextWebSocketHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(SpringApplicationSocketHandler.class);
	
	private static final List<WebSocketSession> sessions = new ArrayList<>();
	
	@Override
	public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
		sessions.remove(session);
	}
	
	@Override
	public void afterConnectionEstablished(@NonNull WebSocketSession session) {
		sessions.add(session);
	}
	
	@Override
	public void handleMessage(@NonNull WebSocketSession session, @NonNull WebSocketMessage<?> message) throws Exception {
		session.close();
	}
	
	public synchronized void sync() {
        sessions.forEach(session -> {
            try {
                session.sendMessage(new TextMessage("sync"));
            } catch (IOException e) {
                LOGGER.error("Sync error", e);
            }
        });
	}
}

package dev.code_offline.basalt_server.websocket;

import dev.code_offline.basalt_server.BasaltApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class BasaltSocketHandler extends TextWebSocketHandler {
	private static final List<WebSocketSession> sessions = new ArrayList<>();
	
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		sessions.remove(session);
	}
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		sessions.add(session);
	}
	
	public void sync() {
		sessions.forEach(session -> {
			try {
				session.sendMessage(new TextMessage("sync"));
			} catch (IOException e) {
				BasaltApplication.logger.error(e.getMessage());
			}
		});
	}
}

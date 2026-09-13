package org.example.cstp.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    // userId -> 该用户的所有在线连接（Set 支持同账号多端/多标签页）
    private static final Map<Long, Set<WebSocketSession>> USER_SESSIONS = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public ChatWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId == null) {
            return;
        }
        //computeIfAbsent 这个方法。它的意思是"查一下本子里有没有这一页，没有就新建一页，有就直接翻到那一页"
        //用 CopyOnWriteArraySet 是因为它在多线程（多个用户同时连接/断开）下读写安全。
        USER_SESSIONS.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(session);
        log.info("用户 {} WebSocket连接成功", userId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        removeSession(session);
        log.info("WebSocket连接关闭");
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        removeSession(session);
    }

    private void removeSession(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId == null) {
            return;
        }
        Set<WebSocketSession> sessions = USER_SESSIONS.get(userId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                USER_SESSIONS.remove(userId);
            }
        }
    }

    /**
     * 给指定用户的所有在线连接推送一条 JSON 消息
     * 用户不在线时静默跳过（消息已落库，对方上线/进入会话时走 HTTP 拉历史兜底）
     */
    public void pushToUser(Long userId, Map<String, Object> data) {
        //Set<WebSocketSession>是引用类型，所以可以修改USER_SESSIONS里面的set集合
        Set<WebSocketSession> sessions = USER_SESSIONS.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            log.info("用户 {} 不在线，跳过WebSocket推送", userId);
            return;
        }
        String json;
        try {
            json = objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error("推送消息序列化失败", e);
            return;
        }
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(json));
                } catch (Exception e) {
                    log.error("向用户 {} 推送消息失败", userId, e);
                }
            }
        }
    }
}

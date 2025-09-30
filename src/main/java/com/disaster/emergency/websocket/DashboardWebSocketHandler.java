package com.disaster.emergency.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 大屏WebSocket处理器
 * 
 * <p>处理大屏实时数据推送的WebSocket连接。</p>
 * 
 * @author 系统
 * @version 1.0
 * @since 2024-01-01
 */
@Component
public class DashboardWebSocketHandler implements WebSocketHandler {

    // 存储所有连接的会话
    private final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    
    // 存储用户会话映射
    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("WebSocket连接建立: " + session.getId());
        
        // 发送连接成功消息
        Map<String, Object> message = new java.util.HashMap<>();
        message.put("type", "connection_established");
        message.put("message", "连接成功");
        message.put("timestamp", System.currentTimeMillis());
        sendMessage(session, message);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage) {
            String payload = ((TextMessage) message).getPayload();
            System.out.println("收到消息: " + payload);
            
            try {
                // 解析消息
                Map<String, Object> data = objectMapper.readValue(payload, Map.class);
                String type = (String) data.get("type");
                
                switch (type) {
                    case "register_user":
                        handleUserRegistration(session, data);
                        break;
                    case "subscribe":
                        handleSubscription(session, data);
                        break;
                    case "unsubscribe":
                        handleUnsubscription(session, data);
                        break;
                    case "ping":
                        handlePing(session);
                        break;
                    default:
                        System.out.println("未知消息类型: " + type);
                }
            } catch (Exception e) {
                System.err.println("处理消息失败: " + e.getMessage());
                sendErrorMessage(session, "消息处理失败: " + e.getMessage());
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("WebSocket传输错误: " + exception.getMessage());
        sessions.remove(session);
        removeUserSession(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        System.out.println("WebSocket连接关闭: " + session.getId() + ", 状态: " + closeStatus);
        sessions.remove(session);
        removeUserSession(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 处理用户注册
     */
    private void handleUserRegistration(WebSocketSession session, Map<String, Object> data) {
        String userId = (String) data.get("userId");
        String userType = (String) data.get("userType");
        
        if (userId != null) {
            userSessions.put(userId, session);
            System.out.println("用户注册: " + userId + ", 类型: " + userType);
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("type", "registration_success");
            response.put("userId", userId);
            response.put("message", "用户注册成功");
            sendMessage(session, response);
        }
    }

    /**
     * 处理订阅
     */
    private void handleSubscription(WebSocketSession session, Map<String, Object> data) {
        String topic = (String) data.get("topic");
        System.out.println("用户订阅主题: " + topic);
        
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("type", "subscription_success");
        response.put("topic", topic);
        response.put("message", "订阅成功");
        sendMessage(session, response);
    }

    /**
     * 处理取消订阅
     */
    private void handleUnsubscription(WebSocketSession session, Map<String, Object> data) {
        String topic = (String) data.get("topic");
        System.out.println("用户取消订阅主题: " + topic);
        
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("type", "unsubscription_success");
        response.put("topic", topic);
        response.put("message", "取消订阅成功");
        sendMessage(session, response);
    }

    /**
     * 处理心跳
     */
    private void handlePing(WebSocketSession session) {
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("type", "pong");
        response.put("timestamp", System.currentTimeMillis());
        sendMessage(session, response);
    }

    /**
     * 发送消息给指定会话
     */
    public void sendMessage(WebSocketSession session, Map<String, Object> message) {
        try {
            if (session.isOpen()) {
                String json = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(json));
            }
        } catch (IOException e) {
            System.err.println("发送消息失败: " + e.getMessage());
        }
    }

    /**
     * 广播消息给所有连接
     */
    public void broadcastMessage(Map<String, Object> message) {
        sessions.forEach(session -> sendMessage(session, message));
    }

    /**
     * 发送消息给指定用户
     */
    public void sendMessageToUser(String userId, Map<String, Object> message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null) {
            sendMessage(session, message);
        }
    }

    /**
     * 发送错误消息
     */
    private void sendErrorMessage(WebSocketSession session, String errorMessage) {
        Map<String, Object> error = new java.util.HashMap<>();
        error.put("type", "error");
        error.put("message", errorMessage);
        error.put("timestamp", System.currentTimeMillis());
        sendMessage(session, error);
    }

    /**
     * 移除用户会话
     */
    private void removeUserSession(WebSocketSession session) {
        userSessions.entrySet().removeIf(entry -> entry.getValue().equals(session));
    }

    /**
     * 获取连接数
     */
    public int getConnectionCount() {
        return sessions.size();
    }

    /**
     * 获取用户数
     */
    public int getUserCount() {
        return userSessions.size();
    }
}

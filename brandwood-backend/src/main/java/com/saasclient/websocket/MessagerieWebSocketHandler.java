package com.saasclient.websocket;

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

/**
 * Gestionnaire WebSocket de la messagerie temps reel. Tient un registre
 * {userId -> sessions ouvertes} et permet de pousser un message (JSON) a un
 * utilisateur des qu'il est connecte. L'authentification est faite en amont par
 * {@link JwtHandshakeInterceptor} (l'attribut "userId" est present dans la session).
 */
@Slf4j
@Component
public class MessagerieWebSocketHandler extends TextWebSocketHandler {

    private final Map<Long, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId == null) { return; }
        sessions.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(session);
        log.debug("WS connecte: user {} ({} session(s))", userId, sessions.get(userId).size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId == null) { return; }
        Set<WebSocketSession> set = sessions.get(userId);
        if (set != null) {
            set.remove(session);
            if (set.isEmpty()) { sessions.remove(userId); }
        }
    }

    /** Vrai si l'utilisateur a au moins une session WebSocket ouverte. */
    public boolean estEnLigne(Long userId) {
        Set<WebSocketSession> set = sessions.get(userId);
        return set != null && !set.isEmpty();
    }

    /** Pousse un objet (serialise en JSON) vers toutes les sessions ouvertes de l'utilisateur. */
    public void envoyerAUtilisateur(Long userId, Object payload) {
        Set<WebSocketSession> set = sessions.get(userId);
        if (set == null || set.isEmpty()) { return; }
        try {
            TextMessage message = new TextMessage(objectMapper.writeValueAsString(payload));
            for (WebSocketSession s : set) {
                if (s.isOpen()) {
                    synchronized (s) { s.sendMessage(message); }
                }
            }
        } catch (Exception e) {
            log.warn("Echec push WebSocket vers user {}: {}", userId, e.getMessage());
        }
    }
}

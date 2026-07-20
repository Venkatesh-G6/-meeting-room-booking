package com.yourcompany.roombooking.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConversationStateManager {

    private static final Logger log = LoggerFactory.getLogger(ConversationStateManager.class);

    private final Map<String, ConversationContext> stateMap = new ConcurrentHashMap<>();

    public ConversationContext getContext(String userId) {
        ConversationContext context = stateMap.get(userId);
        if (context == null) {
            context = new ConversationContext();
            context.reset();
            stateMap.put(userId, context);
        } else if (context.isExpired()) {
            context.reset();
        }
        return context;
    }

    public void saveContext(String userId, ConversationContext context) {
        context.setLastUpdated(LocalDateTime.now());
        stateMap.put(userId, context);
    }

    public void clearContext(String userId) {
        stateMap.remove(userId);
    }

    @Scheduled(fixedDelay = 300000)
    public void cleanExpiredSessions() {
        int removed = 0;
        Iterator<Map.Entry<String, ConversationContext>> iterator = stateMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ConversationContext> entry = iterator.next();
            if (entry.getValue().isExpired()) {
                iterator.remove();
                removed++;
            }
        }
        if (removed > 0) {
            log.info("Cleaned up {} expired bot conversation sessions", removed);
        }
    }
}

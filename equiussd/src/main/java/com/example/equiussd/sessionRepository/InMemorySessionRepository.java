package com.example.equiussd.sessionRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemorySessionRepository implements SessionRepository {

    // In-memory storage for active sessions
    private final Map<String, Boolean> activeSessions = new ConcurrentHashMap<>();

    @Override
    public boolean isPhoneNumberInUse(String phoneNumber) {
        return activeSessions.containsKey(phoneNumber) && activeSessions.get(phoneNumber);
    }

    @Override
    public void addSession(String phoneNumber) {
        activeSessions.put(phoneNumber, true);
    }

    @Override
    public void removeSession(String phoneNumber) {
        activeSessions.remove(phoneNumber);
    }
}



package com.example.equiussd.sessionRepository;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface SessionRepository {

    // Check if a phone number is already associated with an active session
    boolean isPhoneNumberInUse(String phoneNumber);

    // Add a new session for a phone number
    void addSession(String phoneNumber);

    // Remove a session associated with a phone number
    void removeSession(String phoneNumber);
}



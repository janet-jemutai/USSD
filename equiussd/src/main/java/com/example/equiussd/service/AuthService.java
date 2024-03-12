package com.example.equiussd.service;
import com.example.equiussd.sessionRepository.SessionRepository;
import com.example.equiussd.sessionRepository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private SessionRepository sessionRepository;

    public void loginUser(String phoneNumber) throws DuplicateSessionException {
        // Check if the phone number is already associated with an active session
        if (sessionRepository.isPhoneNumberInUse(phoneNumber)) {
            throw new DuplicateSessionException("Phone number already being used in another active session.");
        }

        // Continue with the login process
        // ...
    }
}




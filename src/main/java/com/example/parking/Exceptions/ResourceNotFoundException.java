package com.example.parking.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// ה-Annotation הזה אומר ל-Spring: כשמישהו זורק את השגיאה הזו, תחזיר ללקוח קוד 404
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
package com.example.parking.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

//@NoArgsConstructor

    @AllArgsConstructor
    @Data
    @ToString
    @Builder
    @RequiredArgsConstructor
    @Entity
    public class Users {
        @Id
        private long userId;
        private String fullName;
        private String phoneNumber;
        private String type;//מנוי
        private String email;//מנוי
    String password;
    private double balance; // יתרה כספית בחשבון
    private int stars; // לצבירת נקודות הטבה

        @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
        private List<Vehicles> vehicles;






    }


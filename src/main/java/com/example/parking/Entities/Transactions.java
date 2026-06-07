package com.example.parking.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@NoArgsConstructor
    @AllArgsConstructor
    @Data
    @ToString
    @Builder
    @Entity
    public class Transactions {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private long transactionId;

        private String vehicleId;
        private long spotId;
        private long startTime;      // זמן כניסה (שניות/מילישניות)
        private long endTime;        // זמן יציאה
        private double totalPayment; // הסכום שחושב בסוף
        private boolean paymentStatus;
        private Long userId; // יכול להיות Null אם הרכב לא רשום



    }


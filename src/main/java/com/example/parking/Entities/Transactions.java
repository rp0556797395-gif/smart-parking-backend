package com.example.parking.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

    @AllArgsConstructor
    @Data
    @ToString
    @Builder
    @Entity
    public class Transactions {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private long transactionId;

        private String vehicleId; // מקשר לרכב
        private long spotId;         // מספר החניה
        private long startTime;      // זמן כניסה (שניות/מילישניות)
        private long endTime;        // זמן יציאה
        private double totalPayment; // הסכום שחושב בסוף
        private boolean paymentStatus; // האם התשלום בוצע (TRUE / FALSE)
        private Long userId; // יכול להיות Null אם הרכב לא רשום

        public Transactions(String abc1234, int i, long l, long l1, double v, boolean b, long l2) {
        }
        // קונסטרוקטור ברירת מחדל
        public Transactions() {
        }

        // אין צורך בגטרים וסטרים הודות ל-Lombok


//        public Transactions() {
//        }
//
//        public Transactions(long transactionId, String vehicleId, long spotId, long startTime, long endTime, double totalPayment, boolean paymentStatus, Long userId) {
//            this.transactionId = transactionId;
//            this.vehicleId = vehicleId;
//            this.spotId = spotId;
//            this.startTime = startTime;
//            this.endTime = endTime;
//            this.totalPayment = totalPayment;
//            this.paymentStatus = paymentStatus;
//            this.userId = userId;
//        }

        public long getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(long transactionId) {
            this.transactionId = transactionId;
        }

        public String getVehicleId() {
            return vehicleId;
        }

        public void setVehicleId(String vehicleId) {
            this.vehicleId = vehicleId;
        }

        public long getSpotId() {
            return spotId;
        }

        public void setSpotId(long spotId) {
            this.spotId = spotId;
        }

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public long getEndTime() {
            return endTime;
        }

        public void setEndTime(long endTime) {
            this.endTime = endTime;
        }

        public double getTotalPayment() {
            return totalPayment;
        }

        public void setTotalPayment(double totalPayment) {
            this.totalPayment = totalPayment;
        }

        public boolean isPaymentStatus() {
            return paymentStatus;
        }

        public void setPaymentStatus(boolean paymentStatus) {
            this.paymentStatus = paymentStatus;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }
    }


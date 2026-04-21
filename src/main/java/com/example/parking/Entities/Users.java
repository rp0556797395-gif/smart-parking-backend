package com.example.parking.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

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

        public long getUserId() {
            return userId;
        }

        public void setUserId(long userId) {
            this.userId = userId;
        }

        public String getFullName() {
            return fullName;
        }
        public void setemail(String email) {this.email = email;}

        public String getemail() {return email;}

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public double getBalance() {
            return balance;
        }

        public void setBalance(double balance) {
            this.balance = balance;
        }

        public int getStars() {
            return stars;
        }

        public void setStars(int stars) {
            this.stars = stars;
        }
    }


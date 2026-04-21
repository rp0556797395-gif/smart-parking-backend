package com.example.parking.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @ToString
    @Builder
    @Entity
    public class Availability {
        @Id
        private long spotId;             // מזהה החניה (מצביע על ParkingSpot)
        private long availableFromTime;  // זמן בו החניה תהיה פנויה שוב (באורך זמן בשניות)
        private long availableUntilTime; // זמן עד החניה תפנה שוב (באורך זמן בשניות)

        // אין צורך בגטרים וסטרים הודות ל-Lombok

        public long getSpotId() {
            return spotId;
        }

        public void setSpotId(long spotId) {
            this.spotId = spotId;
        }

        public long getAvailableFromTime() {
            return availableFromTime;
        }

        public void setAvailableFromTime(long availableFromTime) {
            this.availableFromTime = availableFromTime;
        }

        public long getAvailableUntilTime() {
            return availableUntilTime;
        }

        public void setAvailableUntilTime(long availableUntilTime) {
            this.availableUntilTime = availableUntilTime;
        }
    }


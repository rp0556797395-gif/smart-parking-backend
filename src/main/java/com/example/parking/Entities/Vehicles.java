package com.example.parking.Entities;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@NoArgsConstructor
    @AllArgsConstructor
    @Data
    @ToString
    @Builder
    @Entity
    public class Vehicles {
        @Id
        private long vehicleId;      // מזהה ייחודי לרכב
        private String licensePlate; // מספר רישוי הרכב
        //private long spotId;         // מזהה החניה שבה הרכב חנה (מצביע על ParkingSpot)
        private long userId;
        private String vehicleType;    // למשל: "נכים", "חשמלי", "רגיל"

        public long getVehicleId() {
            return vehicleId;
        }

        public void setVehicleId(long vehicleId) {
            this.vehicleId = vehicleId;
        }

        public long getUserId() {
            return userId;
        }

        public void setUserId(long userId) {
            this.userId = userId;
        }

        public String getLicensePlate() {
            return licensePlate;
        }

        public void setLicensePlate(String licensePlate) {
            this.licensePlate = licensePlate;
        }

        public String getVehicleType() {
            return vehicleType;
        }

        public void setVehicleType(String vehicleType) {
            this.vehicleType = vehicleType;
        }

        // אין צורך בגטרים וסטרים הודות ל-Lombok
    }




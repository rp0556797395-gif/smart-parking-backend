package com.example.parking.Entities;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@NoArgsConstructor
    @AllArgsConstructor
    @Data
    @ToString
    @Builder
    @Entity
    public class Vehicles {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long vehicleId;      // מזהה ייחודי לרכב
        private String licensePlate; // מספר רישוי הרכב
        //private long spotId;         // מזהה החניה שבה הרכב חנה (מצביע על ParkingSpot)
        private Long userId;
        private String vehicleType;    // למשל: "נכים", "חשמלי", "רגיל"


    // החיבור האוטומטי בצד של הרכב:
    // עדכני את השדה המקשר החדש בצורה הבאה:
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false) // <-- התיקון כאן!
    @JsonIgnoreProperties({"vehicles", "hibernateLazyInitializer", "handler"})
    private Users user;


    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public void setUser(Users user) {
        this.user = user;
    }
}




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
public class Parking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // הוסיפי את השורה הזו בדיוק כאן    private long spotId;          // מזהה ייחודי לחניה
    private long spotId; // וודאי שזה השם המדויק!

    private String location;      // מיקום (למשל: "Floor 1, Row A, Spot 5")
    private long floor;           // שיניתי ל-long כדי שיתאים ל-Service
    private boolean isOccupied;   // האם תפוסה
    private long nextAvailableTime;
    private double pricePerHour;  // מחיר לשעה
    private String spotType;      // נכים, חשמלי, רגיל

    // שימי לב: האות הראשונה קטנה! זה קריטי ל-Lombok ול-Spring
    private String currentVehicleId;
    // אין צורך בגטרים וסטרים הודות ל-Lombok


    public long getSpotId() { return spotId; }
    public void setSpotId(long spotId) { this.spotId = spotId; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public long getFloor() { return floor; }
    public void setFloor(long floor) { this.floor = floor; }

    public boolean getisOccupied() { return isOccupied; }
    public void setOccupied(boolean occupied) { isOccupied = occupied; }

    public long getNextAvailableTime() { return nextAvailableTime; }
    public void setNextAvailableTime(long nextAvailableTime) { this.nextAvailableTime = nextAvailableTime; }

    public double getPricePerHour() { return pricePerHour; }
    public void setPricePerHour(double pricePerHour) { this.pricePerHour = pricePerHour; }

    public String getSpotType() { return spotType; }
    public void setSpotType(String spotType) { this.spotType = spotType; }

    public String getCurrentVehicleId() { return currentVehicleId; }
    public void setCurrentVehicleId(String currentVehicleId) { this.currentVehicleId = currentVehicleId; }
}
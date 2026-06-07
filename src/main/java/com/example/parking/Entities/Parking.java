package com.example.parking.Entities;


import jakarta.persistence.*;
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
    private long spotId; //// וודאי שזה השם המדויק!
    @Column(name = "parking_row") // שינינו מ-row ל-parking_row
    private String row;    private String location;     // מיקום (למשל: "Floor 1, Row A, Spot 5")
    private long floor;           // שיניתי ל-long כדי שיתאים ל-Service
    // במקום @Column(name = "\"index\"")
    @Column(name = "spot_index")
    private Long index;           // שיניתי ל-long כדי שיתאים ל-Service
    public boolean  isOccupied;   // האם תפוסה
    private long nextAvailableTime;
    private double pricePerHour;  // מחיר לשעה
    private String spotType;
    private String currentVehicleId;





    }
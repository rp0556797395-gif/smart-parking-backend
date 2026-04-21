package com.example.parking.Controller;

import com.example.parking.Entities.Parking;
import com.example.parking.Service.ParkingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/parking")
public class ParkingController {

    private final ParkingService parkingService;

    public ParkingController(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

    @PostMapping("/enter/{plate}")
    public ResponseEntity<String> startParking(@PathVariable String plate, @RequestParam(required = false) Integer hours) {

        // אם המשתמש לא הכניס שעות, אנחנו נכניס 0 במקומו
        int hoursToProcess = (hours == null) ? 0 : hours;
        // עכשיו שולחים את הלוחית ואת מספר השעות ל-Service
        String result = parkingService.startParking(plate, hoursToProcess);

        if (result.contains("מצטערים") || result.contains("כבר רשום") || result.contains("⚠️")) {
            return ResponseEntity.status(400).body(result);
        }

        return ResponseEntity.ok(result);
    }
    // יציאה מהחניון - חישוב תשלום ושחרור חניה
    @PostMapping("/exit/{plate}")
    public ResponseEntity<String> vehicleExit(@PathVariable String plate,@RequestParam  String method, @RequestParam  String details) {
        String result = parkingService.endParking(plate,  method, details);//APP CREDIT_CARD

        if (result.contains("לא נמצא")) {
            return ResponseEntity.status(404).body(result);
        }

        return ResponseEntity.ok(result);
    }
    @GetMapping("/all")
    public ResponseEntity<List<Parking>> getAllSpots() {
        return ResponseEntity.ok(parkingService.getAllSpots());
    }

    @GetMapping("/floor/{floor}")
    public ResponseEntity<List<Parking>> getSpotsByFloor(@PathVariable Integer floor) {
        return ResponseEntity.ok(parkingService.getSpotsByFloor(floor));
    }

    @GetMapping("/available")
    public ResponseEntity<Map<Long, Integer>> getAvailableCountPerFloor() {
        return ResponseEntity.ok(parkingService.getAvailableCountPerFloor());
    }

    @GetMapping("/suggested/{plate}")
    public ResponseEntity<?> getSuggestedSpot(@PathVariable String plate) {
        Parking spot = parkingService.getSuggestedSpot(plate);
        if (spot == null) {
            return ResponseEntity.status(404).body("מצטערים, אין חניות פנויות כרגע במערכת.");
        }
        return ResponseEntity.ok(spot);
    }

    @PutMapping("/update-status/{spotId}")
    public ResponseEntity<String> updateSpotStatus(@PathVariable Long spotId, @RequestParam Boolean isOccupied) {
        parkingService.updateSpotStatus(spotId, isOccupied);
        String message = isOccupied ? "חניה " + spotId + " נתפסה." : "חניה " + spotId + " שוחררה.";
        return ResponseEntity.ok(message);
    }

    //"אני נמצא כרגע בכניסה (1), איך אני מגיע לחניה הפנויה שהצעת לי (13)?"
    @GetMapping("/navigation")
    public ResponseEntity<List<List<Integer>>> getNavigationPath(@RequestParam Long startId, @RequestParam Long targetId) {
        return ResponseEntity.ok(parkingService.getNavigationPath(startId, targetId));
    }

//מיקום רכב חונה
    @GetMapping("/find/{plate}")
    public ResponseEntity<Parking> findVehicleLocation(@PathVariable String plate) {
        Parking spot = parkingService.findVehicleLocation(plate);
        return spot != null ? ResponseEntity.ok(spot) : ResponseEntity.notFound().build();
    }
    @GetMapping("/setup-test")
    public ResponseEntity<String> setupTest() {
        return ResponseEntity.ok(parkingService.createTestData());
    }

}
package com.example.parking.Controller;

import com.example.parking.Entities.Vehicles;
import com.example.parking.Service.VehiclesService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin(origins = "*")
public class VehiclesController {
@Autowired
    private final VehiclesService vehiclesService;

    public VehiclesController(VehiclesService vehiclesService) {
        this.vehiclesService = vehiclesService;
    }

    /**
     * הוספת רכב חדש למערכת
     * דוגמה לגוף הבקשה (JSON):
     * {
     * "licensePlate": "12-345-67",
     * "userId": 1,
     * "model": "Mazda 3",
     * "color": "White"
     * }
     */


    /**
     * הצגת כל הרכבים הרשומים (לצורכי בקרה)
     */
//    @GetMapping("/all")
//    public List<Vehicles> getAllVehicles() {
//        return vehiclesService.getAllVehicles();
//    }

    /**
     * מציאת רכבים לפי ID של משתמש
     */
//    @GetMapping("/user/{userId}")
//    public List<Vehicles> getVehiclesByUser(@PathVariable long userId) {
//        return vehiclesService.getVehiclesByUserId(userId);
//    }



    // הוספת רכב חדש למשתמש
    @PostMapping("/add/{userId}")
    public ResponseEntity<Vehicles> addVehicle(@PathVariable Long userId, @RequestBody Vehicles vehicle) {
        return ResponseEntity.ok(vehiclesService.addVehicle(userId,vehicle));
    }

    // הצגת כל הרכבים של משתמש ספציפי (לעמוד My Garage)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Vehicles>> getAllUserVehicles(@PathVariable Long userId) {
        return ResponseEntity.ok(vehiclesService.getAllUserVehicles(userId));
    }

    // שליפת רכב לפי לוחית רישוי (עבור בדיקות פנימיות)
    @GetMapping("/plate/{plate}")
    public ResponseEntity<Vehicles> getVehicleByPlate(@PathVariable String plate) {
        Vehicles vehicle = vehiclesService.getVehicleByPlate(plate);
        return vehicle != null ? ResponseEntity.ok(vehicle) : ResponseEntity.notFound().build();
    }

    // בדיקה קריטית: האם הרכב מורשה להיכנס עכשיו?
    @GetMapping("/is-authorized/{plate}")
    public ResponseEntity<Boolean> isAuthorized(@PathVariable String plate) {
        return ResponseEntity.ok(vehiclesService.isAuthorized(plate));
    }
}

package com.example.parking.Controller;

import com.example.parking.Entities.Parking;
import com.example.parking.Entities.GraphNode;
import com.example.parking.Service.ParkingGraphService;
import com.example.parking.Service.ParkingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/parking")
public class ParkingController {

    private final ParkingService parkingService;
    private final ParkingGraphService parkingGraphService;

    // הזרקת ה-Services בצורה אחידה דרך הבנאי
    public ParkingController(ParkingService parkingService, ParkingGraphService parkingGraphService) {
        this.parkingService = parkingService;
        this.parkingGraphService = parkingGraphService;
    }

    @GetMapping("/graph-map/{floor}")
    public ResponseEntity<Map<String, List<String>>> getGraphMapByFloor(@PathVariable long floor) {
        Map<String, List<String>> floorGraph = new HashMap<>();
        Map<GraphNode, List<GraphNode>> fullGraph = parkingGraphService.getAdjacencyList();

        for (var entry : fullGraph.entrySet()) {
            String nodeId = entry.getKey().getId();

            // סינון: לוקחים חוליות של הקומה הנוכחית או מעליות גלובליות שמחברות את הקומות
            if (nodeId.startsWith(floor + "-") || nodeId.startsWith("F" + floor + "-") || nodeId.contains("Elevator")) {

                List<String> neighborsIds = entry.getValue().stream()
                        .map(GraphNode::getId)
                        .toList();

                floorGraph.put(nodeId, neighborsIds);
            }
        }
        return ResponseEntity.ok(floorGraph);
    }

    @GetMapping("/path")
    public ResponseEntity<List<String>> getPath(@RequestParam String startId, @RequestParam String targetId) {

        List<String> path = parkingGraphService.calculatePath(startId, targetId);

        return ResponseEntity.ok(path);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllSpots() {
        try {
            List<Parking> spots = parkingService.getAllSpots();
            return ResponseEntity.ok(spots);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/enter/{plate}")
    public ResponseEntity<String> startParking(
            @PathVariable String plate,
            @RequestParam(required = false) Integer hours,
            @RequestParam(required = false) String arrivalDate) {

        int hoursToProcess = (hours == null) ? 0 : hours;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            System.out.println("DEBUG: Authenticated user: " + auth.getName());
        }

        String result = parkingService.startParking(plate, hoursToProcess, arrivalDate);

        if (result.contains("מצטערים") || result.contains("כבר רשום") || result.contains("⚠️")) {
            return ResponseEntity.status(400).body(result);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/exit/{plate}")
    public ResponseEntity<String> vehicleExit(
            @PathVariable String plate,
            @RequestParam String method,
            @RequestParam String details) {

        String result = parkingService.endParking(plate, method, details);

        if (result.contains("לא נמצא")) {
            return ResponseEntity.status(404).body(result);
        }
        return ResponseEntity.ok(result);
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

    @GetMapping("/navigation")
    public ResponseEntity<List<List<Integer>>> getNavigationPath(@RequestParam Long startId, @RequestParam Long targetId) {
        return ResponseEntity.ok(parkingService.getNavigationPath(startId, targetId));
    }

    @GetMapping("/find/{plate}")
    public ResponseEntity<Parking> findVehicleLocation(@PathVariable String plate) {
        Parking spot = parkingService.findVehicleLocation(plate);
        return spot != null ? ResponseEntity.ok(spot) : ResponseEntity.notFound().build();
    }


    @PostMapping("/gate-entry/{plate}")
    public ResponseEntity<String> processGateEntry(@PathVariable String plate) {
        String result = parkingService.processGateEntry(plate);

        if (result.contains("❌") || result.contains("⚠️")) {
            return ResponseEntity.status(403).body(result);
        }
        return ResponseEntity.ok(result);
    }
    @GetMapping("/check-availability")
    public ResponseEntity<?> checkSpotAvailability(@RequestParam long spotId, @RequestParam int hours) {
        boolean available = parkingService.isSpotAvailable(spotId, hours);

        if (available) {
            return ResponseEntity.ok(Stream.of("available", true).collect(Collectors.toMap(e -> e, e -> e)));
            // או פשוט להחזיר אובייקט/מפה פשוטה: Map.of("available", true)
        } else {
            return ResponseEntity.ok(Map.of("available", false, "message", "החניה תפוסה או שוריינה ע\"י משתמש אחר"));
        }
    }
}
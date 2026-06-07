package com.example.parking.Controller;

import com.example.parking.Entities.Transactions;
import com.example.parking.Service.ParkingService;
import com.example.parking.Service.TransactionsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/transactions")
public class TransactionsController {

    private final TransactionsService transactionsService;
    private final ParkingService parkingService;

    public TransactionsController(ParkingService parkingService,TransactionsService transactionsService) {
        this.transactionsService = transactionsService;
        this.parkingService = parkingService;
    }



    @PostMapping("/pre-book")
    public ResponseEntity<String> preBookParking(
            @RequestParam String plate,
            @RequestParam String arrivalDate, // פורמט: yyyy-MM-dd HH:mm
            @RequestParam int durationHours) {

        try {
            String result = parkingService.createPreBooking(plate, arrivalDate, durationHours);
            if (result.contains("❌")) return ResponseEntity.badRequest().body(result);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ שגיאה בעיבוד התאריך. וודאי פורמט: yyyy-MM-dd HH:mm");
        }
    }

    // ה. זימון היסטוריה: מחזיר רשימת חניות קודמות עבור משתמש
    @GetMapping("/user/{userId}/history")
    public ResponseEntity<List<Transactions>> getHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(transactionsService.getUserHistory(userId));
    }

    // ו. זימון דוח מנהל: מחזיר נתונים כספיים וסטטיסטיים לממשק הניהול
    @GetMapping("/admin/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(transactionsService.getGeneralStatistics());
    }

    // ז. זימון שליטה ידנית: מדמה פתיחה או סגירה ידנית של מחסומים (לוג בלבד)
    @PostMapping("/admin/gate-control")
    public ResponseEntity<String> gateControl(@RequestParam Integer gateId, @RequestParam String command) {
        // זימון הלוגיקה מתוך הסרוויס
        String response = transactionsService.performManualGateControl(gateId, command);

        return ResponseEntity.ok(response);
    }
}
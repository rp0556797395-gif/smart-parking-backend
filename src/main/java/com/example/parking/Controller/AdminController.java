package com.example.parking.Controller;

import com.example.parking.Entities.Transactions;
import com.example.parking.Service.AdminService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/daily-summary")
    public Map<String, Object> getDailySummary() {
        return adminService.getDailyReport();
    }

    /**
     * Endpoint לקבלת סיכום נתונים יומי עבור ה-Dashboard
     */

    /**
     * Endpoint לקבלת רשימת רכבים שחונים מעל 24 שעות (התראות פיקוח)
     */
    @GetMapping("/alerts/overstay")
    public List<Transactions> getOverstayAlerts() {
        return adminService.getOverstayingVehicles();
    }
    @GetMapping("/dashboard")
    public Map<String, Object> getAdminDashboard() {
        // קריאה לסרוויס שיבצע את שלושת החישובים שביקשת
        Map<String, Object> stats = adminService.getAdminStats();

        // הוספת תיעוד לתשובה כדי שיהיה ברור מה חוזר ל-React
        // 1. "revenueToday" -> כמה כסף החניון הרוויח היום
        // 2. "starsGivenToday" -> כמה כוכבים חולקו היום
        // 3. "mostPopularSpot" -> מקום החניה הכי מבוקש

        return stats;
    }
}

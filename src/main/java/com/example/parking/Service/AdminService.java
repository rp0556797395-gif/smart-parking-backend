package com.example.parking.Service;

import com.example.parking.Entities.Transactions;
import com.example.parking.Reposetories.AdminRepo;
import com.example.parking.Reposetories.TransactionsRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService extends ClientService{

    private final TransactionsRepo transactionsRepo;
    private final AdminRepo adminRepo;

    public AdminService( AdminRepo adminRepo,TransactionsRepo transactionsRepo) {
        this.transactionsRepo = transactionsRepo;
        this.adminRepo = adminRepo;
    }

    public Map<String, Object> getDailyReport() {
        long startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

        // מושכים את כל העסקאות של היום כרשימה
        List<Transactions> todayTransactions = adminRepo.findByEndTimeGreaterThanEqual(startOfDay);

        // חישוב הכנסות בעזרת Stream (במקום SQL SUM)
        double totalRevenue = todayTransactions.stream()
                .mapToDouble(Transactions::getTotalPayment)
                .sum();

        // מציאת החניה הכי פופולרית בעזרת Grouping (במקום SQL GROUP BY)
        Long mostPopularSpot = todayTransactions.stream()
                .collect(Collectors.groupingBy(Transactions::getSpotId, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        Map<String, Object> report = new HashMap<>();
        report.put("totalRevenue", totalRevenue);
        report.put("transactionsCount", todayTransactions.size());
        report.put("mostPopularSpotId", mostPopularSpot);

        return report;
    }
    public List<Transactions> getOverstayingVehicles() {
        long twentyFourHoursAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000);

        // סינון עסקאות שטרם הסתיימו (paymentStatus=false) והתחילו לפני יותר מ-24 שעות
        return transactionsRepo.findAll().stream()
                .filter(t -> !t.isPaymentStatus() && t.getStartTime() < twentyFourHoursAgo)
                .collect(Collectors.toList());
    }
    /**
     * פונקציה המפיקה דוח ניהולי עם שלושת הנתונים שביקשת
     */
    public Map<String, Object> getAdminStats() {
        // חישוב תחילת היום הנוכחי במילישניות (00:00)
        long startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

        // שליפת כל העסקאות שהסתיימו מהיום והלאה מה-Repository
        List<Transactions> todayTransactions = adminRepo.findByEndTimeGreaterThanEqual(startOfDay);

        // 1. כמה כסף החניון הרוויח היום?
        // סכימה של כל שדות ה-totalPayment מרשימת העסקאות של היום
        double totalRevenue = todayTransactions.stream()
                .mapToDouble(Transactions::getTotalPayment)
                .sum();

        // 2. כמה כוכבים חולקו היום למשתמשים?
        // לפי הלוגיקה: כוכב אחד על כל 10 ש"ח מהרווח היומי
        int totalStarsGiven = (int) (totalRevenue / 10);

        // 3. אילו מקומות חניה הם הכי "מבוקשים"?
        // סופרים כמה פעמים כל spotId מופיע בעסקאות היום ומחזירים את המבוקש ביותר
        Long mostPopularSpotId = todayTransactions.stream()
                .collect(Collectors.groupingBy(Transactions::getSpotId, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        // אריזה של שלושת הנתונים במפה מסודרת
        Map<String, Object> results = new HashMap<>();
        results.put("revenueToday", totalRevenue);           // סעיף 1: רווח
        results.put("starsGivenToday", totalStarsGiven);    // סעיף 2: כוכבים
        results.put("mostPopularSpot", mostPopularSpotId);  // סעיף 3: מקום מבוקש

        return results;
    }
}
package com.example.parking.Reposetories;

import com.example.parking.Entities.Transactions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdminRepo extends JpaRepository<Transactions, Long> {

    // 1. מציאת כל העסקאות שבוצעו מהזמן שניתן (היום)
    List<Transactions> findByEndTimeGreaterThanEqual(long startOfDay);

    // 2. מציאת עסקאות לפי משתמש וסטטוס תשלום (כפי שביקשת קודם)
    List<Transactions> findByUserIdAndPaymentStatus(long userId, boolean status);

    // 3. מציאת עסקה פעילה לפי רכב (כדי לסגור חניה)
    Optional<Transactions> findByVehicleIdAndPaymentStatusFalse(String vehicleId);
}

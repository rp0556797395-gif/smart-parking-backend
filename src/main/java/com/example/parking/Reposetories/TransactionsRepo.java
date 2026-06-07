package com.example.parking.Reposetories;
import com.example.parking.Entities.Transactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionsRepo extends JpaRepository<Transactions, Long> {

//    // מוצא את החניה הפעילה של רכב מסוים (לפי לוחית רישוי וזמן סיום שהוא 0 או null)
//    // זה קריטי לרגע שהרכב לוחץ על "סיום חניה"
//    Optional<TransactionsRepo> findByLicensePlateAndEndTime(String licensePlate, long endTime);
//
//    // מציאת היסטוריית חניות של רכב מסוים
//    List<TransactionsRepo> findByLicensePlate(String licensePlate);
//
//    // בדיקה אם רכב נמצא כרגע בחניה (endTime == 0)
//    boolean existsByLicensePlateAndEndTime(String licensePlate, long endTime);

    // מוצא את כל העסקאות של רכב מסוים (להיסטוריה)
    List<Transactions> findByvehicleId(String vehicleId);

    // מוצא עסקה פעילה (כשה-endTime הוא 0)
    Optional<Transactions> findByvehicleIdAndEndTime(String vehicleId, long endTime);

    // בודק אם רכב כבר חונה כרגע
    //boolean existsByvehicleIdAndEndTime(String vehicleId, long endTime);
    List<Transactions>  findActiveByvehicleId(String plate, LocalDateTime now);


    // מוצא את כל הרכבים שחונים כרגע בחניון
    List<Transactions> findAllByEndTime(long endTime);
    List<Transactions> findByUserId(Long id);
///  ////////////////////////////////
    Optional<Transactions> findByVehicleIdAndPaymentStatusFalse(long vehicleId);

    List<Transactions> findByPaymentStatusFalse();

    // בתוך TransactionsRepository.java
    Optional<Transactions> findFirstByVehicleIdAndPaymentStatusFalse(String vehicleId);

    @Query("SELECT t FROM Transactions t WHERE t.paymentStatus = false AND t.startTime < :expectedEndTime AND t.endTime > :now")
    List<Transactions> findActiveTransactionsInTimeRange(@Param("now") long now, @Param("expectedEndTime") long expectedEndTime);

    @Query("SELECT t FROM Transactions t WHERE t.spotId = :spotId AND t.paymentStatus = false AND t.startTime < :expectedEndTime AND t.endTime > :now")
    List<Transactions> findConflictsForSpecificSpot(long spotId, long now, long expectedEndTime);
}
package com.example.parking.Service;
import com.example.parking.Entities.Parking;
import com.example.parking.Entities.Transactions;
import com.example.parking.Entities.Vehicles;
import com.example.parking.Reposetories.ParkingRepo;
import com.example.parking.Reposetories.TransactionsRepo;
import com.example.parking.Reposetories.UsersRepo;
import com.example.parking.Reposetories.VehiclesRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TransactionsService extends ClientService{

    private final TransactionsRepo transactionRepo;
    private final ParkingRepo parkingRepo;
    private final VehiclesRepo vehiclesRepo;
    private final UsersRepo usersRepo;

    public TransactionsService(TransactionsRepo transactionRepo, ParkingRepo parkingRepo,
                               VehiclesRepo vehiclesRepo, UsersRepo usersRepo) {
        this.transactionRepo = transactionRepo;
        this.parkingRepo = parkingRepo;
        this.vehiclesRepo = vehiclesRepo;
        this.usersRepo = usersRepo;
    }

    // ה. היסטוריית חניות של משתמש
    public List<Transactions> getUserHistory(Long userId) {
        return transactionRepo.findAll().stream()
                .filter(t -> userId.equals(t.getUserId()))
                .collect(Collectors.toList());
    }

    // ו. דוח סטטיסטיקה למנהל
    public Map<String, Object> getGeneralStatistics() {
        List<Transactions> paid = transactionRepo.findAll().stream()
                .filter(Transactions::isPaymentStatus).collect(Collectors.toList());

        double revenue = paid.stream().mapToDouble(Transactions::getTotalPayment).sum();

        return Map.of(
                "totalRevenue", revenue,
                "totalServed", paid.size(),
                "activeNow", transactionRepo.findAll().stream().filter(t -> !t.isPaymentStatus()).count()
        );
    }

    // פונקציה לשליטה ידנית במחסומים (Admin Only)
    public String performManualGateControl(Integer gateId, String command) {
        // 1. נרמול הפקודה (הפיכה לאותיות גדולות)
        String action = command.toUpperCase();

        // 2. רישום בלוֹג של השרת (לצורכי אבטחה ובקרה)
        System.out.println("📢 התראה: בוצעה שליטה ידנית!");
        System.out.println("מחסום מספר: " + gateId);
        System.out.println("פעולה שבוצעה: " + action);
        System.out.println("זמן אירוע: " + LocalDateTime.now());

        // 3. כאן במציאות תבוא פקודה לבקר ה-IoT (כמו Arduino)
        // if(action.equals("OPEN")) { hardwareProvider.openGate(gateId); }

        return "מחסום " + gateId + " הופעל בהצלחה לפעולה: " + action;
    }

}
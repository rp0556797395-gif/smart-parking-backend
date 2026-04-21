package com.example.parking.Service;
import com.example.parking.Entities.Transactions;
import com.example.parking.Entities.Users;
import com.example.parking.Entities.Vehicles;
import com.example.parking.Exceptions.ResourceNotFoundException;
import com.example.parking.Reposetories.*;
import com.example.parking.Reposetories.VehiclesRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service

public class VehiclesService {

    private final VehiclesRepo vehiclesRepo;
    private final UsersRepo usersRepo;
    private final TransactionsRepo transactionsRepo;

    public VehiclesService(VehiclesRepo vehiclesRepo, UsersRepo usersRepo, TransactionsRepo transactionsRepo) {
        this.vehiclesRepo = vehiclesRepo;
        this.usersRepo = usersRepo;
        this.transactionsRepo = transactionsRepo;
    }

    public Vehicles addVehicle(Long userId, Vehicles vehicle) {
        Users user = usersRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        vehicle.setUserId(userId); // שיוך הרכב למשתמש
        return vehiclesRepo.save(vehicle);
    }

    public List<Vehicles> getAllUserVehicles(Long userId) {
        return vehiclesRepo.findByUserId(userId);
    }

    public Vehicles getVehicleByPlate(String plate) {
        return vehiclesRepo.findByLicensePlate(plate);
    }

    public Boolean isAuthorized(String plate) {
        Vehicles vehicle = vehiclesRepo.findByLicensePlate(plate);
        if (vehicle == null) return false;

        Users user = usersRepo.findById(vehicle.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        // בדיקה 1: האם בעל הרכב הוא מנוי (Membership)?
        if ("MANUI".equalsIgnoreCase(user.getType())) {
            return true;
        }

        // בדיקה 2: האם יש לרכב הזמנה פעילה (Reservation) לרגע זה?
        List<Transactions> activeReservations = transactionsRepo.findActiveByvehicleId(plate, LocalDateTime.now());
        return !activeReservations.isEmpty();
    }
}
package com.example.parking.Reposetories;

import com.example.parking.Entities.Vehicles;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
    public interface VehiclesRepo extends JpaRepository<Vehicles, String> {
    // מציאת כל הרכבים של משתמש ספציפי
    List<Vehicles> findByUserId(long userId);

    // האם הרכב הזה כבר רשום במערכת?
    // boolean existsByLicensePlate(String licensePlate);


    Vehicles findByLicensePlate(String licensePlate);




}
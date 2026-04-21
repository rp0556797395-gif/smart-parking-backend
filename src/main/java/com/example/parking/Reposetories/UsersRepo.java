package com.example.parking.Reposetories;


import com.example.parking.Entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsersRepo extends JpaRepository<Users, Long> {
    // האם קיים משתמש עם הטלפון הזה?
    boolean existsByPhoneNumber(String phoneNumber);

    // מציאת משתמש לפי מספר טלפון
    Users findByPhoneNumber(String phoneNumber);

}
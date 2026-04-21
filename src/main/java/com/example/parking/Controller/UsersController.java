package com.example.parking.Controller;
import com.example.parking.Entities.Transactions;
import com.example.parking.Entities.Users;
import com.example.parking.Exceptions.ResourceNotFoundException;
import com.example.parking.Service.ParkingService;
import com.example.parking.Service.TransactionsService;
import com.example.parking.Service.UsersService;
import com.example.parking.Service.VehiclesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users") // הכתובת הבסיסית
public class UsersController {

    private final UsersService usersService;
    private final VehiclesService vehiclesService;
    private final TransactionsService transactionsService;

    public UsersController(UsersService usersService, VehiclesService vehiclesService,TransactionsService transactionsService) {
        this.usersService = usersService;
        this.vehiclesService = vehiclesService;
        this.transactionsService = transactionsService;
    }
    @GetMapping("/getAllUsers")
    public List<Users> getAllUsers() {
        return usersService.getAllUsers();
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam long id) {
        Optional<Users> user = usersService.getUserById(id);
        if (user.isPresent()) {
            return "ברוך הבא " + user.get().getFullName() + "! הגעת בהצלחה.";
        } else {
            return "שגיאה: לא נמצא משתמש עם מספר מזהה זה.";
        }
    }
    /**
     * בדיקת יתרת כוכבים/נקודות של משתמש
     */
    @PostMapping("/register")
    public String registerUser(@RequestBody Users newUser) {
        try {
            // נרשם את המשתמש ונותנים לו 50 כוכבים בהתחלה
            Users savedUser = usersService.addNewUser(newUser);
            return "המשתמש " + savedUser.getFullName() + " נרשם בהצלחה.";
        } catch (RuntimeException e) {
            return e.getMessage();  // אם קיים כבר משתמש עם אותו טלפון, תחזור שגיאה
        }
    }

    @GetMapping("/{userId}/points")
    public String getUserPoints(@PathVariable long userId) {
        return usersService.getUserById(userId)
                .map(u -> "User has " + u.getStars() + " stars.")
                .orElse("User not found.");
    }
    @GetMapping("/getUserProfile/{userId}")
    public Optional<Users> getUserProfile(Long userId)  {
        return usersService.getUserById(userId);
    }

    @PutMapping("/updateToManui/{userId}/{type}") // שימוש ב-Put כי אנחנו מעדכנים נתון קיים
    public Users updateToManui(@PathVariable Long userId, @PathVariable String type) {
        return usersService.updateToManui(userId, type);
    }

    @GetMapping("/{userId}/history")
    public ResponseEntity<List<Transactions>> getUserHistory(@PathVariable Long userId) {
    // בדיקה ראשונית - האם המשתמש בכלל קיים במערכת?
    System.out.println("Searching history for user: ");
    if (!usersService.existsById(userId)) {
        return ResponseEntity.notFound().build();
    }

    List<Transactions> history = usersService.getUserHistory(userId);

    // החזרת הרשימה עם קוד הצלחה 200
    return ResponseEntity.ok(history);
    }
}

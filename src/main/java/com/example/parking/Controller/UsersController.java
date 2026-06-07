package com.example.parking.Controller;
import com.example.parking.Entities.Transactions;
import com.example.parking.Entities.Users;
import com.example.parking.Entities.Vehicles;
import com.example.parking.Exceptions.ResourceNotFoundException;
import com.example.parking.Service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/users") // הכתובת הבסיסית
public class UsersController {

    private final UsersService usersService;
    private final TransactionsService transactionsService;

    public UsersController( UsersService usersService, VehiclesService vehiclesService,TransactionsService transactionsService) {
        this.usersService = usersService;
        this.transactionsService = transactionsService;
    }



    @GetMapping("/getAllUsers")
    public List<Users> getAllUsers() {
        return usersService.getAllUsers();
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> loginRequest) {
        try {
            // 1. חילוץ האימייל והסיסמה שהגיעו מה-React
            String idStr = loginRequest.get("userId");

// 2. ממירים למספר ארוך (Long) בצורה בטוחה
            Long id = Long.parseLong(idStr);
            String password = loginRequest.get("password");

            // 2. הפעלת ה-Service: הוא בודק שהמשתמש קיים, שהסיסמה נכונה, ומחזיר טוקן JWT
            String token = usersService.login(id, password);

            // 3. החזרת הטוקן ל-React במבנה JSON מסודר (סטטוס 200 OK)
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "message", "התחברת בהצלחה!"
            ));

        } catch (RuntimeException e) {
            // אם ה-Login נכשל (אימייל לא קיים או סיסמה שגויה), נחזיר שגיאה 401 (Unauthorized)
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Users newUser) { // משנים ל-ResponseEntity כדי להחזיר אובייקט מסודר
        try {
            // 1. מפעילים את ה-Service ומקבלים את הטוקן שנוצר עבור המשתמש החדש
            String token = usersService.addNewUser(newUser);

            // 2. מחזירים ל-React את הטוקן בתוך JSON (בדיוק כמו שעשינו ב-Login!)
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "message", "המשתמש נרשם וחובר בהצלחה!"
            ));

        } catch (RuntimeException e) {
            // אם הייתה שגיאה (אימייל קיים וכד'), נחזיר שגיאה 400 ל-React
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }



    @GetMapping("/{userId}/points")
    public String getUserPoints(@PathVariable long userId) {
        return usersService.getUserById(userId)
                .map(u -> "User has " + u.getStars() + " stars.")
                .orElse("User not found.");
    }
    @GetMapping("/profile")
    public ResponseEntity<Users> getMyProfile(Principal principal) {
        // 1. מחלצים את ה-ID מהטוקן (ה-subject שהכנסנו לטוקן הוא ה-userId)
        Long userId = Long.parseLong(principal.getName());

        // 2. משתמשים ב-ID כדי למצוא את המשתמש
        return usersService.getUserById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @PutMapping("/updateToManui/{userId}/{type}") // שימוש ב-Put כי אנחנו מעדכנים נתון קיים
    public Users updateToManui(@PathVariable Long userId, @PathVariable String type) {
        return usersService.updateToManui(userId, type);
    }


    @GetMapping("/history")
// שינוי סוג ההחזרה ב-ResponseEntity ל-List של Transactions
    public ResponseEntity<List<Transactions>> getUserHistory(Principal principal) {
        try {
            // מחלצים את ה-ID מהטוקן
            Long userId = Long.parseLong(principal.getName());

            System.out.println("Searching history for userאאאא ID: " + userId);

            // שליפת רשימת העסקאות מהשירות
            List<Transactions> history = transactionsService.getUserHistory(userId);

            System.out.println("Searching history for userאאאא ID: " + userId);

            // החזרת הרשימה כפי שהיא, ללא Casting שגוי
            return ResponseEntity.ok(history);

        } catch (Exception e) {
            // שינוי לסטטוס 500 (שגיאת שרת פנימית) כדי לשקף את המציאות בצורה נכונה
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/addVehicle")
    public ResponseEntity<?> addVehicleToUser(@RequestBody Vehicles vehicle, Principal principal) {
        try {
            // חילוץ המזהה מהטוקן
            Long userId = Long.parseLong(principal.getName());

            // העברת הנתונים לסרוויס שיבצע את הקשירה למשתמש ושמירה ב-DB
            Vehicles savedVehicle = usersService.addVehicle(userId, vehicle);

            return ResponseEntity.ok(savedVehicle);
        } catch (Exception e) {
            return ResponseEntity.status(400).body("שגיאה בהוספת רכב: " + e.getMessage());
        }
    }
}

package com.example.parking.Service;
import com.example.parking.Entities.Transactions;
import com.example.parking.Entities.Users;
import com.example.parking.Entities.Vehicles;
import com.example.parking.Exceptions.ResourceNotFoundException;
import com.example.parking.Reposetories.TransactionsRepo;
import com.example.parking.Reposetories.UsersRepo;
import com.example.parking.Reposetories.VehiclesRepo;
import com.example.parking.jwt.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;

@Service

public class UsersService extends ClientService{

    private final UsersRepo usersRepo;
    private final TransactionsRepo transactionsRepo;
    private TransactionsRepo transactionRepo;
    private VehiclesRepo  vehicleRepo;

    public UsersService(VehiclesRepo  vehicleRepo,UsersRepo usersRepo, TransactionsRepo transactionsRepo) {
        this.usersRepo = usersRepo;
        this.transactionsRepo = transactionsRepo;
        this.vehicleRepo=vehicleRepo;
    }

    // הוספת משתמש חדש
    public Users saveUser(Users user) {
       return usersRepo.save(user);
    }

    @Autowired
    private PasswordEncoder passwordEncoder; // מוזרק מתוך ה-SecurityConfig שלך

    @Autowired
    private JwtUtil jwtUtil;
    public String addNewUser(Users newUser) {
        // 1. בדיקה אם המשתמש כבר קיים במערכת לפי אימייל
        if (!usersRepo.findByEmail(newUser.getEmail()).isEmpty()) {
            throw new RuntimeException("משתמש עם אימייל זה כבר קיים במערכת");
        }

        // 2. הצפנת הסיסמה של המשתמש הנוכחי!
        String encodedPassword = passwordEncoder.encode(newUser.getPassword());
        newUser.setPassword(encodedPassword); // מעדכנים את הסיסמה לסיסמה המוצפנת

        // 3. הגדרת ברירת מחדל (למשל: לתת לו 50 כוכבים או להגדיר סוג מנוי)
        // newUser.setStars(50);

        // 4. שמירה סופית בבסיס הנתונים
         usersRepo.save(newUser);
        // 4. יצירת טוקן אוטומטי למשתמש שנרשם עכשיו!
        String token = jwtUtil.generateToken(newUser.getEmail(),newUser.getPassword(),newUser.getUserId());

        return token; //
    }
    // קבלת כל המשתמשים
    public List<Users> getAllUsers() {
        return usersRepo.findAll();
    }

    public Users updateToManui(Long userId, String type)   {
        Users user = usersRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("משתמש לא נמצא"));
        user.setType(type);
        return usersRepo.save(user);


    }


    public List<Transactions> getUserHistory() {
        // 1. שליפת הנתונים מה-Repository

        // 1. שליפה אוטומטית ומאובטחת של האימייל מהטוקן
        String email = getCurrentUsername();

        // 2. מציאת המשתמש לפי האימייל מהטוקן
        Users user = usersRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("משתמש לא נמצא"));
        List<Transactions> history = transactionsRepo.findByUserId(user.getUserId());

        // 2. בדיקה לצורך תיעוד בלבד (לוגים) - אין צורך לזרוק שגיאה!
        if (history.isEmpty()) {
            System.out.println("למשתמש מספר "  + " אין עדיין היסטוריית חניות/עסקאות במערכת.");
        }

        // החזרת הרשימה (גם אם היא ריקה)
        return history;
    }
    // מציאת משתמש לפי ID
    public Optional<Users> getUserById(long userId) {
        return usersRepo.findById(userId);
    }

    // עדכון יתרה כספית (למשל לאחר תשלום)
//    public void updateBalance(long userId, double amount) {
//        usersRepo.findById(userId).ifPresent(user -> {
//            user.setBalance(user.getBalance() + amount);
//            usersRepo.save(user);
//        });
//    }

    /// //////////////////////////////
    public String updateStarsAfterPayment(long userId, double paymentAmount) {
        // 1. מציאת המשתמש ב-DB (שימוש בחריגה המקצועית שיצרנו קודם!)
        Users user = usersRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("משתמש לא נמצא"));

        // 2. חישוב הכוכבים החדשים (כוכב על כל 10 ש"ח)
        int starsToAdd = (int) (paymentAmount / 10);

        if (starsToAdd > 0) {
            // 3. עדכון היתרה של הכוכבים
            user.setStars(user.getStars() + starsToAdd);
            usersRepo.save(user); // שמירה ב-Database

            return String.format("כל הכבוד! צברת %d כוכבים חדשים. סך הכוכבים שלך כעת: %d",
                    starsToAdd, user.getStars());
        }

        return "תודה על התשלום! חסרים לך עוד כמה שקלים כדי לצבור כוכב נוסף.";
    }

    public List<Transactions> getUserHistoryByUserId(Long userId) {
        // שליפת הנתונים מה-Repository
        List<Transactions> history = transactionRepo.findByUserId(userId);

        System.out.println("Searching history for user22222222222 ID: " + userId);

        // בדיקה: אם הרשימה שחזרה היא null, נחזיר רשימה ריקה (ArrayList)
        // אם היא אינה null, נחזיר אותה כפי שהיא
        return (history != null) ? history : new java.util.ArrayList<>();
    }

    public boolean existsById() {
        // 1. שליפה אוטומטית ומאובטחת של האימייל מהטוקן
        String email = getCurrentUsername();

        // 2. מציאת המשתמש לפי האימייל מהטוקן
        Users user = usersRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("משתמש לא נמצא"));
        return usersRepo.existsById(user.getUserId());
    }

    public String login(long id, String password) {
        // 1. חיפוש המשתמש בבסיס הנתונים לפי האימייל שלו
        Optional<Users> userOpt = usersRepo.findById(id);

        // 2. אם האימייל לא קיים בכלל - זורקים שגיאה
        if (userOpt.isEmpty()) {
            throw new RuntimeException("אימייל או סיסמה שגויים");
        }

        Users user = userOpt.get();

        // 3. בדיקה האם הסיסמה שהמשתמש הקליד מתאימה לסיסמה המוצפנת שב-DB
        // שימי לב: משתמשים ב-passwordEncoder.matches ולא ב-equals רגיל!
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("אימייל או סיסמה שגויים");
        }

        // 4. אם הכל תקין - מייצרים ומחזירים את הטוקן המאובטח עם המייל, ה-userId וה-type שלו
        String token = jwtUtil.generateToken(user.getEmail(), user.getPassword(), user.getUserId());

        return token; // הטוקן חוזר לקונטרולר ומשם ישירות ל-React
    }

    @Transactional
    public Vehicles addVehicle(Long userId, Vehicles vehicle) {
        // 1. שליפת המשתמש ממסד הנתונים כדי לוודא שהוא קיים
        Users user = usersRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // 2. הגדרת השדות של הרכב מהנתונים שהגיעו
        vehicle.setUserId(userId);
        vehicle.setUser(user);

        // 3. שמירת הרכב ב-Repository (הפעולה שכותבת ל-DB)
        return vehicleRepo.save(vehicle);
    }
}
package com.example.parking.Service;
import com.example.parking.Entities.Transactions;
import com.example.parking.Entities.Users;
import com.example.parking.Exceptions.ResourceNotFoundException;
import com.example.parking.Reposetories.TransactionsRepo;
import com.example.parking.Reposetories.UsersRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;

@Service

public class UsersService {

    private final UsersRepo usersRepo;
    private final TransactionsRepo transactionsRepo;

    public UsersService(UsersRepo usersRepo, TransactionsRepo transactionsRepo) {
        this.usersRepo = usersRepo;
        this.transactionsRepo = transactionsRepo;
    }

    // הוספת משתמש חדש
    public Users saveUser(Users user) {
       return usersRepo.save(user);
    }

    public Users addNewUser(Users newUser) {
        // במקרה של משתמש עם טלפון זהה, נוכל לחזור על זה או להחזיר שגיאה
        if (usersRepo.existsById(newUser.getUserId())) {
            throw new RuntimeException("משתמש עם מספר טלפון זה כבר קיים");
        }

        // הוספת כוכבים התחלתיים
        newUser.setStars(50);


        // שמירת המשתמש בבסיס הנתונים
        return usersRepo.save(newUser);
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


    public List<Transactions> getUserHistory(Long userId) {
        // 1. שליפת הנתונים מה-Repository
        List<Transactions> history = transactionsRepo.findByUserId(userId);
        // 2. בדיקה: אם הרשימה ריקה, ייתכן שהמשתמש לא קיים או שאין לו חניות
        if (history.isEmpty()) {
            throw new ResourceNotFoundException("לא נמצאה היסטוריית חניות עבור משתמש מספר: " + userId);
        }
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

    public boolean existsById(Long userId) {
        return usersRepo.existsById(userId);
    }
}
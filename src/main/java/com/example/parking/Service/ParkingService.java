package com.example.parking.Service; // שים לב לשם החבילה אצלך (Service בלי s)
import com.example.parking.Entities.Parking;
import com.example.parking.Entities.Transactions;
import com.example.parking.Entities.Users;
import com.example.parking.Entities.Vehicles;
import com.example.parking.Reposetories.ParkingRepo;
import com.example.parking.Reposetories.TransactionsRepo;
import com.example.parking.Reposetories.UsersRepo;
import com.example.parking.Reposetories.VehiclesRepo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
@Service
public class ParkingService {

    private final ParkingRepo parkingRepo;
    private final UsersRepo usersRepo;
    private final  VehiclesRepo vehiclesRepo;
    private final  TransactionsRepo transactionRepo;
    private final EmailService emailService; // 1. הוספת השירות כאן

    public ParkingService( EmailService emailService, TransactionsRepo transactionRepo,VehiclesRepo vehiclesRepo,ParkingRepo parkingRepo, UsersRepo usersRepo) {
        this.parkingRepo = parkingRepo;
        this.usersRepo = usersRepo;
        this.vehiclesRepo = vehiclesRepo;
        this.transactionRepo = transactionRepo;
        this.emailService = emailService;
    }

    // שליפת כל 1,000 החניות
    public List<Parking> getAllSpots() {
        return parkingRepo.findAll();
    }

    // שליפת חניות לפי קומה - שימוש ב-equals בגלל ה-Long
    public List<Parking> getSpotsByFloor(Integer floor) {
        return parkingRepo.findAll().stream()
                .filter(spot -> spot.getFloor() != 0 && spot.getFloor() == floor)
                .collect(Collectors.toList());
    }

    // ספירת חניות פנויות לכל קומה
    public Map<Long, Integer> getAvailableCountPerFloor() {
        List<Parking> allSpots = parkingRepo.findAll();

        // אם הרשימה ריקה, נבין שהחניות לא נוצרו
        if (allSpots.isEmpty()) {
            System.out.println("⚠️ אזהרה: אין חניות במסד הנתונים!");
        }

        return allSpots.stream()
                // מוודא שהחניה קיימת והיא פנויה (false)
                .filter(spot -> spot.getisOccupied() == false)
                .collect(Collectors.groupingBy(
                        Parking::getFloor,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
    }

    // מציאת חניה מומלצת
    public Parking getSuggestedSpot(String plate) {
        return parkingRepo.findAll().stream()
                // אנחנו רוצים רק חניות שאינן תפוסות
                .filter(spot -> !spot.getisOccupied())
                .findFirst()
                .orElse(null);
    }
    public String startParking(String plate, Integer hoursRequested) {
        // 1. בדיקה אם הרכב כבר נמצא בחניון (מניעת כפילות)
        if (parkingRepo.findByCurrentVehicleId(plate).isPresent()) {
            return "⚠️ הרכב " + plate + " כבר נמצא בתוך החניון.";
        }

        // 2. חיפוש הרכב במאגר ובדיקת סוג לקוח (הלוגיקה שלך)
        Vehicles vehicle = vehiclesRepo.findByLicensePlate(plate);
        String welcomeMessage;
        Long userId = null;
        String vehicleType = "REGULAR";

        if (vehicle != null) {
            userId = vehicle.getUserId();
            vehicleType = vehicle.getVehicleType();
            if ("HANDICAPPED".equalsIgnoreCase(vehicleType)) {
                welcomeMessage = "♿ שלום רב! זוהה רכב נכה - החניה בחינם.";
            } else {
                welcomeMessage = "🚗 ברוך הבא לקוח רשום! (הנחה תחושב ביציאה).";
            }
        } else {
            welcomeMessage = "👋 ברוך הבא אורח! הכניסה אושרה בתעריף מלא.";
        }

        // 3. מציאת חניה פנויה
        int hoursForCheck = (hoursRequested != null && hoursRequested > 0) ? hoursRequested : 2;
        Parking spot = getSuggestedSpot(plate, hoursForCheck);
        if (spot == null) {
            return "❌ מצטערים, אין מקום פנוי בחניון כרגע.";
        }

        // 4. חישוב זמנים לעסקה
        long now = System.currentTimeMillis();
        long estimatedEndTime = 0; // 0 אומר "ללא הגבלה"
        double initialPayment = 0;

        if (hoursRequested != null && hoursRequested > 0) {
            estimatedEndTime = now + (hoursRequested * 3600000L); // המרה למילישניות

            // חישוב מחיר ראשוני (אם זה נכה - תמיד 0)
            if (!"HANDICAPPED".equalsIgnoreCase(vehicleType)) {
                initialPayment = spot.getPricePerHour() * hoursRequested;
            }
        }

        // 5. יצירת העסקה בטבלה שלך (Transactions)
        Transactions transaction = new Transactions();
        transaction.setVehicleId(plate);
        transaction.setSpotId(spot.getSpotId());
        transaction.setStartTime(now);
        transaction.setEndTime(estimatedEndTime);
        transaction.setUserId(userId);
        transaction.setTotalPayment(initialPayment);
        transaction.setPaymentStatus(false);

        transactionRepo.save(transaction); // שמירה בטבלת עסקאות

        // 6. עדכון ושמירת החניה (נשאר כפי שהיה)
        spot.setOccupied(true);
        spot.setCurrentVehicleId(plate);
        parkingRepo.save(spot);

        // 7. בניית הודעת סיום
        String timeMode = (estimatedEndTime > 0) ? " ל-" + hoursRequested + " שעות" : " ללא הגבלת זמן";
        return "✅ " + welcomeMessage + "\nכניסה מאושרת" + timeMode + ".\nסע לשלום לחניה: " + spot.getLocation();
    }
    public String endParking(String plate, String method, String details) {
        // 1. חיפוש העסקה הפתוחה של הרכב (זאת שעדיין לא שולמה)
        Optional<Transactions> transOpt = transactionRepo.findAll().stream()
                .filter(t -> t.getVehicleId().equals(plate) && !t.isPaymentStatus())
                .findFirst();

        if (transOpt.isEmpty()) {
            return "⚠️ שגיאה: לא נמצאה עסקה פעילה עבור הרכב " + plate;
        }

        Transactions trans = transOpt.get();
        long now = System.currentTimeMillis();
        double finalPrice = 0;
        long durationMillis;

        // 2. מציאת נתוני החניה והרכב לצורך חישוב
        Parking spot = parkingRepo.findById(trans.getSpotId()).orElse(null);
        Vehicles vehicle = vehiclesRepo.findByLicensePlate(plate);

        if (spot == null) return "⚠️ שגיאה: נתוני החניה לא נמצאו.";

        // 3. לוגיקת חישוב הזמן
        if (trans.getEndTime() > trans.getStartTime()) {
            // מקרה א': הוזמן מראש לזמן מוגדר
            durationMillis = trans.getEndTime() - trans.getStartTime();
            finalPrice = trans.getTotalPayment(); // המחיר כבר חושב בכניסה
        } else {
            // מקרה ב': נכנס ללא הגבלה (0) - מחשבים זמן אמת
            durationMillis = now - trans.getStartTime();
            double hours = Math.ceil(durationMillis / 3600000.0); // עיגול לשעה הקרובה
            finalPrice = hours * spot.getPricePerHour();
        }

        // 4. בדיקת הנחות (נכה/מנוי) - למקרה שזה לא חושב בכניסה
        if (vehicle != null) {
            if ("HANDICAPPED".equalsIgnoreCase(vehicle.getVehicleType())) {
                finalPrice = 0;
            } else {
                Optional<Users> userOpt = usersRepo.findById(vehicle.getUserId());
                if (userOpt.isPresent() && "SUBSCRIBER".equalsIgnoreCase(userOpt.get().getType())) {
                    finalPrice = finalPrice * 0.8; // 20% הנחה
                }
            }
        }

        // 5. עדכון העסקה ושחרור החניה
        trans.setEndTime(now);

        String paymentNote = executePaymentInternal(trans, method, details);
        transactionRepo.save(trans);

        spot.setOccupied(false);
        spot.setCurrentVehicleId(null);

        parkingRepo.save(spot);

        String durationStr = String.format("%.1f", durationMillis / 3600000.0);

        if (vehicle != null && vehicle.getUserId() != 0) {
            usersRepo.findById(vehicle.getUserId()).ifPresent(user -> {
                if (user.getemail() != null && !user.getemail().isEmpty()) {
                    emailService.sendParkingReceipt(
                            user.getemail()

                    );
                    System.out.println("📧 קבלה נשלחה בהצלחה למייל: " + user.getemail());
                }
            });
        }
        return "🚗 הרכב " + plate + " יצא.\n" +
                "⏱️ זמן: " + durationStr + " שעות.\n" +
                "💰 סכום: " + finalPrice + " ש\"ח.\n" +
                "✅ " + paymentNote + "\nלהתראות!";
    }
    private String executePaymentInternal(Transactions trans, String method, String details) {
        trans.setPaymentStatus(true);
        transactionRepo.save(trans);

        if ("CREDIT_CARD".equalsIgnoreCase(method) && details.length() >= 4) {
            return "💳 שולם באשראי (.." + details.substring(details.length() - 4) + ")";
        }
        return "📱 שולם באמצעות " + method;
    }
    // עדכון מצב חניה (מהמצלמה)
    public void updateSpotStatus(Long spotId, Boolean isOccupied) {
        Parking spot = parkingRepo.findById(spotId)
                .orElseThrow(() -> new RuntimeException("חניה לא נמצאה"));
        spot.setOccupied(isOccupied);
        parkingRepo.save(spot);
    }

    // ניווט ללא שימוש ב-Point (רשימה של רשימות)
    public List<List<Integer>> getNavigationPath(Long startId, Long targetId) {
        List<List<Integer>> path = new ArrayList<>();
        path.add(List.of(0, 0));   // כניסה
        path.add(List.of(50, 100)); // פנייה
        path.add(List.of(100, 200)); // יעד
        return path;
    }

    // איתור רכב (Find My Car) - פתרון מדויק לשמות השדות שלך
    public Parking findVehicleLocation(String plate) {
        if (plate == null) return null;

        return parkingRepo.findAll().stream()
                .filter(spot -> {
                    // כאן את צריכה להשתמש בשם ה-Getter המדויק שיש לך ב-Parking Entity
                    // אם השדה הוא currentVehicleId, הפונקציה היא getCurrentVehicleId()
                    Object carId = spot.getCurrentVehicleId();
                    return carId != null && String.valueOf(carId).equals(plate);
                })
                .findFirst()
                .orElse(null);
    }


    // פונקציית עזר לבדיקת חפיפה (מקרי קצה של זמנים)
    private Parking findSpotForTimeRange(long start, long end) {
        List<Parking> allSpots = parkingRepo.findAll();
        for (Parking spot : allSpots) {
            // מחפשים עסקאות קיימות על אותה חניה שחופפות לזמן המבוקש
            boolean isOverlapping = transactionRepo.findAll().stream()
                    .filter(t -> t.getSpotId() == spot.getSpotId())
                    .anyMatch(t -> (start < t.getEndTime() && end > t.getStartTime()));

            if (!isOverlapping) return spot; // מצאנו חניה שפנויה בדיוק בטווח הזה
        }
        return null;
    }

    public String createPreBooking(String plate, String arrivalDateStr, int durationHours) {
        try {
            // 1. הגדרת פורמט תאריך (וודאי שאת שולחת ככה ב-Postman: 2026-03-15 10:00)
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime startTime = LocalDateTime.parse(arrivalDateStr, formatter);
            LocalDateTime endTime = startTime.plusHours(durationHours); // חישוב שעת סיום

            // המרה למילישניות לצורך שמירה בעסקה (הטבלה שלך)
            long startTimeMillis = startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long endTimeMillis = endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            // בדיקה שהתאריך לא בעבר
            if (startTimeMillis < System.currentTimeMillis()) {
                return "❌ שגיאה: לא ניתן להזמין חניה לזמן שעבר.";
            }

            // 2. חיפוש חניה פנויה בטווח הזה
            Parking availableSpot = findSpotForTimeRange(startTimeMillis, endTimeMillis);
            if (availableSpot == null) {
                return "❌ מצטערים, אין חניה פנויה בטווח הזמנים המבוקש.";
            }

            // 3. לוגיקת מחיר והנחות (כפי שכתבנו קודם)
            Vehicles vehicle = vehiclesRepo.findByLicensePlate(plate);
            double totalPrice = availableSpot.getPricePerHour() * durationHours;
            if (vehicle != null && "HANDICAPPED".equalsIgnoreCase(vehicle.getVehicleType())) {
                totalPrice = 0;
            }

            // 4. שמירת העסקה
            Transactions booking = new Transactions();
            booking.setVehicleId(plate);
            booking.setSpotId(availableSpot.getSpotId());
            booking.setStartTime(startTimeMillis);
            booking.setEndTime(endTimeMillis);
            booking.setTotalPayment(totalPrice);
            booking.setPaymentStatus(false);
            transactionRepo.save(booking);

            // 5. הודעה מסודרת עם שעות מדויקות
            String formattedStart = startTime.format(DateTimeFormatter.ofPattern("HH:mm"));
            String formattedEnd = endTime.format(DateTimeFormatter.ofPattern("HH:mm"));
            String formattedDate = startTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            return "✅ ההזמנה בוצעה בהצלחה!\n" +
                    "📅 תאריך: " + formattedDate + "\n" +
                    "⏰ שעות: מ-" + formattedStart + " עד " + formattedEnd + " (סך הכל " + durationHours + " שעות)\n" +
                    "📍 מיקום: " + availableSpot.getLocation() + "\n" +
                    "💰 לתשלום: " + totalPrice + " ש\"ח.";

        } catch (Exception e) {
            return "❌ שגיאה: פורמט התאריך לא תקין. נא לשלוח בפורמט: yyyy-MM-dd HH:mm";
        }
    }



    public Parking getSuggestedSpot(String plate, int expectedHours) {
        long now = System.currentTimeMillis();
        long expectedEndTime = now + (expectedHours * 3600000L);

        return parkingRepo.findAll().stream()
                // 1. פנויה פיזית כרגע
                .filter(spot -> !spot.getisOccupied())

                // 2. בדיקה: האם יש מישהו שהזמין את המשבצת הזו "בתוך" חלון הזמן הזה?
                .filter(spot -> !hasConflict(spot.getSpotId(), now, expectedEndTime))

                .findFirst()
                .orElse(null);
    }

    private boolean hasConflict(Long spotId, long start, long end) {
        return transactionRepo.findAll().stream()
                .filter(t -> t.getSpotId() == spotId)
                .filter(t -> !t.isPaymentStatus())
                .anyMatch(t -> (start < t.getEndTime() && end > t.getStartTime()));
    }
    // הפונקציה תרוץ כל 60,000 מילישניות (דקה אחת)

    @Scheduled(fixedRate = 60000) // רץ כל דקה
    public void checkUpcomingConflicts() {
        long now = System.currentTimeMillis();
        long fifteenMinutesFromNow = now + (15 * 60 * 1000); // חישוב של 15 דקות קדימה

        // 1. מציאת כל ההזמנות שעומדות להתחיל ב-15 הדקות הקרובות
        List<Transactions> upcomingReservations = transactionRepo.findAll().stream()
                .filter(t -> !t.isPaymentStatus())
                .filter(t -> t.getStartTime() <= fifteenMinutesFromNow && t.getStartTime() > now)
                .collect(Collectors.toList());

        for (Transactions reservation : upcomingReservations) {
            // 2. בדיקה האם החניה של ההזמנה הזו תפוסה פיזית כרגע
            Optional<Parking> spotOpt = parkingRepo.findById(reservation.getSpotId());

            if (spotOpt.isPresent() && spotOpt.get().getisOccupied()) {
                Parking spot = spotOpt.get();
                String currentCar = spot.getCurrentVehicleId();

                // אם הרכב שחונה שם הוא לא הרכב שהזמין
                if (!currentCar.equals(reservation.getVehicleId())) {
                    sendWarningAlert(currentCar, spot.getLocation(), reservation.getStartTime());
                }
            }
        }
    }


    private void sendWarningAlert(String carPlate, String location, long startTimeMillis) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalDateTime startTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTimeMillis), ZoneId.systemDefault());

        String message = "⚠️ התראה למנהל: הרכב " + carPlate + " חונה בחניה " + location +
                " למרות שקיימת הזמנה לשעה " + startTime.format(timeFormatter) + "!";

        // כאן אפשר לזמן את פונקציית המייל שכבר בנית!
        System.out.println(message);

        // בונוס: שליחת מייל לבעל הרכב החורג (אם קיים במערכת)
        Vehicles v = vehiclesRepo.findByLicensePlate(carPlate);
        if (v != null && v.getUserId() != 0) {
            Users user = usersRepo.findById(v.getUserId()).orElse(null);
            if (user != null) {
                // sendRealEmail(user.getEmail(), "נא לפנות את החניה", "חנייתך מסתיימת, החניה מוזמנת לאחר.");
            }
        }
    }

    public String createTestData() {

        System.out.println("--- מתחיל יצירת נתונים ---");

//        Users testUser = new Users();
//        testUser.setFullName("Tester");
//        testUser.setemail("rp0556797395@gmail.com");
//        testUser.setType("SUBSCRIBER");
//        testUser.setPassword("SUBSCRIBER");
//        testUser.setPhoneNumber("SUBSCRIBER");
//        testUser.setStars(7);
//        testUser.setBalance(7);
//        testUser.setUserId(95);
//
//        System.out.println("מנסה לשמור משתמש...");
//        testUser = usersRepo.save(testUser);
//        System.out.println("✅ משתמש נשמר עם ID: " + testUser.getUserId());
//
//        Vehicles testVehicle = new Vehicles();
//        testVehicle.setLicensePlate("TEST-123");
//        testVehicle.setUserId(95);
//        testVehicle.setVehicleId(35);
//        testVehicle.setVehicleType("REGULAR");
//        testVehicle.setUserId(testUser.getUserId());
//
//        System.out.println("מנסה לשמור רכב...");
//        vehiclesRepo.save(testVehicle);
//        System.out.println("✅ רכב TEST-123 נשמר.");
//
//        return "✅ הנתונים נוצרו בהצלחה! אפשר לבדוק מייל.";

        try {
            // שליחת מייל בדיקה עם נתונים קבועים
            emailService.sendParkingReceipt(
                    "rp0556797395@gmail.com"// כתובת הנמען
                    // מיקום החניה
            );
            System.out.println("📧 המערכת ניסתה לשלוח את המייל בהצלחה!");
        } catch (Exception e) {
            // טיפול בשגיאות (למשל: בעיית התחברות לגוגל)
            System.err.println("❌ שגיאה בשליחת המייל: " + e.getMessage());
            e.printStackTrace();
        }
        return "rrrr";


    }
    }
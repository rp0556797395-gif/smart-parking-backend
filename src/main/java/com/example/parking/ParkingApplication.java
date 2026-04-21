package com.example.parking;

import com.example.parking.Entities.Parking;
import com.example.parking.Entities.Vehicles;
import com.example.parking.Entities.Users;
import com.example.parking.Reposetories.ParkingRepo; // ודאי שהשם תואם לתיקייה אצלך
import com.example.parking.Reposetories.UsersRepo;
import com.example.parking.Reposetories.VehiclesRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

@SpringBootApplication
@EnableScheduling // <-- הוסיפי את השורה הזו
public class ParkingApplication {

	public static void main(String[] args) {
		SpringApplication.run(ParkingApplication.class, args);
	}

	@Bean
	public CommandLineRunner initDatabase(ParkingRepo parkingRepo, UsersRepo usersRepo, VehiclesRepo vehiclesRepo) {
		return args -> {
			// 1. יצירת 10 משתמשים (יוזרים)
			if (usersRepo.count() == 0) {
				for (int i = 1; i <= 10; i++) {
					Users user = new Users();
					user.setUserId((long) i);
					user.setFullName("User " + i);
					user.setPhoneNumber("050-123456" + i);
					user.setPassword("pass" + i);
					user.setStars(i); // נותן לכל אחד כמות כוכבים התחלתית
					user.setBalance(100.0); // יתרה התחלתית לתשלום
					user.setType("REGULAR");
					usersRepo.save(user);
				}
				System.out.println("👥 10 משתמשים נוצרו בהצלחה.");
			}

			// 2. יצירת 10 רכבים (משויכים ליוזרים)
			if (vehiclesRepo.count() == 0) {
				for (int i = 1; i <= 10; i++) {
					Vehicles vehicle = new Vehicles();
					vehicle.setVehicleId((long) i);
					vehicle.setLicensePlate("PLATE-00" + i); // מספר רכב שקל לבדוק
					vehicle.setUserId((long) i); // מקשר ליוזר שיצרנו למעלה
					// רכבים אי זוגיים יהיו נכים לצורך בדיקת הנחות
					vehicle.setVehicleType(i % 2 == 0 ? "REGULAR" : "HANDICAPPED");
					vehiclesRepo.save(vehicle);
				}
				System.out.println("🚗 10 רכבים נוצרו וקושרו למשתמשים.");
			}

			// 3. יצירת 36 חניות (אם עדיין לא קיימות)
			if (parkingRepo.count() == 0) {
				for (long f = 1; f <= 3; f++) {
					for (char row = 'A'; row <= 'C'; row++) {
						for (int spot = 1; spot <= 4; spot++) {
							Parking p = new Parking();
							p.setFloor(f);
							p.setOccupied(false);
							p.setLocation(f + "-" + row + "-" + spot);
							p.setPricePerHour(10.0 + f);
							p.setSpotType("REGULAR");
							parkingRepo.save(p);
						}
					}
				}
				System.out.println("🅿️ 36 חניות נוצרו בהצלחה.");
			}

			System.out.println("✅ ברוך השם! מסד הנתונים מוכן לבדיקה עם נתונים אמיתיים.");
		};
	}
}
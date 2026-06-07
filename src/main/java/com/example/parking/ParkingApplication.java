package com.example.parking;

import com.example.parking.Entities.Parking;
import com.example.parking.Entities.Vehicles;
import com.example.parking.Entities.Users;
import com.example.parking.Reposetories.ParkingRepo;
import com.example.parking.Reposetories.UsersRepo;
import com.example.parking.Reposetories.VehiclesRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

@SpringBootApplication
@EnableScheduling
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
					user.setStars(i);
					user.setBalance(100.0);
					user.setType("REGULAR");
					usersRepo.save(user);
				}
				System.out.println("👥 10 משתמשים נוצרו בהצלחה.");
			}

			// 2. יצירת רכבים (לכל משתמש רכב אחד או שניים - סה"כ 15 רכבים)
			if (vehiclesRepo.count() == 0) {
				long vehicleIdCounter = 1;
				for (int i = 1; i <= 10; i++) {
					// כמה רכבים לייצר למשתמש הנוכחי? (רכב 1 או 2 רכבים לסירוגין)
					int numberOfVehicles = (i % 2 == 0) ? 2 : 1;

					for (int v = 1; v <= numberOfVehicles; v++) {
						Vehicles vehicle = new Vehicles();
						// מייצר מספר לוחית זיהוי ייחודי, למשל: PLATE-1-1, PLATE-2-1
						vehicle.setLicensePlate("PLATE-" + i + "-" + v);
						vehicle.setUserId((long) i); // קישור למשתמש
						vehicle.setVehicleType(i % 2 == 0 ? "REGULAR" : "HANDICAPPED");
						vehiclesRepo.save(vehicle);
						vehicleIdCounter++;
					}
				}
				System.out.println("🚗 רכבים נוצרו בהצלחה (לכל משתמש יש 1 או 2 רכבים).");
			}
			// ==========================================
			// 4. הדפסת כל הנתונים הקיימים במסד הנתונים
			// ==========================================
			System.out.println("\n=============================================");
			System.out.println("📊 נתונים נוכחיים במסד הנתונים (מודפסים בכל ריצה):");
			System.out.println("=============================================");
			System.out.println("\n👥 רשימת משתמשים:");
			usersRepo.findAll().forEach(user ->
					System.out.println("ID: " + user.getUserId() + " | שם: " + user.getFullName() + " | טלפון: " + user.getPhoneNumber())
			);
			System.out.println("\n🚗 רשימת רכבים:");
			vehiclesRepo.findAll().forEach(vehicle ->
					System.out.println("לוחית רישוי: " + vehicle.getLicensePlate() + " | שייך למשתמש ID: " + vehicle.getUserId())
			);
			if (parkingRepo.count() == 0) {
				for (long f = 1; f <= 3; f++) {
					for (char row = 'A'; row <= 'E'; row++) {
						for (int spot = 1; spot <= 10; spot++) {
							Parking p = new Parking();
							p.setFloor(f);
							p.setRow(String.valueOf(row));
							p.setIndex((long) spot);
							p.setLocation(f + "-" + row + "-" + spot);
							p.setPricePerHour(10.0 + f);
							p.setSpotType("REGULAR");
							p.setOccupied(false);

							if (f == 1 && row == 'A' && spot <= 6) {
								p.setOccupied(true);
							}

							parkingRepo.save(p);
						}
					}
				}
				System.out.println("✅ 150 חניות נוצרו בהצלחה.");
			}

			System.out.println("✅ ברוך השם! מסד הנתונים אותחל בדיוק לפי הבקשה ומוכן להצגת חניות תפוסות במפה!");
		};
	}
}
package com.example.parking.Service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService extends ClientService{

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * שליחת קבלה מפורטת למשתמש לאחר תשלום
     */
    public void sendParkingReceipt(String toEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("🧾 קבלה עבור חניה - רכב " );
        message.setText("שלום,\n\n" +
                "תודה שחנית אצלנו! להלן פרטי העסקה:\n" +
                "----------------------------------\n" +
                "🚗 מספר רכב: "  + "\n" +
                "⏱️ משך שהייה: " + " שעות\n" +
                "💰 סכום שולם: " +  " ש\"ח\n" +
                "----------------------------------\n\n" +
                "נסיעה טובה ובטוחה!");

        mailSender.send(message);
        System.out.println("📧 מייל נשלח בהצלחה לכתובת: " + toEmail);
    }
}

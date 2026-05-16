// package com.brickerp.common.config;

// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.mail.SimpleMailMessage;
// import org.springframework.mail.javamail.JavaMailSender;
// import org.springframework.stereotype.Service;

// @Service
// @RequiredArgsConstructor
// @Slf4j
// public class EmailService {

//     private final JavaMailSender mailSender;

//     @Value("${app.alert.email:admin@brickerp.com}")
//     private String alertEmail;

//     public void sendLowStockAlert(String productName, String warehouseName,
//             int currentStock, int minimumStock) {
//         try {
//             SimpleMailMessage message = new SimpleMailMessage();
//             message.setTo(alertEmail);
//             message.setSubject("⚠️ Low Stock Alert - " + productName);
//             message.setText(
//                     "LOW STOCK ALERT\n\n" +
//                             "Product: " + productName + "\n" +
//                             "Warehouse: " + warehouseName + "\n" +
//                             "Current Stock: " + currentStock + "\n" +
//                             "Minimum Stock Level: " + minimumStock + "\n\n" +
//                             "Please reorder immediately.\n\n" +
//                             "Brick ERP System");
//             mailSender.send(message);
//             log.info("Low stock alert sent for product: {}", productName);
//         } catch (Exception e) {
//             log.error("Failed to send low stock alert email: {}", e.getMessage());
//         }
//     }

//     public void sendEmail(String to, String subject, String body) {
//         try {
//             SimpleMailMessage message = new SimpleMailMessage();
//             message.setTo(to);
//             message.setSubject(subject);
//             message.setText(body);
//             mailSender.send(message);
//         } catch (Exception e) {
//             log.error("Failed to send email: {}", e.getMessage());
//         }
//     }
// }

package com.brickerp.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.alert.email}")
    private String alertEmail;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendLowStockAlert(
            String productName,
            String warehouseName,
            int currentStock,
            int minimumStock) {

        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(fromEmail);
            message.setTo(alertEmail);
            message.setSubject("Low Stock Alert - " + productName);

            message.setText(
                    "LOW STOCK ALERT\n\n" +
                            "Product: " + productName + "\n" +
                            "Warehouse: " + warehouseName + "\n" +
                            "Current Stock: " + currentStock + "\n" +
                            "Minimum Stock: " + minimumStock + "\n\n" +
                            "Please reorder immediately.\n\n" +
                            "Brick ERP System");

            mailSender.send(message);

            log.info("Low stock alert sent for {}", productName);

        } catch (Exception e) {
            log.error("Failed sending low stock email", e);
        }
    }

    public void sendEmail(String to, String subject, String body) {
        try {

            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

            log.info("Mail sent to {}", to);

        } catch (Exception e) {
            log.error("Failed sending email", e);
        }
    }
}
package com.example.hospitalManagement.kafka.consumer;

import com.example.hospitalManagement.dto.AppointmentNotificationMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "appointment-notification",groupId = "notification-group")
    public void consumerAppointment(String message){
        try {
            AppointmentNotificationMessage notification = objectMapper.readValue(message, AppointmentNotificationMessage.class);
            System.out.println("Received appointment notification for: " + notification.getPatientName());
            
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            
            helper.setTo(notification.getPatientEmail());
            helper.setSubject("Xác nhận đặt lịch khám thành công tại bệnh viện");
            
            String htmlContent = "<!DOCTYPE html>"
                    + "<html><head><style>"
                    + "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }"
                    + ".container { max-width: 600px; margin: 20px auto; border: 1px solid #e0e0e0; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }"
                    + ".header { background-color: #1976D2; color: white; padding: 20px; text-align: center; }"
                    + ".header h2 { margin: 0; font-size: 24px; }"
                    + ".content { padding: 30px; background-color: #fcfcfc; }"
                    + ".info-card { background: white; padding: 20px; border-radius: 8px; border: 1px solid #eee; margin-top: 20px; }"
                    + ".info-row { margin-bottom: 12px; font-size: 15px; }"
                    + ".label { font-weight: bold; color: #555; display: inline-block; width: 100px; }"
                    + ".footer { text-align: center; padding: 15px; font-size: 13px; color: #888; border-top: 1px solid #eee; background: #f9f9f9; }"
                    + ".highlight { color: #d32f2f; font-weight: bold; }"
                    + "</style></head><body>"
                    + "<div class='container'>"
                    + "<div class='header'><h2>Xác Nhận Đặt Lịch Khám</h2></div>"
                    + "<div class='content'>"
                    + "<p>Xin chào <strong>" + notification.getPatientName() + "</strong>,</p>"
                    + "<p>Cảm ơn bạn đã tin tưởng và đặt lịch khám tại bệnh viện. Dưới đây là thông tin chi tiết về lịch khám của bạn:</p>"
                    + "<div class='info-card'>"
                    + "<div class='info-row'><span class='label'>Bác sĩ:</span> " + notification.getDoctorName() + "</div>"
                    + "<div class='info-row'><span class='label'>Ngày khám:</span> " + notification.getAppointmentDate() + "</div>"
                    + "<div class='info-row'><span class='label'>Giờ khám:</span> " + notification.getAppointmentTime() + "</div>"
                    + "<div class='info-row'><span class='label'>Lý do:</span> " + notification.getReason() + "</div>"
                    + "</div>"
                    + "<p style='margin-top:20px;'>Vui lòng đến trước thời gian hẹn <span class='highlight'>15 phút</span> để thực hiện các thủ tục cần thiết.</p>"
                    + "</div>"
                    + "<div class='footer'>Đây là email tự động, vui lòng không trả lời.<br>© 2026 Hospital Management System</div>"
                    + "</div></body></html>";
            
            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
            System.out.println("HTML Email sent successfully to " + notification.getPatientEmail());
        } catch (Exception e) {
            System.err.println("Error processing appointment notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @KafkaListener(topics = "cancellation-notification",groupId = "notification-group")
    public void consumerCancellation(String message){
        
        System.out.println("Cancellation: "+message);
    }
    @KafkaListener(topics = "reminder-notification",groupId = "notification-group")
    public void consumerReminder(String message){
        System.out.println("Reminder: "+message);
    }
}

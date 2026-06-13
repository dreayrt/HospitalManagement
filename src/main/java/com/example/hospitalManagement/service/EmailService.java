package com.example.hospitalManagement.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Mã OTP Khôi Phục Mật Khẩu - MediCore Health");

            String htmlContent = "<!DOCTYPE html>" +
                    "<html>" +
                    "<head>" +
                    "    <style>" +
                    "        body { font-family: 'Arial', sans-serif; background-color: #f4f7f6; margin: 0; padding: 0; }" +
                    "        .container { max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }" +
                    "        .header { background-color: #0d6efd; padding: 30px; text-align: center; color: #ffffff; }" +
                    "        .header h1 { margin: 0; font-size: 24px; font-weight: 600; letter-spacing: 1px; }" +
                    "        .content { padding: 40px 30px; color: #333333; line-height: 1.6; font-size: 16px; }" +
                    "        .content p { margin: 0 0 15px; }" +
                    "        .otp-box { background-color: #f8f9fa; border: 2px dashed #0d6efd; border-radius: 8px; padding: 20px; text-align: center; margin: 30px 0; font-size: 36px; font-weight: bold; color: #0d6efd; letter-spacing: 8px; }" +
                    "        .footer { background-color: #f8f9fa; padding: 20px; text-align: center; font-size: 13px; color: #777777; border-top: 1px solid #eeeeee; }" +
                    "    </style>" +
                    "</head>" +
                    "<body>" +
                    "    <div class='container'>" +
                    "        <div class='header'>" +
                    "            <h1>MediCore Health</h1>" +
                    "        </div>" +
                    "        <div class='content'>" +
                    "            <p>Xin chào,</p>" +
                    "            <p>Chúng tôi nhận được yêu cầu khôi phục mật khẩu cho tài khoản của bạn tại hệ thống <strong>MediCore Health</strong>.</p>" +
                    "            <p>Dưới đây là mã xác minh (OTP) của bạn:</p>" +
                    "            <div class='otp-box'>" + otp + "</div>" +
                    "            <p>Mã này có hiệu lực trong vòng <strong>5 phút</strong>. Tuyệt đối không chia sẻ mã này với bất kỳ ai để đảm bảo an toàn cho tài khoản của bạn.</p>" +
                    "            <p>Nếu bạn không thực hiện yêu cầu này, xin vui lòng bỏ qua email này. Tài khoản của bạn vẫn an toàn.</p>" +
                    "        </div>" +
                    "        <div class='footer'>" +
                    "            <p>&copy; 2024 MediCore Healthcare Systems. All rights reserved.</p>" +
                    "            <p>Đây là email tự động, vui lòng không trả lời thư này.</p>" +
                    "        </div>" +
                    "    </div>" +
                    "</body>" +
                    "</html>";

            helper.setText(htmlContent, true); // Set to true to indicate HTML content

            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Failed to send email: " + e.getMessage());
            throw new RuntimeException("Lỗi khi gửi email OTP");
        }
    }
}

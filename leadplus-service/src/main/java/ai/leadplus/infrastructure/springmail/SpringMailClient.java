package ai.leadplus.infrastructure.springmail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpringMailClient {

    public void sendEmail(SpringMailRequest request, String userEmail, String userPassword) {
        JavaMailSender mailSender = createMailSender(userEmail, userPassword);
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setSubject(request.getSubject());

            helper.setText(request.getBody(), true);
            if (request.getFromAddress() != null) {
                helper.setFrom(request.getFromAddress());
            }
            helper.setTo(request.getToAddresses());
            if (request.getCcAddresses() != null) {
                helper.setCc(request.getCcAddresses());
            }
            if (request.getBccAddresses() != null) {
                helper.setBcc(request.getBccAddresses());
            }
            if (request.getAttachments() != null) {
                for (SpringMailRequest.Attachment attachment : request.getAttachments()) {
                    helper.addAttachment(attachment.getFileName(), attachment.getResource(), attachment.getContentType());
                }
            }
            mailSender.send(message);
            log.info("Email sent successfully as subject: {}", request.getSubject());
        } catch (MessagingException e) {
            log.error("Failed to send email: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private JavaMailSender createMailSender(String username, String password) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        return mailSender;
    }
}

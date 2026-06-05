package ai.leadplus.infrastructure.google.gmail;

import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.exception.ServiceUnavailableException;
import ai.leadplus.infrastructure.google.auth.GoogleAuthClient;
import jakarta.activation.DataHandler;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;

@Slf4j
@Service
@RequiredArgsConstructor
public class GmailClient {

    private final GoogleAuthClient googleAuthClient;
    private final RestTemplate restTemplate;

    public GmailMessageResponse sendEmail(GmailEmailSendRequest request, String accessToken) {
        try {
            log.info("Sending Gmail email to {}", request.getToAddresses());

            MimeMessage message = buildMimeMessage(request);

            byte[] rawMessageBytes = messageToBytes(message);
            String encodedEmail = Base64.getUrlEncoder().withoutPadding().encodeToString(rawMessageBytes);

            GmailMessageResponse response = sendGmailMessage(encodedEmail, accessToken);

            log.info(
                    "Gmail email sent successfully. messageId={}, threadId={}",
                    response.getId(),
                    response.getThreadId()
            );
            return response;

        } catch (MessagingException | IOException ex) {
            log.error("Failed to send Gmail email", ex);
            throw new ServiceUnavailableException("Failed to send Gmail email");
        }
    }

    public GmailThreadResponse getThreadById(String threadId, String accessToken) {
        try {
            log.info("Fetching Gmail thread: {}", threadId);

            HttpHeaders headers = googleAuthClient.createHeader(accessToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<GmailThreadResponse> response =
                    restTemplate.exchange(
                            "https://gmail.googleapis.com/gmail/v1/users/me/threads/" + threadId,
                            HttpMethod.GET,
                            entity,
                            GmailThreadResponse.class
                    );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ServiceUnavailableException("Failed to fetch Gmail thread");
            }

            return response.getBody();

        } catch (Exception ex) {
            log.error("Error fetching Gmail thread {}", threadId, ex);
            throw new ServiceUnavailableException("Unable to fetch Gmail thread");
        }
    }


    private MimeMessage buildMimeMessage(GmailEmailSendRequest request) throws MessagingException {
        Session session = Session.getDefaultInstance(new Properties());
        MimeMessage message = new MimeMessage(session);

        if (request.getFromAddress() != null) {
            message.setFrom(request.getFromAddress());
        }
        setRecipients(message, Message.RecipientType.TO, request.getToAddresses());
        setRecipients(message, Message.RecipientType.CC, request.getCcAddresses());
        setRecipients(message, Message.RecipientType.BCC, request.getBccAddresses());
        message.setSubject(request.getSubject());

        Multipart multipart = new MimeMultipart();

        MimeBodyPart bodyPart = new MimeBodyPart();
        bodyPart.setContent(
                request.getBody(),
                request.isHtml()
                        ? "text/html; charset=UTF-8"
                        : "text/plain; charset=UTF-8"
        );
        multipart.addBodyPart(bodyPart);

        if (request.getAttachments() != null) {
            for (GmailEmailSendRequest.Attachment attachment : request.getAttachments()) {
                multipart.addBodyPart(buildAttachmentPart(attachment));
            }
        }

        message.setContent(multipart);
        return message;
    }

    private MimeBodyPart buildAttachmentPart(GmailEmailSendRequest.Attachment attachment) {
        try {
            validateAttachment(attachment);
            MimeBodyPart attachmentPart = new MimeBodyPart();
            ByteArrayDataSource dataSource = new ByteArrayDataSource(attachment.getFileData(), attachment.getContentType());
            attachmentPart.setDataHandler(new DataHandler(dataSource));
            attachmentPart.setFileName(attachment.getFileName());

            return attachmentPart;

        } catch (Exception ex) {
            log.error("Failed to build attachment: {}", attachment.getFileName(), ex);
            throw new ServiceUnavailableException("Failed to process email attachment");
        }
    }

    private GmailMessageResponse sendGmailMessage(String rawMessage, String accessToken) {
        HttpHeaders headers = googleAuthClient.createHeader(accessToken);

        Map<String, String> body = Map.of("raw", rawMessage);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<GmailMessageResponse> response =
                restTemplate.postForEntity(
                        "https://gmail.googleapis.com/gmail/v1/users/me/messages/send",
                        entity,
                        GmailMessageResponse.class
                );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new ServiceUnavailableException("Gmail API did not return a valid response");
        }

        return response.getBody();
    }

    private void validateAttachment(GmailEmailSendRequest.Attachment attachment) {
        if (!StringUtils.hasText(attachment.getFileName())) {
            throw new BadRequestException("Attachment fileName is required");
        }
        if (!StringUtils.hasText(attachment.getContentType())) {
            throw new BadRequestException("Attachment contentType is required");
        }
        if (attachment.getFileData() == null || attachment.getFileData().length == 0) {
            throw new BadRequestException(
                    "Attachment fileData is required"
            );
        }
    }

    private void setRecipients(MimeMessage message,
                               Message.RecipientType type,
                               Iterable<InternetAddress> recipients) throws MessagingException {
        if (recipients == null) return;
        for (InternetAddress r : recipients) {
            message.addRecipient(type, r);
        }
    }

    private byte[] messageToBytes(MimeMessage message) throws IOException, MessagingException {
        try (var outputStream = new java.io.ByteArrayOutputStream()) {
            message.writeTo(outputStream);
            return outputStream.toByteArray();
        }
    }
}
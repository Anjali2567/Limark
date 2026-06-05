package ai.leadplus.infrastructure.aws.ses;

import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.exception.ServiceUnavailableException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Service
@RequiredArgsConstructor
public class AwsSESClient {

    private final SesClient sesClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.ses.sender}")
    private String senderEmail;

    @Value("${aws.cloudfront.url}")
    private String baseUrl;

    @SneakyThrows
    public void sendEmail(String recipientEmail, String mailTemplate, Map<String, Object> templateData) {
        sendEmail("LeadPlus", List.of(recipientEmail), mailTemplate, templateData);
    }

    @SneakyThrows
    public void sendEmail(List<String> recipientEmail, String mailTemplate, Map<String, Object> templateData) {
        sendEmail("LeadPlus", recipientEmail, mailTemplate, templateData);
    }

    @SneakyThrows
    public void sendEmail(
            String senderName,
            List<String> recipientEmail,
            String mailTemplate,
            Map<String, Object> templateData
    ) {
        validateSendEmailInputs(recipientEmail, mailTemplate, templateData);

        templateData.put("BASE_URL", baseUrl);

        String templateDataJson;
        try {
            templateDataJson = objectMapper.writeValueAsString(templateData);
        } catch (Exception e) {
            throw new ServiceUnavailableException("Failed to serialize template data: " + e.getMessage());
        }

        SendTemplatedEmailRequest templatedEmailRequest =
                SendTemplatedEmailRequest.builder()
                        .destination(destination ->
                                destination.toAddresses(recipientEmail))
                        .template(mailTemplate)
                        .templateData(templateDataJson)
                        .source(senderName + " <" + senderEmail + ">")
                        .build();

        sesClient.sendTemplatedEmail(templatedEmailRequest);
    }


    public void sendEmailWithAttachments(AwsSimpleEmailRequest request) {
        if (!StringUtils.hasText(request.getSenderEmail())) {
            throw new BadRequestException("Sender email is required");
        }
        if (CollectionUtils.isEmpty(request.getToRecipients())) {
            throw new BadRequestException("At least one recipient email is required");
        }

        try {
            Session session = Session.getDefaultInstance(new Properties());
            MimeMessage message = new MimeMessage(session);
            message.setSubject(request.getSubject(), "UTF-8");
            message.setFrom(new InternetAddress(request.getSenderName() + " <" + request.getSenderEmail() + ">"));
            message.setRecipients(jakarta.mail.Message.RecipientType.TO,
                    InternetAddress.parse(String.join(",", request.getToRecipients())));

            if (!CollectionUtils.isEmpty(request.getCcRecipients())) {
                message.setRecipients(jakarta.mail.Message.RecipientType.CC,
                        InternetAddress.parse(String.join(",", request.getCcRecipients())));
            }

            if (!CollectionUtils.isEmpty(request.getBccRecipients())) {
                message.setRecipients(jakarta.mail.Message.RecipientType.BCC,
                        InternetAddress.parse(String.join(",", request.getBccRecipients())));
            }

            if (StringUtils.hasText(request.getReplyToEmail())) {
                message.setReplyTo(InternetAddress.parse(request.getReplyToEmail()));
            }
            MimeMultipart multipart = new MimeMultipart();

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setContent(request.getBody(), "text/plain; charset=UTF-8");
            multipart.addBodyPart(textPart);

            if (!CollectionUtils.isEmpty(request.getAttachments())) {
                for (AwsSimpleEmailRequest.AwsSimpleEmailAttachment attachment : request.getAttachments()) {
                    MimeBodyPart attachmentPart = new MimeBodyPart();

                    if (attachment.getFileBytes() != null) {
                        DataSource dataSource = new ByteArrayDataSource(
                                attachment.getFileBytes(),
                                attachment.getContentType()
                        );
                        attachmentPart.setDataHandler(new DataHandler(dataSource));
                        attachmentPart.setFileName(attachment.getFileName());
                        multipart.addBodyPart(attachmentPart);
                    }
                }
            }
            message.setContent(multipart);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            message.writeTo(outputStream);

            RawMessage rawMessage = RawMessage.builder()
                    .data(SdkBytes.fromByteArray(outputStream.toByteArray()))
                    .build();
            SendRawEmailRequest rawEmailRequest = SendRawEmailRequest.builder()
                    .rawMessage(rawMessage)
                    .build();

            sesClient.sendRawEmail(rawEmailRequest);

        } catch (Exception e) {
            throw new ServiceUnavailableException("Failed to send email with attachments: " + e.getMessage());
        }
    }

    private void validateSendEmailInputs(
            List<String> recipientEmail,
            String mailTemplate,
            Map<String, Object> templateData
    ) {
        if (CollectionUtils.isEmpty(recipientEmail)) {
            throw new BadRequestException("Recipient email list must not be null or empty");
        }

        if (!StringUtils.hasText(mailTemplate)) {
            throw new BadRequestException("Mail template must not be null or empty");
        }

        if (templateData == null) {
            throw new BadRequestException("Template data must not be null");
        }
    }


}

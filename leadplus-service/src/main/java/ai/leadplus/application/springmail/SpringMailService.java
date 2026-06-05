package ai.leadplus.application.springmail;

import ai.leadplus.application.attachmentlibraries.AttachmentLibraryDto;
import ai.leadplus.application.attachmentlibraries.AttachmentLibraryService;
import ai.leadplus.application.contactemails.ContactEmailDto;
import ai.leadplus.application.common.RecipientDto;
import ai.leadplus.application.common.utils.FileReader;
import ai.leadplus.application.common.utils.HtmlUtils;
import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.exception.UnauthorizedException;
import ai.leadplus.application.mailboxes.MailboxDto;
import ai.leadplus.application.users.UserDto;
import ai.leadplus.application.users.UserService;

import ai.leadplus.domain.mailboxes.MailBoxType;
import ai.leadplus.infrastructure.springmail.SpringMailClient;
import ai.leadplus.infrastructure.springmail.SpringMailRequest;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpringMailService {

    private final SpringMailClient springMailClient;
    private final AttachmentLibraryService attachmentLibraryService;
    private final UserService userService;

    @Value("${aws.cloudfront.url}")
    private String cloudfrontUrl;

    public void sendEmail(ContactEmailDto contactEmailDto,
                          MailboxDto mailboxDto,
                          String unsubscribeUrl) {

        if (!Objects.equals(mailboxDto.getType(), MailBoxType.SMTP)) {
            throw new BadRequestException("Mailbox type is not SMTP");
        }
        if (!StringUtils.hasText(mailboxDto.getMetaData().getSmtpAppPassword())) {
            throw new UnauthorizedException("User is not authorized with SMTP");
        }
        if (CollectionUtils.isEmpty(contactEmailDto.getToRecipients())) {
            throw new BadRequestException("To recipients cannot be empty");
        }

        UserDto userDto = userService.getUserById(mailboxDto.getUserId());
        InternetAddress fromInternetAddress;

        try {
            fromInternetAddress = new InternetAddress(userDto.getEmail(), userDto.getName());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        InternetAddress[] toInternetAddresses = mapRecipientsToInternetAddresses(contactEmailDto.getToRecipients());
        InternetAddress[] ccInternetAddresses = mapRecipientsToInternetAddresses(contactEmailDto.getCcRecipients());
        InternetAddress[] bccInternetAddresses = mapRecipientsToInternetAddresses(contactEmailDto.getBccRecipients());

        String emailBody = HtmlUtils.addUnsubscribeFooter(contactEmailDto.getBody(), unsubscribeUrl);

        SpringMailRequest mailRequest = SpringMailRequest.builder()
                .subject(contactEmailDto.getSubject())
                .body(emailBody)
                .fromAddress(fromInternetAddress)
                .toAddresses(toInternetAddresses)
                .ccAddresses(ccInternetAddresses)
                .bccAddresses(bccInternetAddresses)
                .build();

        if (!CollectionUtils.isEmpty(contactEmailDto.getAttachmentIds())) {
            List<AttachmentLibraryDto> attachments = attachmentLibraryService.getAttachmentsByIds(contactEmailDto.getAttachmentIds());
            List<SpringMailRequest.Attachment> mailAttachments = attachments.stream()
                    .map(this::convertToSpringAttachment)
                    .toList();
            mailRequest.setAttachments(mailAttachments);
        }
        springMailClient.sendEmail(mailRequest, mailboxDto.getEmailAddress(), mailboxDto.getMetaData().getSmtpAppPassword());
    }


    private InternetAddress[] mapRecipientsToInternetAddresses(java.util.List<RecipientDto> recipients) {
        if (CollectionUtils.isEmpty(recipients)) {
            return new InternetAddress[0];
        }
        return recipients.stream()
                .map(recipient -> {
                    try {
                        return recipient.toInternetAddress();
                    } catch (UnsupportedEncodingException | AddressException e) {
                        log.error("Error converting recipient to InternetAddress: {}", recipient, e);
                        throw new RuntimeException(e);
                    }
                })
                .toArray(InternetAddress[]::new);
    }

    private SpringMailRequest.Attachment convertToSpringAttachment(AttachmentLibraryDto attachmentLibraryDto) {
        String fileUrl = cloudfrontUrl + attachmentLibraryDto.getFileUrl();
        byte[] contentBytes = FileReader.convertUrlToBytes(fileUrl);
        String fullFilename = attachmentLibraryDto.getFilename() + "." + attachmentLibraryDto.getFileType();
        String mimeType = URLConnection.guessContentTypeFromName(fullFilename);
        if (mimeType == null) mimeType = "application/octet-stream";
        return SpringMailRequest.Attachment.builder()
                .fileName(fullFilename)
                .resource(new ByteArrayResource(contentBytes))
                .contentType(mimeType)
                .build();
    }
}

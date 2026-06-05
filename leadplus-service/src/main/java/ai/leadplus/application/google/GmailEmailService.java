package ai.leadplus.application.google;

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
import ai.leadplus.infrastructure.google.auth.GoogleAuthClient;
import ai.leadplus.infrastructure.google.auth.GoogleTokenResponse;
import ai.leadplus.infrastructure.google.gmail.GmailClient;
import ai.leadplus.infrastructure.google.gmail.GmailEmailSendRequest;
import ai.leadplus.infrastructure.google.gmail.GmailMessageResponse;
import ai.leadplus.infrastructure.google.label.GmailLabelClient;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class GmailEmailService {

    private final GmailClient gmailClient;
    private final GoogleAuthClient googleAuthClient;
    private final AttachmentLibraryService attachmentLibraryService;
    private final UserService userService;
    private final GmailLabelClient gmailLabelClient;

    @Value("${aws.cloudfront.url}")
    private String cloudfrontUrl;

    public GmailMessageResponse sendEmail(ContactEmailDto contactEmailDto,
                                          MailboxDto mailboxDto) {
        return sendEmail(contactEmailDto, mailboxDto, null);
    }

    public GmailMessageResponse sendEmail(ContactEmailDto contactEmailDto,
                                          MailboxDto mailboxDto,
                                          String unsubscribeUrl) {

        if (!Objects.equals(mailboxDto.getType(), MailBoxType.GMAIL)) {
            throw new BadRequestException("Mailbox type is not GMAIL");
        }
        if (!StringUtils.hasText(mailboxDto.getMetaData().getRefreshToken()) || !StringUtils.hasText(mailboxDto.getMetaData().getGoogleId())) {
            throw new UnauthorizedException("User is not authorized with Google");
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

        List<InternetAddress> toInternetAddresses = mapRecipientsToInternetAddresses(contactEmailDto.getToRecipients());
        List<InternetAddress> ccInternetAddresses = mapRecipientsToInternetAddresses(contactEmailDto.getCcRecipients());
        List<InternetAddress> bccInternetAddresses = mapRecipientsToInternetAddresses(contactEmailDto.getBccRecipients());

        String emailBody = HtmlUtils.addUnsubscribeFooter(contactEmailDto.getBody(), unsubscribeUrl);

        GmailEmailSendRequest mailRequest = GmailEmailSendRequest.builder()
                .subject(contactEmailDto.getSubject())
                .body(emailBody)
                .fromAddress(fromInternetAddress)
                .toAddresses(toInternetAddresses)
                .ccAddresses(ccInternetAddresses)
                .bccAddresses(bccInternetAddresses)
                .html(true)
                .build();

        if (!CollectionUtils.isEmpty(contactEmailDto.getAttachmentIds())) {
            List<AttachmentLibraryDto> attachments = attachmentLibraryService.getAttachmentsByIds(contactEmailDto.getAttachmentIds());
            List<GmailEmailSendRequest.Attachment> mailAttachments = attachments.stream()
                    .map(this::convertToGmailAttachment)
                    .toList();
            mailRequest.setAttachments(mailAttachments);
        }
        GoogleTokenResponse response = googleAuthClient.fetchTokensFromRefreshToken(mailboxDto.getMetaData().getRefreshToken());
        GmailMessageResponse messageResponse = gmailClient.sendEmail(mailRequest, response.getAccess_token());
        String googleLabelId = mailboxDto.getMetaData().getGoogleLabelId();
        if (StringUtils.hasText(googleLabelId)) {
            gmailLabelClient.appendLabelToMessage(response.getAccess_token(), messageResponse.getId(), googleLabelId);
        }
        return messageResponse;
    }


    private List<InternetAddress> mapRecipientsToInternetAddresses(List<RecipientDto> recipients) {
        if (CollectionUtils.isEmpty(recipients)) {
            return List.of();
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
                .toList();
    }

    private GmailEmailSendRequest.Attachment convertToGmailAttachment(AttachmentLibraryDto attachmentLibraryDto) {
        String fileUrl = cloudfrontUrl + attachmentLibraryDto.getFileUrl();
        byte[] contentBytes = FileReader.convertUrlToBytes(fileUrl);
        return GmailEmailSendRequest.Attachment.builder()
                .fileName(attachmentLibraryDto.getFilename() + "." + attachmentLibraryDto.getFileType())
                .contentType("application/pdf")
                .fileData(contentBytes)
                .build();
    }
}

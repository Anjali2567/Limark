package ai.leadplus.application.azure;

import ai.leadplus.application.attachmentlibraries.AttachmentLibraryDto;
import ai.leadplus.application.attachmentlibraries.AttachmentLibraryService;
import ai.leadplus.application.contactemails.ContactEmailDto;
import ai.leadplus.application.common.RecipientDto;
import ai.leadplus.application.common.utils.FileReader;
import ai.leadplus.application.common.utils.HtmlUtils;
import ai.leadplus.application.exception.AzureUnauthorizedException;
import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.mailboxes.AzureRefreshTokenUpdatedEvent;
import ai.leadplus.application.mailboxes.MailboxDto;
import ai.leadplus.domain.mailboxes.MailBoxType;
import ai.leadplus.infrastructure.azure.auth.AzureAuthClient;
import ai.leadplus.infrastructure.azure.auth.AzureRefreshTokenResponse;
import ai.leadplus.infrastructure.azure.graph.emails.AzureEmailClient;
import ai.leadplus.infrastructure.azure.graph.emails.AzureEmailSendRequest;
import ai.leadplus.infrastructure.azure.graph.emails.AzureGraphMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AzureEmailService {

    private final AzureEmailClient azureEmailClient;
    private final AzureAuthClient azureAuthClient;
    private final AttachmentLibraryService attachmentLibraryService;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${aws.cloudfront.url}")
    private String cloudfrontUrl;

    public AzureGraphMessageResponse sendEmail(
            ContactEmailDto contactEmailDto,
            MailboxDto mailboxDto) {
        return sendEmail(contactEmailDto, mailboxDto, null);
    }

    public AzureGraphMessageResponse sendEmail(
            ContactEmailDto contactEmailDto,
            MailboxDto mailboxDto,
            String unsubscribeUrl) {

        if (!Objects.equals(mailboxDto.getType(), MailBoxType.OUTLOOK)) {
            throw new BadRequestException("Mailbox type is not Outlook");
        }

        if (!StringUtils.hasText(mailboxDto.getMetaData().getRefreshToken()) || !StringUtils.hasText(mailboxDto.getMetaData().getAzureId())) {
            throw new AzureUnauthorizedException("User is not authorized with Azure");
        }
        if (CollectionUtils.isEmpty(contactEmailDto.getToRecipients())) {
            throw new BadRequestException("To recipients cannot be empty");
        }

        List<AzureEmailSendRequest.Recipient> toAzureRecipients = mapRecipientsToAzure(contactEmailDto.getToRecipients());
        List<AzureEmailSendRequest.Recipient> ccAzureRecipients = mapRecipientsToAzure(contactEmailDto.getCcRecipients());
        List<AzureEmailSendRequest.Recipient> bccAzureRecipients = mapRecipientsToAzure(contactEmailDto.getBccRecipients());


        String emailBody = HtmlUtils.addUnsubscribeFooter(contactEmailDto.getBody(), unsubscribeUrl);
        AzureEmailSendRequest.Body body = new AzureEmailSendRequest.Body("HTML", emailBody);

        AzureEmailSendRequest emailRequest = AzureEmailSendRequest.builder()
                .subject(contactEmailDto.getSubject())
                .body(body)
                .toRecipients(toAzureRecipients)
                .ccRecipients(ccAzureRecipients)
                .bccRecipients(bccAzureRecipients)
                .build();

        if(!CollectionUtils.isEmpty(contactEmailDto.getAttachmentIds())) {
            List<AttachmentLibraryDto> attachmentLibraryDtoList = attachmentLibraryService.getAttachmentsByIds(contactEmailDto.getAttachmentIds());
            List<AzureEmailSendRequest.Attachment> attachments = attachmentLibraryDtoList.stream()
                    .map(this::convertToAzureAttachment)
                    .toList();
            emailRequest.setAttachments(attachments);
        }

        AzureRefreshTokenResponse response = azureAuthClient.fetchAccessTokenAndRefreshTokenFromRefreshToken(mailboxDto.getMetaData().getRefreshToken());
        AzureRefreshTokenUpdatedEvent event = new AzureRefreshTokenUpdatedEvent(this, String.valueOf(mailboxDto.getId()), response.getRefresh_token());
        eventPublisher.publishEvent(event);
        AzureGraphMessageResponse azureGraphMessageResponse = azureEmailClient.createDraftEmail(emailRequest, mailboxDto.getMetaData().getAzureId(), response.getAccess_token());
        azureEmailClient.sendDraftedEmail(mailboxDto.getMetaData().getAzureId(), azureGraphMessageResponse.getId(), response.getAccess_token());
        contactEmailDto.setPlatform(MailBoxType.OUTLOOK);
        return azureGraphMessageResponse;
    }


    private List<AzureEmailSendRequest.Recipient> mapRecipientsToAzure(List<RecipientDto> recipients) {
        return CollectionUtils.isEmpty(recipients) ? null :
                recipients.stream()
                        .map(RecipientDto::toAzureRecipient)
                        .toList();
    }

    private AzureEmailSendRequest.Attachment convertToAzureAttachment(AttachmentLibraryDto attachmentLibraryDto) {
        String fileUrl = cloudfrontUrl + attachmentLibraryDto.getFileUrl();
        String contentBytes = FileReader.convertUrlToBase64(fileUrl);
        return AzureEmailSendRequest.Attachment.builder()
                .name(attachmentLibraryDto.getFilename())
                .contentBytes(contentBytes)
                .contentType("application/pdf")
                .build();
    }
}

package ai.leadplus.application.aws;


import ai.leadplus.application.attachmentlibraries.AttachmentLibraryDto;
import ai.leadplus.application.attachmentlibraries.AttachmentLibraryService;
import ai.leadplus.application.contactemails.ContactEmailDto;
import ai.leadplus.application.common.RecipientDto;
import ai.leadplus.application.common.utils.FileReader;
import ai.leadplus.application.common.utils.HtmlUtils;
import ai.leadplus.application.users.UserDto;
import ai.leadplus.domain.mailboxes.MailBoxType;
import ai.leadplus.infrastructure.aws.ses.AwsSESClient;
import ai.leadplus.infrastructure.aws.ses.AwsSimpleEmailRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AwsSESService {

    @Value("${aws.cloudfront.url}")
    private String cloudfrontUrl;

    private final AttachmentLibraryService attachmentLibraryService;
    private final AwsSESClient awsSESClient;

    public void sendInteractionEmail(ContactEmailDto emailContentDto,
                                     UserDto userDto,
                                     String email,
                                     String unsubscribeUrl) {

        List<String> bccRecipients = getEmailsFromRecipients(emailContentDto.getBccRecipients());
        bccRecipients.addLast(email);

        String emailBody = HtmlUtils.addUnsubscribeFooter(
                emailContentDto.getBody(),
                unsubscribeUrl
        );

        AwsSimpleEmailRequest awsSimpleEmailRequest = AwsSimpleEmailRequest.builder()
                .senderName(userDto.getName())
                .senderEmail(email)
                .toRecipients(getEmailsFromRecipients(emailContentDto.getToRecipients()))
                .ccRecipients(getEmailsFromRecipients(emailContentDto.getCcRecipients()))
                .bccRecipients(bccRecipients)
                .replyToEmail(email)
                .subject(emailContentDto.getSubject())
                .body(emailBody)
                .build();
        if (!CollectionUtils.isEmpty(emailContentDto.getAttachmentIds())) {
            List<AttachmentLibraryDto> attachments = attachmentLibraryService.getAttachmentsByIds(emailContentDto.getAttachmentIds());
            List<AwsSimpleEmailRequest.AwsSimpleEmailAttachment> mailAttachments = attachments.stream()
                    .map(this::convertToAwsAttachment)
                    .toList();
            awsSimpleEmailRequest.setAttachments(mailAttachments);
        }
        emailContentDto.setPlatform(MailBoxType.SES);
        awsSESClient.sendEmailWithAttachments(awsSimpleEmailRequest);
    }

    private List<String> getEmailsFromRecipients(List<RecipientDto> recipients) {
        if (recipients == null) {
            return new ArrayList<>();
        }
        return recipients.stream()
                .map(RecipientDto::getEmail)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
    }

    private AwsSimpleEmailRequest.AwsSimpleEmailAttachment convertToAwsAttachment(AttachmentLibraryDto attachmentLibraryDto) {
        String fileUrl = cloudfrontUrl + attachmentLibraryDto.getFileUrl();
        byte[] contentBytes = FileReader.convertUrlToBytes(fileUrl);
        return AwsSimpleEmailRequest.AwsSimpleEmailAttachment.builder()
                .fileName(attachmentLibraryDto.getFilename())
                .fileBytes(contentBytes)
                .contentType(attachmentLibraryDto.getFileType())
                .build();
    }
}
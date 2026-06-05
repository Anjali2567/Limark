package ai.leadplus.application.mailboxes;

import ai.leadplus.application.campaignorchestrator.CampaignEmailSentEvent;
import ai.leadplus.application.campaigns.CampaignAssignmentService;
import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.exception.SmtpConnectionException;
import ai.leadplus.application.exception.UnauthorizedException;
import ai.leadplus.application.users.UserDto;
import ai.leadplus.application.workspaceuser.WorkspaceUserDto;
import ai.leadplus.application.workspaceuser.WorkspaceUserService;
import ai.leadplus.domain.mailboxes.*;
import ai.leadplus.domain.workspaceusers.WorkspaceUserRole;
import ai.leadplus.infrastructure.aws.ses.AwsSESAuthClient;
import ai.leadplus.infrastructure.springmail.SpringMailClient;
import ai.leadplus.infrastructure.springmail.SpringMailRequest;
import jakarta.mail.internet.InternetAddress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailboxService {

    private final MailboxRepository mailboxRepository;
    private final AwsSESAuthClient awsSESAuthClient;
    private final SpringMailClient springMailClient;
    private final CampaignAssignmentService campaignAssignmentService;
    private final WorkspaceUserService workspaceUserService;

    public void updateAzureDetails(Long userId, Long workspaceId, String azureId, String email, String refreshToken) {

        Mailbox mailBox = mailboxRepository.findByUserIdAndWorkspaceIdAndTypeAndActiveTrue(userId, workspaceId, MailBoxType.OUTLOOK)
                .orElse(Mailbox.builder()
                        .userId(userId)
                        .workspaceId(workspaceId)
                        .type(MailBoxType.OUTLOOK)
                        .active(true)
                        .build());

        MailboxProviderConfig mailboxProviderConfig = MailboxProviderConfig.builder()
                .azureId(azureId)
                .refreshToken(refreshToken)
                .connectedAt(LocalDateTime.now())
                .build();
        mailBox.setMetaData(mailboxProviderConfig);
        mailBox.setEmailAddress(email);
        mailBox.setTokenExpired(false);
        mailboxRepository.save(mailBox);
    }

    public void updateGoogleDetails(Long userId, Long workspaceId, String googleId, String email, String refreshToken, String labelId) {

        Mailbox mailBox = mailboxRepository.findByUserIdAndWorkspaceIdAndTypeAndActiveTrue(userId, workspaceId, MailBoxType.GMAIL)
                .orElse(Mailbox.builder()
                        .userId(userId)
                        .workspaceId(workspaceId)
                        .type(MailBoxType.GMAIL)
                        .active(true)
                        .build());

        MailboxProviderConfig mailboxProviderConfig = MailboxProviderConfig.builder()
                .googleId(googleId)
                .googleLabelId(labelId)
                .refreshToken(refreshToken)
                .connectedAt(LocalDateTime.now())
                .build();
        mailBox.setMetaData(mailboxProviderConfig);
        mailBox.setEmailAddress(email);
        mailBox.setTokenExpired(false);
        mailboxRepository.save(mailBox);
    }

    public MailboxDto getOrCreateSesDetails(Long userId, String email, Long workspaceId) {

        Optional<Mailbox> existingMailbox =
                mailboxRepository.findByUserIdAndWorkspaceIdAndTypeAndActiveTrue(
                        userId, workspaceId, MailBoxType.SES
                );

        if (existingMailbox.isPresent()) {
            return MailboxDto.fromEntity(existingMailbox.get());
        }

        Mailbox mailbox = Mailbox.builder()
                .userId(userId)
                .workspaceId(workspaceId)
                .emailAddress(email)
                .type(MailBoxType.SES)
                .active(true)
                .metaData(MailboxProviderConfig.builder()
                        .connectedAt(LocalDateTime.now())
                        .awsSESVerificationState(AwsSESVerificationState.UNVERIFIED)
                        .build())
                .build();

        return MailboxDto.fromEntity(mailboxRepository.save(mailbox));
    }

    public List<MailboxDto> getConnectedMailboxes(Long workspaceId, Long userId) {
        List<Mailbox> mailboxes = mailboxRepository.findAllByUserIdAndWorkspaceIdAndActiveTrue(userId, workspaceId);
        return mailboxes.stream()
                .map(MailboxDto::fromEntity)
                .toList();
    }

    public List<MailboxDto> getConnectedMailboxes(Long workspaceId) {
        List<Mailbox> mailboxes = mailboxRepository.findAllByWorkspaceIdAndActiveTrue(workspaceId);
        return mailboxes.stream()
                .map(MailboxDto::fromEntity)
                .toList();
    }

    public List<MailboxDto> getAuthorizedMailboxes(Long workspaceId, Long userId) {
        List<Mailbox> mailboxes = canAccessAllWorkspaceMailboxes(workspaceId, userId)
                ? mailboxRepository.findAllByWorkspaceIdAndActiveTrue(workspaceId)
                : mailboxRepository.findAllByUserIdAndWorkspaceIdAndActiveTrue(userId, workspaceId);
        return mailboxes.stream()
                .map(MailboxDto::fromEntity)
                .toList();
    }

    public MailboxDto getAccessibleMailboxById(Long mailboxId, Long workspaceId, Long userId) {
        return MailboxDto.fromEntity(findAccessibleMailboxById(mailboxId, workspaceId, userId));
    }

    public void deactivateMailbox(Long mailboxId, Long workspaceId, Long userId) {
        Mailbox mailbox = findAccessibleMailboxById(mailboxId, workspaceId, userId);
        if (!Objects.equals(mailbox.getUserId(), userId)) {
            throw new UnauthorizedException("You can only disconnect your own mailbox");
        }
        if (campaignAssignmentService.isSendingMailboxAssigned(mailboxId)) {
            throw new BadRequestException("Cannot deactivate mailbox as it is assigned to active campaigns");
        }
        if(Objects.equals(mailbox.getType(), MailBoxType.SES)){
            if (Objects.equals(mailbox.getMetaData().getAwsSESVerificationState(), AwsSESVerificationState.VERIFIED)) {
                awsSESAuthClient.removeEmailIdentity(mailbox.getEmailAddress());
            }
            mailbox.getMetaData()
                    .setAwsSESVerificationState(AwsSESVerificationState.UNVERIFIED);
        }
        if (Objects.equals(mailbox.getType(), MailBoxType.SMTP)) {
            MailboxProviderConfig config = mailbox.getMetaData();
            config.setSmtpAppPassword(null);
            mailbox.setMetaData(config);
        }
        mailbox.setActive(false);
        mailboxRepository.save(mailbox);
    }

    private Mailbox findAccessibleMailboxById(Long mailboxId, Long workspaceId, Long userId) {
        Mailbox mailbox = findMailboxById(mailboxId);
        ensureMailboxAccess(workspaceId, userId, mailbox);
        return mailbox;
    }

    public MailboxDto createSmtpMailbox(UserDto userDto, Long workspaceId, String email, String smtpAppPassword) {

        try {
            InternetAddress internetAddress = new InternetAddress(email, userDto.getName());
            SpringMailRequest testMail = SpringMailRequest.builder()
                    .subject("SMTP Connection Test")
                    .body("Your SMTP connection has been successfully established.")
                    .fromAddress(internetAddress)
                    .toAddresses(new InternetAddress[]{internetAddress})
                    .build();

            springMailClient.sendEmail(testMail, email, smtpAppPassword);
        } catch (Exception ex) {
            log.error("Failed to send SMTP test email to {}: {}", email, ex.getMessage(), ex);
            throw new SmtpConnectionException("Failed to verify SMTP connection: " + ex.getMessage());
        }

        Mailbox mailbox = mailboxRepository.findByUserIdAndWorkspaceIdAndTypeAndActiveTrue(userDto.getId(), workspaceId, MailBoxType.SMTP)
                .orElse(Mailbox.builder()
                        .userId(userDto.getId())
                        .workspaceId(workspaceId)
                        .type(MailBoxType.SMTP)
                        .active(true)
                        .build());

        MailboxProviderConfig providerConfig = MailboxProviderConfig.builder()
                .smtpAppPassword(smtpAppPassword)
                .connectedAt(LocalDateTime.now())
                .build();
        mailbox.setMetaData(providerConfig);
        mailbox.setEmailAddress(email);
        mailboxRepository.save(mailbox);

        return MailboxDto.fromEntity(mailbox);
    }

    public MailboxDto getMailboxById(Long mailboxId) {
        Mailbox mailbox = findMailboxById(mailboxId);
        return MailboxDto.fromEntity(mailbox);
    }

    public void saveMailbox(MailboxDto mailboxDto) {
        mailboxRepository.save(mailboxDto.toEntity());
    }

    public void markTokenExpired(Long mailboxId) {
        Mailbox mailbox = findMailboxById(mailboxId);
        mailbox.setTokenExpired(true);
        mailboxRepository.save(mailbox);
    }

    @Transactional
    public long resetMailboxEmailCountDaily() {
        return mailboxRepository.resetEmailsSentTodayForActiveMailboxes(0);
    }

    @EventListener
    public void handleCampaignEmailSendEvent(CampaignEmailSentEvent event) {
        MailboxDto mailboxDto = event.getMailboxDto();
        Mailbox mailbox = findMailboxById(mailboxDto.getId());
        mailbox.setEmailsSentToday(mailbox.getEmailsSentToday() + 1);
        mailboxRepository.save(mailbox);
    }

    private Mailbox findMailboxById(Long mailboxId) {
        return mailboxRepository.findByIdAndActiveTrue(mailboxId)
                .orElseThrow(() -> new ResourceNotFoundException("Mailbox not found with id: " + mailboxId));
    }

    private void ensureMailboxAccess(Long workspaceId, Long userId, Mailbox mailbox) {
        if (!Objects.equals(mailbox.getWorkspaceId(), workspaceId)) {
            throw new UnauthorizedException("Mailbox does not belong to the provided workspace");
        }
        if (canAccessAllWorkspaceMailboxes(workspaceId, userId)) {
            return;
        }
        if (!Objects.equals(mailbox.getUserId(), userId)) {
            throw new UnauthorizedException("Insufficient permissions to access mailbox");
        }
    }

    private boolean canAccessAllWorkspaceMailboxes(Long workspaceId, Long userId) {
        WorkspaceUserDto workspaceUser = workspaceUserService.findByWorkspaceIdAndUserId(workspaceId, userId);
        return workspaceUser.getRole() == WorkspaceUserRole.OWNER
                || workspaceUser.getRole() == WorkspaceUserRole.WORKSPACE_ADMIN;
    }

    @Async
    @EventListener
    public void updateRefreshToken(AzureRefreshTokenUpdatedEvent event) {
        Mailbox mailbox = findMailboxById(Long.parseLong(event.getMailboxId()));
        MailboxProviderConfig metaData = mailbox.getMetaData();
        metaData.setRefreshToken(event.getNewRefreshToken());
        mailbox.setMetaData(metaData);
        mailboxRepository.save(mailbox);
    }
}

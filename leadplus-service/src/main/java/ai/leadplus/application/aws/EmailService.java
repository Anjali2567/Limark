package ai.leadplus.application.aws;

import ai.leadplus.domain.common.SourcingRequestType;
import ai.leadplus.infrastructure.aws.ses.AwsSESClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final AwsSESClient awsSESClient;
    private final EmailTemplateConfiguration templates;

    public void sendResetPasswordLink(String email, String username, String resetPasswordLink) {
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("RESET_PASSWORD_LINK", resetPasswordLink);
        templateData.put("USERNAME", usernameValidation(username));
        awsSESClient.sendEmail(List.of(email), templates.getResetPassword(), templateData);
    }

    public void sendUserApprovedEmail(String email, String username, String authLink) {
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("AUTH_URL", authLink);
        templateData.put("USERNAME", usernameValidation(username));
        awsSESClient.sendEmail(List.of(email), templates.getUserApproved(), templateData);
    }

    public void sendUserRejectedEmail(String email, String username, List<String> reasons) {
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("USERNAME", usernameValidation(username));
        awsSESClient.sendEmail(List.of(email), templates.getUserRejected(), templateData);
    }

    public void sendVendorApprovedEmail(String email, String username, String authLink) {
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("AUTH_URL", authLink);
        templateData.put("USERNAME", usernameValidation(username));
        awsSESClient.sendEmail(List.of(email), templates.getVendorApproved(), templateData);
    }

    public void sendVendorRejectedEmail(String email, String username, String reasons) {
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("USERNAME", usernameValidation(username));
        awsSESClient.sendEmail(List.of(email), templates.getVendorRejected(), templateData);
    }

    public void sendWorkspaceInvitationEmail(String email, String username, String workspaceName, String inviteLink) {
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("USERNAME", usernameValidation(username));
        templateData.put("WORKSPACE_NAME", workspaceName);
        templateData.put("INVITE_LINK", inviteLink);
        awsSESClient.sendEmail(List.of(email), templates.getWorkspaceInvitation(), templateData);
    }

    public void sendEmailVerificationEmail(String email, String username, String verificationLink) {
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("USERNAME", usernameValidation(username));
        templateData.put("VERIFICATION_LINK", verificationLink);
        awsSESClient.sendEmail(List.of(email), templates.getEmailVerify(), templateData);
    }

    public void sendFeedbackReceivedEmail(String email, String username, String message) {
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("USERNAME", usernameValidation(username));
        templateData.put("MESSAGE", message);
        awsSESClient.sendEmail(List.of(email), templates.getFeedbackReceived(), templateData);
    }

    public void sendCollaboratorInvite(
            String email,
            String userName,
            String inviterName,
            String companyName,
            String accessType,
            String inviteLink,
            SourcingRequestType type
    ) {
        Map<String, Object> templateData = mapToTemplateData(userName, inviterName, companyName, accessType, inviteLink, type);
        awsSESClient.sendEmail(email, templates.getCollaboratorInvite(), templateData);
    }

    public void sendCollaboratorAccessChange(
            String email,
            String userName,
            String inviterName,
            String companyName,
            String accessType,
            String inviteLink,
            SourcingRequestType type
    ) {
        Map<String, Object> templateData = mapToTemplateData(userName, inviterName, companyName, accessType, inviteLink, type);
        awsSESClient.sendEmail(email, templates.getCollaboratorAccessChange(), templateData);
    }

    private Map<String, Object> mapToTemplateData(String userName, String inviterName, String companyName, String accessType, String inviteLink, SourcingRequestType type) {
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("USERNAME", userName);
        templateData.put("INVITER_NAME", inviterName);
        templateData.put("COMPANY_NAME", companyName);
        templateData.put("ACCESS_TYPE", accessType);
        templateData.put("INVITE_LINK", inviteLink);
        templateData.put("SOURCE_TYPE", mapSourcingRequestTypeToString(type));
        templateData.put("YEAR", String.valueOf(LocalDate.now().getYear()));
        return templateData;
    }

    private String mapSourcingRequestTypeToString(SourcingRequestType type) {
        return switch (type) {
            case REQUEST_FOR_QUOTE -> "Request for Quote";
            case REQUEST_FOR_PROPOSAL -> "Request for Proposal";
        };
    }

    public void sendOTPVerificationEmail(String email, String username, String otp) {
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("USERNAME", usernameValidation(username));
        templateData.put("OTP_CODE", otp);
        awsSESClient.sendEmail(List.of(email), templates.getOtpVerification(), templateData);
    }

    public String usernameValidation(String username) {
        if (!StringUtils.hasText(username)) {
            return "LeadPlus User";
        }
        return username;
    }

    private String formatReasonsHtml(List<String> reasons) {
        if (reasons == null || reasons.isEmpty()) {
            return "<p style='text-align:left;'>No specific reasons were provided.</p>";
        }

        StringBuilder formattedReasons = new StringBuilder();
        formattedReasons.append("<ul style='text-align: left;'>");

        int count = Math.min(5, reasons.size());
        for (int i = 0; i < count; i++) {
            formattedReasons.append("<li>").append(reasons.get(i)).append("</li>");
        }

        if (reasons.size() > 5) {
            formattedReasons.append("<li>And more issues...</li>");
        }

        formattedReasons.append("</ul>");
        return formattedReasons.toString();
    }

}

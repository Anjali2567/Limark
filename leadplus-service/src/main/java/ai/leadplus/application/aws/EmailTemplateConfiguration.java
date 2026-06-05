package ai.leadplus.application.aws;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "aws.ses.email-template")
public class EmailTemplateConfiguration {
    private String userApproved;
    private String userRejected;
    private String vendorApproved;
    private String vendorRejected;
    private String resetPassword;
    private String workspaceInvitation;
    private String emailVerify;
    private String otpVerification;
    private String feedbackReceived;
    private String collaboratorInvite;
    private String collaboratorAccessChange;
}

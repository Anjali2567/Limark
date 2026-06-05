package ai.leadplus.application.common;

import ai.leadplus.domain.common.Recipient;
import ai.leadplus.infrastructure.azure.graph.emails.AzureEmailAddressRequest;
import ai.leadplus.infrastructure.azure.graph.emails.AzureEmailSendRequest;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipientDto {

    private String email;
    private String name;

    public AzureEmailSendRequest.Recipient toAzureRecipient() {
        return new AzureEmailSendRequest.Recipient(new AzureEmailAddressRequest(email, name));
    }

    public InternetAddress toInternetAddress() throws UnsupportedEncodingException, AddressException {
        if (StringUtils.hasText(name)) {
            return new InternetAddress(email, name);
        } else {
            return new InternetAddress(email);
        }
    }

    public static RecipientDto fromEntity(Recipient recipient) {
        if (recipient == null) {
            return null;
        }
        return RecipientDto.builder()
                .email(recipient.getEmail())
                .name(recipient.getName())
                .build();
    }

    public Recipient toEntity() {
        return Recipient.builder()
                .email(email)
                .name(name)
                .build();
    }
}

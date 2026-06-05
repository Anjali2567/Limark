package ai.leadplus.api.v1.campaigns;

import ai.leadplus.application.campaigncontacts.EmailDataDto;
import ai.leadplus.application.campaigncontacts.EmailDeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailDataResponse {
    private LocalDateTime sentAt;
    private EmailDeliveryStatus emailDeliveryStatus;
    private List<MessageResponse> messages;

    public static EmailDataResponse fromDto(EmailDataDto emailDataDto) {
        if (emailDataDto == null) return null;
        return EmailDataResponse.builder()
                .sentAt(emailDataDto.getSentAt())
                .emailDeliveryStatus(emailDataDto.getEmailDeliveryStatus())
                .messages(CollectionUtils.isEmpty(emailDataDto.getMessageDtos()) ? null :
                        emailDataDto.getMessageDtos().stream()
                                .map(MessageResponse::fromDto)
                                .toList())
                .build();
    }
}

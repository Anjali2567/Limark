package ai.leadplus.application.leadcontactevents;

import ai.leadplus.domain.leadcontactevents.LeadContactEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LeadContactEventDetailedDto extends LeadContactEventDto {

    private String userName;

    public static LeadContactEventDetailedDto fromEntity(LeadContactEvent event, String userName) {
        if (event == null) return null;
        return LeadContactEventDto.baseBuilder(LeadContactEventDetailedDto.builder(), event)
                .userName(userName)
                .build();
    }
}

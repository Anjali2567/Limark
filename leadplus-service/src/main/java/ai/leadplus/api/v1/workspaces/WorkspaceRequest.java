package ai.leadplus.api.v1.workspaces;

import ai.leadplus.application.workspaces.WorkspaceDto;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceRequest {

    private String name;
    private List<RecipientRequest> ccRecipients;
    private List<RecipientRequest> bccRecipients;

    @Positive
    private int dailySendLimit;

    public WorkspaceDto toDto() {
        return WorkspaceDto.builder()
                .name(name)
                .ccRecipients(
                        CollectionUtils.isEmpty(ccRecipients)
                                ? List.of()
                                : ccRecipients.stream()
                                .map(RecipientRequest::toDto)
                                .toList()
                )
                .bccRecipients(
                        CollectionUtils.isEmpty(bccRecipients)
                                ? List.of()
                                : bccRecipients.stream()
                                .map(RecipientRequest::toDto)
                                .toList()
                )
                .dailySendLimit(dailySendLimit)
                .build();
    }
}

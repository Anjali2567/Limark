package ai.leadplus.infrastructure.google.gmail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GmailMessageResponse {
    private String id;
    private String threadId;
}
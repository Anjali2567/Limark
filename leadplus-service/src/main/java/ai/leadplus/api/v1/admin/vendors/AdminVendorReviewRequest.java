package ai.leadplus.api.v1.admin.vendors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminVendorReviewRequest {
    private String reviewComment;
}

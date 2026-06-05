package ai.leadplus.api.v1.vendorsearch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VendorSearchRequest {
    private List<Long> industryIds;
    private List<Long> serviceIds;
    private List<Long> specificationIds;
    private List<String> certifications;
}

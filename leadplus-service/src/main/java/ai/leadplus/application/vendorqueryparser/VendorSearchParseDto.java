package ai.leadplus.application.vendorqueryparser;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VendorSearchParseDto {
    private List<Long> serviceIds;
    private List<Long> industryIds;
    private List<Long> specificationIds;
}

package ai.leadplus.application.vendordatapacks;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class VendorAccess {
    private List<String> accessibleIndustryNames;
    private List<String> namedVendorSegments;
    private boolean vendorNullAccess;
    private Long tenantId;
}

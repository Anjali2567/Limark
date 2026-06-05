package ai.leadplus.api.v1.tenantannouncements;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportCrmContactsRequest {

    @NotEmpty
    private List<Long> tenantContactIds;
}

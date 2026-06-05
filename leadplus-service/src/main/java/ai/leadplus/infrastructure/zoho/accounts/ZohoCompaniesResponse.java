package ai.leadplus.infrastructure.zoho.accounts;

import ai.leadplus.infrastructure.zoho.common.ZohoPageInfo;
import lombok.Data;

import java.util.List;

@Data
public class ZohoCompaniesResponse {
    private List<ZohoCompanyResponse> data;
    private ZohoPageInfo info;
    private String error;
}

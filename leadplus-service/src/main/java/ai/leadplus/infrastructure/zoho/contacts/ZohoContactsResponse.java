package ai.leadplus.infrastructure.zoho.contacts;

import ai.leadplus.infrastructure.zoho.common.ZohoPageInfo;
import lombok.Data;
import java.util.List;

@Data
public class ZohoContactsResponse {

    private List<ZohoContactResponse> data;
    private ZohoPageInfo info;
    private String error;
}
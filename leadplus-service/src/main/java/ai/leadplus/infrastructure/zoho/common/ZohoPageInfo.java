package ai.leadplus.infrastructure.zoho.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ZohoPageInfo {

    @JsonProperty("more_records")
    private boolean moreRecords;

    @JsonProperty("page")
    private int page;
}
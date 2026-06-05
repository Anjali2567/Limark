package ai.leadplus.application.campaigns;

import ai.leadplus.application.leaddatapacks.GatedInfo;
import ai.leadplus.application.leads.LeadFilterCriteria;
import ai.leadplus.application.vendordatapacks.VendorAccess;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CampaignProceededEvent extends ApplicationEvent {

    private final CampaignDto campaignDto;
    private final LeadFilterCriteria leadFilter;
    private final Integer contactLimit;
    private final GatedInfo gatedInfo;
    private final VendorAccess vendorAccess;

    public CampaignProceededEvent(Object source,
                                  CampaignDto campaignDto,
                                  LeadFilterCriteria leadFilter,
                                  Integer contactLimit,
                                  GatedInfo gatedInfo,
                                  VendorAccess vendorAccess) {
        super(source);
        this.campaignDto = campaignDto;
        this.leadFilter = leadFilter;
        this.contactLimit = contactLimit;
        this.gatedInfo = gatedInfo;
        this.vendorAccess = vendorAccess;
    }
}
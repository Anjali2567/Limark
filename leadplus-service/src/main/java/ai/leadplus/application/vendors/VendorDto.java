package ai.leadplus.application.vendors;

import ai.leadplus.domain.vendors.Answer;
import ai.leadplus.domain.vendors.Vendor;
import ai.leadplus.domain.vendors.VendorVerificationStatus;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class VendorDto {

    private Long id;
    private Long userId;
    private Long tenantId;
    private String companyName;
    private String description;
    private String logo;
    private String companySize;
    private String phoneNumber;
    private String faxNumber;
    private String salesEmail;
    private String schedulingLink;
    private String website;
    private String videoLink;
    private String annualRevenue;
    private String tagline;
    private String businessHours;
    private String yearEstablished;
    private List<String> languagesSpoken;
    private String teamDescription;
    private String teamPhoto;
    private AddressDto address;
    private SocialMediaDto socialMedia;
    private List<Long> industryIds;
    private List<Long> serviceIds;
    private List<Long> specificationIds;
    private List<String> regionsCovered;
    private List<String> certifications;
    private String minProjectSize;
    private String avgHourlyRate;
    private List<String> clientBudgets;
    private List<String> clientSizes;
    private List<String> clientEmployeeRanges;
    private List<String> clientLocations;
    private boolean openToAnyLocation;
    private List<String> clientIndustries;
    private boolean openToAnyIndustry;
    private VendorVerificationStatus vendorVerificationStatus;
    private List<AnswerDto> questionnaire;
    private String reviewComment;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static VendorDto fromEntity(Vendor vendor) {
        return baseBuilder(VendorDto.builder(), vendor)
                .build();
    }

    @SuppressWarnings("unchecked")
    protected static <T extends VendorDtoBuilder<?, ?>> T baseBuilder(T builder, Vendor vendor) {
        if (vendor == null) {
            return null;
        }
        List<AnswerDto> questionnaire = CollectionUtils.isEmpty(vendor.getQuestionnaire()) ?
                List.of() :
                vendor.getQuestionnaire().stream()
                        .map(AnswerDto::fromEntity)
                        .toList();
        return (T) builder
                .id(vendor.getId())
                .userId(vendor.getUserId())
                .tenantId(vendor.getTenantId())
                .companyName(vendor.getCompanyName())
                .description(vendor.getDescription())
                .logo(vendor.getLogo())
                .companySize(vendor.getCompanySize())
                .phoneNumber(vendor.getPhoneNumber())
                .faxNumber(vendor.getFaxNumber())
                .salesEmail(vendor.getSalesEmail())
                .schedulingLink(vendor.getSchedulingLink())
                .website(vendor.getWebsite())
                .videoLink(vendor.getVideoLink())
                .annualRevenue(vendor.getAnnualRevenue())
                .tagline(vendor.getTagline())
                .businessHours(vendor.getBusinessHours())
                .yearEstablished(vendor.getYearEstablished())
                .languagesSpoken(vendor.getLanguagesSpoken())
                .teamDescription(vendor.getTeamDescription())
                .teamPhoto(vendor.getTeamPhoto())
                .address(AddressDto.fromEntity(vendor.getAddress()))
                .socialMedia(SocialMediaDto.fromEntity(vendor.getSocialMedia()))
                .industryIds(vendor.getIndustryIds())
                .serviceIds(vendor.getServiceIds())
                .specificationIds(vendor.getSpecificationIds())
                .regionsCovered(vendor.getRegionsCovered())
                .certifications(vendor.getCertifications())
                .minProjectSize(vendor.getMinProjectSize())
                .avgHourlyRate(vendor.getAvgHourlyRate())
                .clientBudgets(vendor.getClientBudgets())
                .clientSizes(vendor.getClientSizes())
                .clientEmployeeRanges(vendor.getClientEmployeeRanges())
                .clientLocations(vendor.getClientLocations())
                .openToAnyLocation(vendor.isOpenToAnyLocation())
                .clientIndustries(vendor.getClientIndustries())
                .openToAnyIndustry(vendor.isOpenToAnyIndustry())
                .vendorVerificationStatus(vendor.getVendorVerificationStatus())
                .questionnaire(questionnaire)
                .reviewComment(vendor.getReviewComment())
                .active(vendor.isActive())
                .createdAt(vendor.getCreatedAt())
                .updatedAt(vendor.getUpdatedAt());
    }

    public Vendor toEntity() {
        List<Answer> questionnaire = CollectionUtils.isEmpty(this.questionnaire) ?
                List.of() :
                this.questionnaire.stream()
                        .map(AnswerDto::toEntity)
                        .toList();
        return Vendor.builder()
                .id(id)
                .userId(userId)
                .tenantId(tenantId)
                .companyName(companyName)
                .description(description)
                .logo(logo)
                .companySize(companySize)
                .phoneNumber(phoneNumber)
                .faxNumber(faxNumber)
                .salesEmail(salesEmail)
                .schedulingLink(schedulingLink)
                .website(website)
                .videoLink(videoLink)
                .annualRevenue(annualRevenue)
                .tagline(tagline)
                .businessHours(businessHours)
                .yearEstablished(yearEstablished)
                .languagesSpoken(languagesSpoken)
                .teamDescription(teamDescription)
                .teamPhoto(teamPhoto)
                .address(address != null ? address.toEntity() : null)
                .socialMedia(socialMedia != null ? socialMedia.toEntity() : null)
                .industryIds(industryIds)
                .serviceIds(serviceIds)
                .specificationIds(specificationIds)
                .regionsCovered(regionsCovered)
                .certifications(certifications)
                .minProjectSize(minProjectSize)
                .avgHourlyRate(avgHourlyRate)
                .clientBudgets(clientBudgets)
                .clientSizes(clientSizes)
                .clientEmployeeRanges(clientEmployeeRanges)
                .clientLocations(clientLocations)
                .openToAnyLocation(openToAnyLocation)
                .clientIndustries(clientIndustries)
                .openToAnyIndustry(openToAnyIndustry)
                .vendorVerificationStatus(vendorVerificationStatus)
                .questionnaire(questionnaire)
                .reviewComment(reviewComment)
                .active(active)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}

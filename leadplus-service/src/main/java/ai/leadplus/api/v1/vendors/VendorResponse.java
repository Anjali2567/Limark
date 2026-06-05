package ai.leadplus.api.v1.vendors;

import ai.leadplus.application.vendors.AddressDto;
import ai.leadplus.application.vendors.AnswerDto;
import ai.leadplus.application.vendors.SocialMediaDto;
import ai.leadplus.application.vendors.VendorDto;
import ai.leadplus.domain.vendors.VendorVerificationStatus;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class VendorResponse {

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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static VendorResponse fromDto(VendorDto dto) {
        return baseBuilder(VendorResponse.builder(), dto)
                .questionnaire(dto.getQuestionnaire())
                .serviceIds(dto.getServiceIds())
                .specificationIds(dto.getSpecificationIds())
                .build();
    }

    @SuppressWarnings("unchecked")
    protected static <T extends VendorResponseBuilder<?, ?>> T baseBuilder(T builder, VendorDto dto) {
        if (dto == null) {
            return null;
        }
        return (T) builder
                .id(dto.getId())
                .userId(dto.getUserId())
                .tenantId(dto.getTenantId())
                .companyName(dto.getCompanyName())
                .description(dto.getDescription())
                .logo(dto.getLogo())
                .companySize(dto.getCompanySize())
                .phoneNumber(dto.getPhoneNumber())
                .faxNumber(dto.getFaxNumber())
                .salesEmail(dto.getSalesEmail())
                .schedulingLink(dto.getSchedulingLink())
                .website(dto.getWebsite())
                .videoLink(dto.getVideoLink())
                .annualRevenue(dto.getAnnualRevenue())
                .tagline(dto.getTagline())
                .businessHours(dto.getBusinessHours())
                .yearEstablished(dto.getYearEstablished())
                .languagesSpoken(dto.getLanguagesSpoken())
                .teamDescription(dto.getTeamDescription())
                .teamPhoto(dto.getTeamPhoto())
                .address(dto.getAddress())
                .socialMedia(dto.getSocialMedia())
                .industryIds(dto.getIndustryIds())
                .regionsCovered(dto.getRegionsCovered())
                .certifications(dto.getCertifications())
                .minProjectSize(dto.getMinProjectSize())
                .avgHourlyRate(dto.getAvgHourlyRate())
                .clientBudgets(dto.getClientBudgets())
                .clientSizes(dto.getClientSizes())
                .clientEmployeeRanges(dto.getClientEmployeeRanges())
                .clientLocations(dto.getClientLocations())
                .openToAnyLocation(dto.isOpenToAnyLocation())
                .clientIndustries(dto.getClientIndustries())
                .openToAnyIndustry(dto.isOpenToAnyIndustry())
                .vendorVerificationStatus(dto.getVendorVerificationStatus())
                .reviewComment(dto.getReviewComment())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt());
    }
}

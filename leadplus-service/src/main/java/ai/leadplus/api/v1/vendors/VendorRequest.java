package ai.leadplus.api.v1.vendors;

import ai.leadplus.application.vendors.AddressDto;
import ai.leadplus.application.vendors.AnswerDto;
import ai.leadplus.application.vendors.SocialMediaDto;
import ai.leadplus.application.vendors.VendorDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorRequest {

    @NotBlank
    private String companyName;
    private String description;
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
    @Valid
    private List<AnswerRequest> questionnaire;

    public VendorDto toDto() {
        List<AnswerDto> questionnaireDto = CollectionUtils.isEmpty(questionnaire) ?
                List.of() :
                questionnaire.stream()
                        .map(AnswerRequest::toDto)
                        .toList();
        return VendorDto.builder()
                .companyName(companyName)
                .description(description)
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
                .address(address)
                .socialMedia(socialMedia)
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
                .questionnaire(questionnaireDto)
                .build();
    }
}

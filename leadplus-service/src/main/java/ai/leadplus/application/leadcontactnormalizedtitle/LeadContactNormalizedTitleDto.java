package ai.leadplus.application.leadcontactnormalizedtitle;

import ai.leadplus.domain.leadcontactnormalizedtitle.TitleAbbreviation;
import ai.leadplus.domain.leadcontactnormalizedtitle.LeadContactNormalizedTitle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadContactNormalizedTitleDto {

    private Long id;
    private Long leadContactId;
    private String originalTitle;
    private String canonicalTitle;
    private String seniority;
    private List<String> normalizedTitles;
    private List<String> keywords;
    private List<TitleAbbreviation> titleAbbreviations;
    private LocalDateTime createdAt;

    public static LeadContactNormalizedTitleDto toDto(LeadContactNormalizedTitle leadContactNormalizedTitle) {
        if (leadContactNormalizedTitle == null) {
            return null;
        }
        return LeadContactNormalizedTitleDto.builder()
                .id(leadContactNormalizedTitle.getId())
                .leadContactId(leadContactNormalizedTitle.getLeadContactId())
                .originalTitle(leadContactNormalizedTitle.getOriginalTitle())
                .canonicalTitle(leadContactNormalizedTitle.getCanonicalTitle())
                .seniority(leadContactNormalizedTitle.getSeniority())
                .normalizedTitles(leadContactNormalizedTitle.getNormalizedTitles())
                .keywords(leadContactNormalizedTitle.getKeywords())
                .titleAbbreviations(leadContactNormalizedTitle.getTitleAbbreviations())
                .createdAt(leadContactNormalizedTitle.getCreatedAt())
                .build();
    }

    public LeadContactNormalizedTitle toEntity() {
        return LeadContactNormalizedTitle.builder()
                .id(id)
                .leadContactId(leadContactId)
                .originalTitle(originalTitle)
                .canonicalTitle(canonicalTitle)
                .seniority(seniority)
                .normalizedTitles(normalizedTitles)
                .keywords(keywords)
                .titleAbbreviations(titleAbbreviations)
                .createdAt(createdAt)
                .build();
    }
}

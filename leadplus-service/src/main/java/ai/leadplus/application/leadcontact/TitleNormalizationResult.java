package ai.leadplus.application.leadcontact;

import ai.leadplus.domain.leadcontactnormalizedtitle.TitleAbbreviation;
import lombok.Data;

import java.util.List;

@Data
public class TitleNormalizationResult {

    private String canonicalTitle;
    private String seniority;
    private List<String> normalizedTitles;
    private List<String> keywords;
    private List<TitleAbbreviation> titleAbbreviations;
}
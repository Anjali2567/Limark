package ai.leadplus.application.leadcontact;

import ai.leadplus.infrastructure.springai.SpringAiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeadContactAiService {

    private final Optional<SpringAiClient> springAiClient;

    private static final String TITLE_NORMALIZATION_PROMPT = """
            You are an expert in job title normalization and professional title analysis.
            
            Given a job title, extract structured information and return STRICT JSON.
            
            Return the following fields:
            
            canonicalTitle → the clean normalized title representing the primary role.
            
            seniority → one of:
            [C_LEVEL, EVP, SVP, VP, DIRECTOR, HEAD, MANAGER, LEAD, PRINCIPAL, INDIVIDUAL]
            
            normalizedTitles → a list of meaningful normalized titles derived from the input.
            
            keywords → meaningful search phrases derived from the title.
            
            IMPORTANT RULES:
            - Keywords MUST be meaningful phrases.
            - Do NOT return single generic words like "Head", "Manager", or "Director".
            - Instead return full phrases such as:
              "Head of Supply Chain"
              "Supply Chain"
              "Supply Chain Quality"
              "Global Operations"
            
            titleAbbreviations → return structured abbreviation objects with BOTH short and full forms.
            
            Each abbreviation must contain:
            shortForm
            fullForm
            
            Example format:
            
            "titleAbbreviations": [
              { "shortForm": "VP", "fullForm": "Vice President" },
              { "shortForm": "Ops", "fullForm": "Operations" },
              { "shortForm": "SC", "fullForm": "Supply Chain" }
            ]
            
            Rules for abbreviations:
            - Vice President → VP
            - Senior Vice President → SVP
            - Executive Vice President → EVP
            - Chief Executive Officer → CEO
            - Operations → Ops
            - Supply Chain → SC
            - Information Technology → IT
            - Human Resources → HR
            - Research and Development → R&D
            
            Abbreviations should include both:
            1. role abbreviations
            2. functional abbreviations
            
            Ignore:
            - company names
            - product names
            - locations
            
            If the title contains multiple roles (like VP / Head of X), extract both.
            
            Return ONLY valid JSON.
            """;

    public TitleNormalizationResult normalizeTitle(String title) {

        if (!StringUtils.hasText(title)) {
            return null;
        }

        if (!springAiClient.isPresent()) {
            log.warn("SpringAiClient not available, skipping AI title normalization");
            return null;
        }

        log.info("Normalizing title with AI: {}", title);

        return springAiClient.get().getChatCompletion(
                title,
                TITLE_NORMALIZATION_PROMPT,
                TitleNormalizationResult.class
        );
    }
}

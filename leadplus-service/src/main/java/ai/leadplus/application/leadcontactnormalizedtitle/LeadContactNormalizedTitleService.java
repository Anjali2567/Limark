package ai.leadplus.application.leadcontactnormalizedtitle;

import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.leadcontact.LeadContactAiService;
import ai.leadplus.application.leadcontact.LeadContactDto;
import ai.leadplus.application.leadcontact.TitleNormalizationResult;
import ai.leadplus.domain.leadcontactnormalizedtitle.LeadContactNormalizedTitle;
import ai.leadplus.domain.leadcontactnormalizedtitle.LeadContactNormalizedTitleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeadContactNormalizedTitleService {

    private final LeadContactNormalizedTitleRepository repository;
    private final LeadContactAiService aiService;

    public LeadContactNormalizedTitleDto createOrUpdateLeadContactNormalizedTitle(LeadContactDto dto) {

        validateInput(dto);

        Optional<LeadContactNormalizedTitle> existingOpt =
                repository.findByLeadContactId(dto.getId());

        if (existingOpt.isPresent()) {
            return updateIfRequired(existingOpt.get(), dto);
        }

        return createNew(dto);
    }

    public LeadContactNormalizedTitleDto getLeadContactNormalizedTitle(Long leadContactId) {
        LeadContactNormalizedTitle entity = repository.findByLeadContactId(leadContactId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Lead contact normalized title not found for lead contact id: " + leadContactId));

        return LeadContactNormalizedTitleDto.toDto(entity);
    }

    private LeadContactNormalizedTitleDto updateIfRequired(
            LeadContactNormalizedTitle existing,
            LeadContactDto dto
    ) {
        if (isSameTitle(existing.getOriginalTitle(), dto.getTitle())) {
            return LeadContactNormalizedTitleDto.toDto(existing);
        }

        TitleNormalizationResult result = normalize(dto.getTitle());
        mapToEntity(existing, dto, result);

        LeadContactNormalizedTitle saved = repository.save(existing);
        return LeadContactNormalizedTitleDto.toDto(saved);
    }

    private LeadContactNormalizedTitleDto createNew(LeadContactDto dto) {

        TitleNormalizationResult result = normalize(dto.getTitle());

        LeadContactNormalizedTitle entity = LeadContactNormalizedTitle.builder()
                .leadContactId(dto.getId())
                .build();

        mapToEntity(entity, dto, result);

        LeadContactNormalizedTitle saved = repository.save(entity);
        return LeadContactNormalizedTitleDto.toDto(saved);
    }

    private TitleNormalizationResult normalize(String title) {
        return aiService.normalizeTitle(title);
    }

    private void mapToEntity(
            LeadContactNormalizedTitle entity,
            LeadContactDto dto,
            TitleNormalizationResult result
    ) {
        entity.setLeadContactId(dto.getId());
        entity.setOriginalTitle(dto.getTitle());
        entity.setCanonicalTitle(result.getCanonicalTitle());
        entity.setSeniority(result.getSeniority());
        entity.setNormalizedTitles(result.getNormalizedTitles());
        entity.setKeywords(result.getKeywords());
        entity.setTitleAbbreviations(result.getTitleAbbreviations());
    }

    private boolean isSameTitle(String oldTitle, String newTitle) {
        return StringUtils.hasText(oldTitle)
                && StringUtils.hasText(newTitle)
                && oldTitle.trim().equalsIgnoreCase(newTitle.trim());
    }

    private void validateInput(LeadContactDto dto) {
        if (dto == null || dto.getId() == null) {
            throw new BadRequestException("LeadContactDto or id must not be null");
        }
    }
}
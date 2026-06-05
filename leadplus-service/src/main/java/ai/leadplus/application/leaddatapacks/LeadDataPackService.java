package ai.leadplus.application.leaddatapacks;

import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.industries.IndustryService;
import ai.leadplus.domain.industries.Industry;
import ai.leadplus.domain.leaddatapacks.LeadDataPack;
import ai.leadplus.domain.leaddatapacks.LeadDataPackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeadDataPackService {

    private final IndustryService industryService;
    private final LeadDataPackRepository leadDataPackRepository;

    public LeadDataPackDto createLeadDataPack(LeadDataPackDto dto) {
        if(!industryService.areIndustriesActive(dto.getIndustryIds())) {
            throw new BadRequestException("Industries are invalid");
        }
        LeadDataPack saved = leadDataPackRepository.save(dto.toEntity());
        return LeadDataPackDto.fromEntity(saved);
    }

    public LeadDataPackDto getById(String id) {
        return LeadDataPackDto.fromEntity(findLeadDataPackById(id));
    }

    public Page<LeadDataPackDto> getAllLeadDataPacks(Pageable pageable) {
        return leadDataPackRepository.findAllByActiveTrue(pageable)
                .map(LeadDataPackDto::fromEntity);
    }

    public LeadDataPackDto updateLeadDataPack(String id, LeadDataPackDto dto) {
        if(!industryService.areIndustriesActive(dto.getIndustryIds())) {
            throw new BadRequestException("Industries are invalid");
        }

        LeadDataPack existing = findLeadDataPackById(id);
        existing.setName(dto.getName());
        existing.setIndustryIds(dto.getIndustryIds());

        LeadDataPack saved = leadDataPackRepository.save(existing);
        return LeadDataPackDto.fromEntity(saved);
    }

    public void deleteLeadDataPackById(String id) {
        LeadDataPack existing = findLeadDataPackById(id);
        existing.setActive(false);
        leadDataPackRepository.save(existing);
    }

    private LeadDataPack findLeadDataPackById(String id) {
        return leadDataPackRepository.findByIdAndActiveTrue(Long.parseLong(id))
                .orElseThrow(() -> new ResourceNotFoundException("LeadDataPack not found with id " + id));
    }

    public Optional<LeadDataPackDto> findBySlug(String slug) {
        return leadDataPackRepository.findBySlugAndActiveTrue(slug)
                .map(LeadDataPackDto::fromEntity);
    }

    public List<LeadDataPack> findAllByIdIn(Collection<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) return List.of();
        List<Long> longIds = ids.stream().toList();
        return leadDataPackRepository.findAllByIdInAndActiveTrue(longIds);
    }

    public GatedInfo getGatedInfo() {
        List<LeadDataPack> allPacks = leadDataPackRepository.findAllByActiveTrue();
        if (allPacks.isEmpty()) return new GatedInfo(List.of(), false);

        Set<Long> industryIds = allPacks.stream()
                .filter(p -> !CollectionUtils.isEmpty(p.getIndustryIds()))
                .flatMap(p -> p.getIndustryIds().stream())
                .collect(Collectors.toSet());

        List<Industry> industries = industryService.getActiveIndustriesByIds(industryIds);

        List<String> namedSegments = industries.stream()
                .filter(i -> !CollectionUtils.isEmpty(i.getSegments()))
                .flatMap(i -> i.getSegments().stream())
                .distinct().toList();

        return new GatedInfo(namedSegments, true);
    }
}
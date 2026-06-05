package ai.leadplus.application.industries;

import ai.leadplus.application.aws.AwsS3Service;
import ai.leadplus.application.campaignagent.tools.IndustryAccessDto;
import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.domain.industries.Industry;
import ai.leadplus.domain.industries.IndustryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IndustryService {

    private final IndustryRepository industryRepository;
    private final AwsS3Service awsS3Service;

    public IndustryDto createIndustry(IndustryDto industryDto) {
        if (industryRepository.existsBySlugIgnoreCaseAndActiveTrue(industryDto.getSlug())) {
            throw new BadRequestException("Duplicate industry: " + industryDto.getName());
        }
        Industry industry = industryDto.toEntity();
        industry.setSlug(industryDto.getSlug().toLowerCase());
        industry.setActive(true);
        Industry savedIndustry = industryRepository.save(industry);
        return IndustryDto.fromEntity(savedIndustry);
    }

    public IndustryDto findBySlug(String slug) {
        return industryRepository.findBySlugAndActiveTrue(slug).map(IndustryDto::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Industry not found with slug: " + slug));
    }

    public Page<IndustryDto> getAllIndustries(String query, Pageable pageable) {
        Page<Industry> industries;
        if (StringUtils.hasText(query)) {
            industries = industryRepository.findAllByNameContainingIgnoreCaseAndDisabledFalseAndActiveTrue(query, pageable);
        } else {
            industries = industryRepository.findAllByDisabledFalseAndActiveTrue(pageable);
        }
        return industries.map(IndustryDto::fromEntity);
    }

    public List<IndustryAccessDto> getAllIndustries() {
        return industryRepository.findAllIndustryAccess();
    }

    public Page<IndustryDto> getAllAdminIndustries(String query, Pageable pageable) {
        Page<Industry> industries;
        if (StringUtils.hasText(query)) {
            industries = industryRepository.findAllByNameContainingIgnoreCaseAndActiveTrue(query, pageable);
        } else {
            industries = industryRepository.findAllByActiveTrue(pageable);
        }
        return industries.map(IndustryDto::fromEntity);
    }

    public IndustryDto getIndustryById(String id) {
        Industry industry = findIndustryById(id);
        return IndustryDto.fromEntity(industry);
    }

    private Industry findIndustryById(String id) {
        return industryRepository.findByIdAndActiveTrue(Long.parseLong(id))
                .orElseThrow(() -> new ResourceNotFoundException("Industry not found with id: " + id));
    }

    public IndustryDto updateIndustry(String id, IndustryDto industryDto) {
        Industry existingIndustry = findIndustryById(id);
        if (!industryDto.getSlug().equalsIgnoreCase(existingIndustry.getSlug()) && industryRepository.existsBySlugIgnoreCaseAndActiveTrue(industryDto.getSlug())) {
            throw new BadRequestException("Duplicate industry: " + industryDto.getName());
        }
        existingIndustry.setName(industryDto.getName());
        existingIndustry.setDescription(industryDto.getDescription());
        existingIndustry.setSlug(industryDto.getSlug().toLowerCase());
        existingIndustry.setSegments(industryDto.getSegments() != null ? industryDto.getSegments() : List.of());
        Industry updatedIndustry = industryRepository.save(existingIndustry);
        return IndustryDto.fromEntity(updatedIndustry);
    }

    public IndustryDto uploadIndustryImageById(String id, MultipartFile file) {
        Industry industry = findIndustryById(id);
        String imageUrl = awsS3Service.uploadFile(file, "industries", id);
        industry.setImage(imageUrl);
        return IndustryDto.fromEntity(industryRepository.save(industry));
    }

    public IndustryDto updateDisableStatus(String id, boolean disabled) {
        Industry existingIndustry = findIndustryById(id);
        if (existingIndustry.isDisabled() == disabled) {
            return IndustryDto.fromEntity(existingIndustry);
        }
        existingIndustry.setDisabled(disabled);
        industryRepository.save(existingIndustry);
        return IndustryDto.fromEntity(existingIndustry);
    }

    public void validateIndustryId(Long industryId) {
        if (industryId == null)
            return;
        if (!industryRepository.existsByIdAndDisabledFalseAndActiveTrue(industryId)) {
            throw new BadRequestException("Invalid Industry ID: " + industryId);
        }
    }

    public void validateIndustryIds(List<Long> industryIds) {
        if (CollectionUtils.isEmpty(industryIds))
            return;
        industryIds = industryIds.stream().distinct().toList();
        long count = industryRepository.countByIdInAndDisabledFalseAndActiveTrue(industryIds);
        if (count != industryIds.size()) {
            throw new BadRequestException("One or more industry IDs are invalid");
        }
    }

    public boolean areIndustriesActive(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }

        long count = industryRepository.countByIdInAndActiveTrue(ids);
        return count == ids.size();
    }

    public boolean existsByIdAndActiveTrue(Long id) {
        return industryRepository.existsByIdAndActiveTrue(id);
    }

    public List<Industry> getActiveIndustriesByIds(Collection<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) return List.of();
        return industryRepository.findAllByIdInAndActiveTrue(ids);
    }

    public List<String> getAllActiveIndustryNames() {
        return industryRepository.findAllByDisabledFalseAndActiveTrue()
                .stream().map(Industry::getName).toList();
    }

    public List<Long> findIndustryIdsByNames(List<String> names) {
        if (CollectionUtils.isEmpty(names)) return List.of();
        return industryRepository.findAllByNameInIgnoreCaseAndDisabledFalseAndActiveTrue(names)
                .stream().map(Industry::getId).toList();
    }
}

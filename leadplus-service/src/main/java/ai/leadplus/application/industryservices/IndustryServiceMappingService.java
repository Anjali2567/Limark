package ai.leadplus.application.industryservices;

import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.services.ServiceCatalogueService;

import ai.leadplus.domain.industryservices.IndustryServiceMapping;
import ai.leadplus.domain.industryservices.IndustryServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IndustryServiceMappingService {

    private final ai.leadplus.application.industries.IndustryService industryService;
    private final ServiceCatalogueService serviceCatalogueService;
    private final IndustryServiceRepository industryServiceRepository;

    public void assignServiceToIndustry(Long industryId, Long serviceId) {
        if (industryServiceRepository.existsByIndustryIdAndServiceId(industryId, serviceId)) {
            throw new BadRequestException("Service is already assigned to this industry");
        }
        serviceCatalogueService.validateServiceId(serviceId);
        industryService.validateIndustryId(industryId);
        IndustryServiceMapping mapping = IndustryServiceMapping.builder()
                .industryId(industryId)
                .serviceId(serviceId)
                .build();
        industryServiceRepository.save(mapping);
    }

    @Transactional
    public void removeServiceFromIndustry(Long industryId, Long serviceId) {
        industryService.validateIndustryId(industryId);
        serviceCatalogueService.validateServiceId(serviceId);
        industryServiceRepository.deleteByIndustryIdAndServiceId(industryId, serviceId);
    }

    public List<Long> getServiceIdsByIndustry(Long industryId) {
        industryService.validateIndustryId(industryId);
        return industryServiceRepository.findAllByIndustryId(industryId)
                .stream()
                .map(IndustryServiceMapping::getServiceId)
                .toList();
    }

    public List<Long> getIndustryIdsByService(Long serviceId) {
        serviceCatalogueService.validateServiceId(serviceId);
        return industryServiceRepository.findAllByServiceId(serviceId)
                .stream()
                .map(IndustryServiceMapping::getIndustryId)
                .toList();
    }
}

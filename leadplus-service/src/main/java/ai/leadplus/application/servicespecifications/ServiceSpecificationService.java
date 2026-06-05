package ai.leadplus.application.servicespecifications;

import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.services.ServiceCatalogueService;
import ai.leadplus.application.specifications.SpecificationService;
import ai.leadplus.domain.servicespecifications.ServiceSpecification;
import ai.leadplus.domain.servicespecifications.ServiceSpecificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceSpecificationService {

    private final ServiceSpecificationRepository serviceSpecificationRepository;
    private final ServiceCatalogueService serviceCatalogueService;
    private final SpecificationService specificationService;

    public void assignSpecificationToService(Long serviceId, Long specificationId) {
        serviceCatalogueService.validateServiceId(serviceId);
        specificationService.validateSpecificationId(specificationId);
        if (serviceSpecificationRepository.existsByServiceIdAndSpecificationId(serviceId, specificationId)) {
            throw new BadRequestException("Specification is already assigned to this service");
        }
        ServiceSpecification spec = ServiceSpecification.builder()
                .serviceId(serviceId)
                .specificationId(specificationId)
                .build();
        serviceSpecificationRepository.save(spec);
    }

    @Transactional
    public void removeSpecificationFromService(Long serviceId, Long specificationId) {
        serviceSpecificationRepository.deleteByServiceIdAndSpecificationId(serviceId, specificationId);
    }

    public List<Long> getSpecificationIdsByService(Long serviceId) {
        return serviceSpecificationRepository.findAllByServiceId(serviceId)
                .stream()
                .map(ServiceSpecification::getSpecificationId)
                .toList();
    }
}

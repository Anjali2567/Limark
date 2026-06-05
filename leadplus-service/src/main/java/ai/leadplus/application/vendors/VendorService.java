package ai.leadplus.application.vendors;

import ai.leadplus.application.aws.EmailService;
import ai.leadplus.application.aws.AwsS3Service;
import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.exception.UnauthorizedException;
import ai.leadplus.application.industries.IndustryService;
import ai.leadplus.application.leaddatapacks.LeadDataPackService;
import ai.leadplus.application.question.QuestionService;
import ai.leadplus.application.services.ServiceDto;
import ai.leadplus.application.services.ServiceCatalogueService;
import ai.leadplus.application.specifications.SpecificationDto;
import ai.leadplus.application.specifications.SpecificationService;
import ai.leadplus.application.users.UserDto;
import ai.leadplus.application.users.UserService;
import ai.leadplus.domain.vendordatapacks.VendorDataPack;
import ai.leadplus.domain.vendordatapacks.VendorDataPackRepository;
import ai.leadplus.domain.vendors.Answer;
import ai.leadplus.domain.vendors.Vendor;
import ai.leadplus.domain.vendors.VendorRepository;
import ai.leadplus.domain.vendors.VendorVerificationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VendorService {

    private final AwsS3Service awsS3Service;
    private final VendorRepository vendorRepository;
    private final VendorSearchService vendorSearchService;
    private final ServiceCatalogueService serviceCatalogueService;
    private final QuestionService questionService;
    private final SpecificationService specificationService;
    private final IndustryService industryService;
    private final EmailService emailService;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;
    private final LeadDataPackService leadDataPackService;
    private final VendorDataPackRepository vendorDataPackRepository;

    @Value("${client.url}")
    private String clientUrl;

    public VendorDto createVendor(VendorDto dto) {
        Vendor vendor = dto.toEntity();

        if (!CollectionUtils.isEmpty(vendor.getIndustryIds())) {
            industryService.validateIndustryIds(vendor.getIndustryIds());
        }

        if (!CollectionUtils.isEmpty(vendor.getServiceIds())) {
            serviceCatalogueService.validateServiceIds(vendor.getServiceIds());
        }

        if (!CollectionUtils.isEmpty(vendor.getSpecificationIds())) {
            specificationService.validateSpecificationIds(vendor.getSpecificationIds());
        }

        if (!CollectionUtils.isEmpty(vendor.getQuestionnaire())) {
            List<Long> questionIds = vendor.getQuestionnaire().stream()
                    .map(Answer::getQuestionId)
                    .distinct()
                    .toList();
            questionService.validateQuestionIds(questionIds);
        }

        if (vendor.getVendorVerificationStatus() == null) {
            vendor.setVendorVerificationStatus(VendorVerificationStatus.INCOMPLETE);
        }

        vendor = vendorRepository.save(vendor);
        return VendorDto.fromEntity(vendor);
    }

    public Page<VendorDto> getAllVendors(String query, List<VendorVerificationStatus> vendorVerificationStatusList, Pageable pageable) {
        Page<Vendor> vendorPage = vendorSearchService.searchVendors(
                query,
                vendorVerificationStatusList,
                pageable
        );
        return vendorPage.map(VendorDto::fromEntity);
    }

    public Page<VendorDetailDto> searchVendors(
            List<Long> industryIds,
            List<Long> serviceIds,
            List<Long> specificationIds,
            List<String> certifications,
            Pageable pageable
    ) {
        Page<Vendor> vendorPage = vendorSearchService.searchVendors(
                industryIds,
                serviceIds,
                specificationIds,
                certifications,
                List.of(VendorVerificationStatus.APPROVED),
                pageable
        );

        List<Long> serviceIdList = new ArrayList<>();
        List<Long> specIdList = new ArrayList<>();

        vendorPage.forEach(vendor -> {
            if (!CollectionUtils.isEmpty(vendor.getServiceIds())) {
                serviceIdList.addAll(vendor.getServiceIds());
            }
            if (!CollectionUtils.isEmpty(vendor.getSpecificationIds())) {
                specIdList.addAll(vendor.getSpecificationIds());
            }
        });

        List<Long> distinctServiceIds = serviceIdList.stream().distinct().toList();
        List<Long> distinctSpecIds = specIdList.stream().distinct().toList();

        Map<Long, ServiceDto> serviceMap = serviceCatalogueService.getAllServicesAsMap(distinctServiceIds);
        Map<Long, SpecificationDto> specMap = specificationService.getAllSpecificationsAsMap(distinctSpecIds);

        return vendorPage.map(vendor -> {
            List<ServiceDto> serviceDtos = new ArrayList<>();
            if (!CollectionUtils.isEmpty(vendor.getServiceIds())) {
                serviceDtos = vendor.getServiceIds().stream()
                        .map(serviceMap::get)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }

            List<SpecificationDto> specDtos = new ArrayList<>();
            if (!CollectionUtils.isEmpty(vendor.getSpecificationIds())) {
                specDtos = vendor.getSpecificationIds().stream()
                        .map(specMap::get)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }

            return VendorDetailDto.fromEntity(vendor, serviceDtos, specDtos);
        });
    }

    public VendorDto updateVendorToPending(Long userId) {
        Vendor existing = findVendorByUserId(userId);
        existing.setVendorVerificationStatus(VendorVerificationStatus.PENDING);
        vendorRepository.save(existing);
        return VendorDto.fromEntity(existing);
    }

    @Transactional
    public VendorDto updateVendorStatus(Long id, VendorVerificationStatus status) {
        return updateVendorStatus(id, status, null);
    }

    @Transactional
    public VendorDto updateVendorStatus(Long id, VendorVerificationStatus status, String reviewComment) {
        Vendor existing = findVendorById(id);
        existing.setVendorVerificationStatus(status);
        existing.setReviewComment(reviewComment);
        vendorRepository.save(existing);
        UserDto userDto = userService.getUserById(existing.getUserId());

        String loginUrl = clientUrl + "/login";

        if (status == VendorVerificationStatus.APPROVED) {
            emailService.sendVendorApprovedEmail(userDto.getEmail(), userDto.getName(), loginUrl);
        } else if (status == VendorVerificationStatus.REJECTED) {
            emailService.sendVendorRejectedEmail(userDto.getEmail(), userDto.getName(), reviewComment);
        }
        VendorDto vendorDto = VendorDto.fromEntity(existing);
        if (status == VendorVerificationStatus.APPROVED) {
            VendorApprovedEvent event = new VendorApprovedEvent(this, vendorDto);
            eventPublisher.publishEvent(event);
        }
        return vendorDto;
    }

    public VendorDto becomeVendor(Long userId) {
        Optional<Vendor> existingVendor = vendorRepository.findByUserIdAndActiveTrue(userId);
        if (existingVendor.isPresent()) {
            return VendorDto.fromEntity(existingVendor.get());
        }
        UserDto userDto = userService.getUserById(userId);
        VendorDto vendorDto = createVendorFromUser(userDto);
        userService.createVendorUser(userDto);
        return vendorDto;
    }

    @Transactional
    public VendorDto createVendorFromUser(UserDto userDto) {
        VendorDto vendorDto = VendorDto.builder()
                .userId(userDto.getId())
                .tenantId(userDto.getTenantId())
                .active(true)
                .vendorVerificationStatus(VendorVerificationStatus.INCOMPLETE) // Temporarily set to APPROVED.
                .build();
        VendorDto saved = createVendor(vendorDto);
        autoAssignSoftwareDataPack(saved);
        // Temporarily set to APPROVED.
//        if (saved.getVendorVerificationStatus() == VendorVerificationStatus.APPROVED) {
//            VendorApprovedEvent event = new VendorApprovedEvent(this, saved);
//            eventPublisher.publishEvent(event);
//        }
        return saved;
    }

    public VendorDto updateVendor(Long userId, VendorDto dto) {
        Vendor existing = findVendorByUserId(userId);

        if (!Objects.equals(existing.getIndustryIds(), dto.getIndustryIds())) {
            industryService.validateIndustryIds(dto.getIndustryIds());
            existing.setIndustryIds(dto.getIndustryIds());
        }

        if (!Objects.equals(existing.getServiceIds(), dto.getServiceIds())) {
            serviceCatalogueService.validateServiceIds(dto.getServiceIds());
            existing.setServiceIds(dto.getServiceIds());
        }

        if (!Objects.equals(existing.getSpecificationIds(), dto.getSpecificationIds())) {
            specificationService.validateSpecificationIds(dto.getSpecificationIds());
            existing.setSpecificationIds(dto.getSpecificationIds());
        }

        List<Answer> questionnaire = CollectionUtils.isEmpty(dto.getQuestionnaire()) ? List.of() :
                dto.getQuestionnaire().stream()
                        .map(AnswerDto::toEntity)
                        .toList();
        if (!Objects.equals(existing.getQuestionnaire(), questionnaire)) {
            List<Long> questionIds = questionnaire.stream()
                    .map(Answer::getQuestionId)
                    .distinct()
                    .toList();
            questionService.validateQuestionIds(questionIds);
            existing.setQuestionnaire(questionnaire);
        }

        existing.setCompanyName(dto.getCompanyName());
        existing.setDescription(dto.getDescription());
        existing.setCompanySize(dto.getCompanySize());
        existing.setPhoneNumber(dto.getPhoneNumber());
        existing.setFaxNumber(dto.getFaxNumber());
        existing.setSalesEmail(dto.getSalesEmail());
        existing.setSchedulingLink(dto.getSchedulingLink());
        existing.setWebsite(dto.getWebsite());
        existing.setVideoLink(dto.getVideoLink());
        existing.setAnnualRevenue(dto.getAnnualRevenue());
        existing.setTagline(dto.getTagline());
        existing.setBusinessHours(dto.getBusinessHours());
        existing.setYearEstablished(dto.getYearEstablished());
        existing.setLanguagesSpoken(dto.getLanguagesSpoken());
        existing.setTeamDescription(dto.getTeamDescription());
        existing.setTeamPhoto(dto.getTeamPhoto());
        existing.setAddress(dto.getAddress() != null ? dto.getAddress().toEntity() : null);
        existing.setSocialMedia(dto.getSocialMedia() != null ? dto.getSocialMedia().toEntity() : null);
        existing.setRegionsCovered(dto.getRegionsCovered());
        existing.setCertifications(dto.getCertifications());
        existing.setMinProjectSize(dto.getMinProjectSize());
        existing.setAvgHourlyRate(dto.getAvgHourlyRate());
        existing.setClientBudgets(dto.getClientBudgets());
        existing.setClientSizes(dto.getClientSizes());
        existing.setClientEmployeeRanges(dto.getClientEmployeeRanges());
        existing.setClientLocations(dto.getClientLocations());
        existing.setOpenToAnyLocation(dto.isOpenToAnyLocation());
        existing.setClientIndustries(dto.getClientIndustries());
        existing.setOpenToAnyIndustry(dto.isOpenToAnyIndustry());
        existing.setReviewComment(dto.getReviewComment());

        vendorRepository.save(existing);
        return VendorDto.fromEntity(existing);
    }

    public VendorDto uploadVendorLogo(Long userId, MultipartFile file) {
        Vendor existing = findVendorByUserId(userId);
        String logoUrl = awsS3Service.uploadFile(file, "vendors", String.valueOf(userId));
        existing.setLogo(logoUrl);
        return VendorDto.fromEntity(vendorRepository.save(existing));
    }

    public VendorDto clearVendorLogo(Long userId) {
        Vendor existing = findVendorByUserId(userId);
        existing.setLogo(null);
        return VendorDto.fromEntity(vendorRepository.save(existing));
    }

    public VendorDto getVendorByUserId(Long userId) {
        return VendorDto.fromEntity(findVendorByUserId(userId));
    }

    public VendorDto getVendorById(Long id) {
        return VendorDto.fromEntity(findVendorById(id));
    }

    public VendorDetailDto getVendorDetailById(Long vendorId) {
        Vendor vendor = findVendorById(vendorId);

        List<Long> serviceIdList = CollectionUtils.isEmpty(vendor.getServiceIds()) ? List.of() : vendor.getServiceIds();
        List<Long> specIdList = CollectionUtils.isEmpty(vendor.getSpecificationIds()) ? List.of() : vendor.getSpecificationIds();

        Map<Long, ServiceDto> serviceMap = serviceCatalogueService.getAllServicesAsMap(serviceIdList);
        Map<Long, SpecificationDto> specMap = specificationService.getAllSpecificationsAsMap(specIdList);

        List<ServiceDto> serviceDtos = serviceIdList.stream()
                .map(serviceMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<SpecificationDto> specDtos = specIdList.stream()
                .map(specMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return VendorDetailDto.fromEntity(vendor, serviceDtos, specDtos);
    }

    public VendorDto getAuthenticatedVendor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            throw new UnauthorizedException("User is not authenticated");
        }

        Long userId = Long.parseLong(authentication.getName());

        Vendor vendor = findVendorByUserId(userId);
        return VendorDto.fromEntity(vendor);
    }

    public VendorDto validateAuthenticatedVendorId(Long vendorId) {
        VendorDto vendorDto = getAuthenticatedVendor();
        if (!vendorDto.getId().equals(vendorId)) {
            throw new UnauthorizedException("Unauthorized access");
        }
        return vendorDto;
    }

    private Vendor findVendorByUserId(Long userId) {
        return vendorRepository.findByUserIdAndActiveTrue(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with userId: " + userId));
    }

    private Vendor findVendorById(Long id) {
        return vendorRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + id));
    }

    public void validateVendorIds(List<Long> vendorIds) {
        if (CollectionUtils.isEmpty(vendorIds))
            return;
        vendorIds = vendorIds.stream().distinct().toList();
        long count = vendorRepository.countAllByIdInAndActiveTrue(vendorIds);
        if (count != vendorIds.size()) {
            throw new BadRequestException("One or more service IDs are invalid");
        }
    }

    public List<VendorDto> getVendorsByIds(List<Long> vendorIds) {
        if (CollectionUtils.isEmpty(vendorIds)) return List.of();

        vendorIds = vendorIds.stream().distinct().toList();
        return vendorRepository.findAllByIdInAndActiveTrue(vendorIds).stream()
                .map(VendorDto::fromEntity)
                .toList();
    }

    public boolean areVendorsActive(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }

        long count = vendorRepository.countByIdInAndActiveTrue(ids);
        return count == ids.size();
    }

    private void autoAssignSoftwareDataPack(VendorDto vendor) {
        leadDataPackService.findBySlug("software").ifPresentOrElse(
                datapack -> {
                    VendorDataPack vendorDataPack = VendorDataPack.builder()
                            .vendorId(vendor.getId())
                            .tenantId(vendor.getTenantId())
                            .leadDataPackId(datapack.getId())
                            .active(true)
                            .build();
                    vendorDataPackRepository.save(vendorDataPack);
                    log.info("Auto-assigned software datapack to vendor {}", vendor.getId());
                },
                () -> log.warn("Software datapack (slug='software') not found — skipping auto-assignment for vendor {}", vendor.getId())
        );
    }
}

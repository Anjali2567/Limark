package ai.leadplus.application.vendordatapacks;

import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.industries.IndustryService;
import ai.leadplus.application.leaddatapacks.LeadDataPackService;
import ai.leadplus.application.vendors.VendorDto;
import ai.leadplus.application.vendors.VendorService;
import ai.leadplus.domain.industries.Industry;
import ai.leadplus.domain.vendordatapacks.VendorDataPack;
import ai.leadplus.domain.vendordatapacks.VendorDataPackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VendorDataPackService {

    private final VendorDataPackRepository vendorDataPackRepository;
    private final VendorService vendorService;
    private final LeadDataPackService leadDataPackService;
    private final IndustryService industryService;

    public VendorDataPackDto createVendorDataPack(VendorDataPackDto dto) {
        VendorDto vendorDto = vendorService.getVendorById(dto.getVendorId());
        dto.setActive(true);
        dto.setTenantId(vendorDto.getTenantId());
        VendorDataPack saved = vendorDataPackRepository.save(dto.toEntity());
        return VendorDataPackDto.fromEntity(saved);
    }

    public VendorDataPackDto getById(String id) {
        return VendorDataPackDto.fromEntity(findActiveById(id));
    }

    public List<VendorDataPackDto> getAll(String tenantId, String vendorId) {
        return vendorDataPackRepository
                .findAllByTenantIdAndVendorIdAndActiveTrue(Long.parseLong(tenantId), Long.parseLong(vendorId))
                .stream()
                .map(VendorDataPackDto::fromEntity)
                .toList();
    }

    public void deleteVendorDataPackById(Long id, Long vendorId) {
        VendorDto vendorDto = vendorService.getVendorById(vendorId);
        VendorDataPack existing = findByIdAndTenantIdAndVendorId(id, vendorDto.getTenantId(), vendorId);
        existing.setActive(false);
        vendorDataPackRepository.save(existing);
    }

    private VendorDataPack findActiveById(String id) {
        return vendorDataPackRepository.findByIdAndActiveTrue(Long.parseLong(id))
                .orElseThrow(() -> new ResourceNotFoundException("VendorDataPack not found with id: " + id));
    }

    private VendorDataPack findByIdAndTenantIdAndVendorId(Long id, Long tenantId, Long vendorId) {
        return vendorDataPackRepository.findByIdAndTenantIdAndVendorIdAndActiveTrue(id, tenantId, vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("VendorDataPack not found with id: " + id + " for tenant: " + tenantId + " and vendor: " + vendorId));
    }

    public Optional<VendorAccess> getVendorAccessForAuthenticatedUser(Long tenantId) {
        try {
            VendorDto vendor = vendorService.getAuthenticatedVendor();
            return Optional.of(getVendorAccess(tenantId, vendor.getId()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public VendorAccess getVendorAccess(Long tenantId, Long vendorId) {
        List<VendorDataPack> vendorPacks = vendorDataPackRepository.findAllByTenantIdAndVendorIdAndActiveTrue(tenantId, vendorId);
        if (vendorPacks.isEmpty()) return new VendorAccess(List.of(), List.of(), false, tenantId);

        Set<Long> packIds = vendorPacks.stream().map(VendorDataPack::getLeadDataPackId).collect(Collectors.toSet());

        Set<Long> industryIds = leadDataPackService.findAllByIdIn(packIds).stream()
                .filter(p -> !CollectionUtils.isEmpty(p.getIndustryIds()))
                .flatMap(p -> p.getIndustryIds().stream())
                .collect(Collectors.toSet());

        List<Industry> industries = industryService.getActiveIndustriesByIds(industryIds);

        List<String> industryNames = industries.stream().map(Industry::getName).distinct().toList();

        List<String> allSegments = new ArrayList<>();
        boolean vendorNullAccess = false;

        for (Industry i : industries) {
            if (CollectionUtils.isEmpty(i.getSegments())) {
                vendorNullAccess = true;
                continue;
            }

            for (String s : i.getSegments()) {
                if (s == null) {
                    vendorNullAccess = true;
                } else {
                    allSegments.add(s);
                }
            }
        }

        return new VendorAccess(industryNames, allSegments, vendorNullAccess, tenantId);
    }
}

package ai.leadplus.application.tenantleadfilters;

import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.domain.tenantleadfilters.TenantLeadFilter;
import ai.leadplus.domain.tenantleadfilters.TenantLeadFilterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TenantLeadFilterService {

    private final TenantLeadFilterRepository tenantLeadFilterRepository;

    public TenantLeadFilterDto createTenantLeadFilter(TenantLeadFilterDto dto) {
        dto.setActive(true);
        TenantLeadFilter saved = tenantLeadFilterRepository.save(dto.toEntity());
        return TenantLeadFilterDto.fromEntity(saved);
    }

    public TenantLeadFilterDto getTenantLeadFilterById(Long id, Long tenantId) {
        return findByIdAndTenantId(id, tenantId);
    }

    public List<TenantLeadFilterDto> getTenantLeadFilterByTenantId(Long tenantId) {
        return tenantLeadFilterRepository.findByTenantIdAndActiveTrue(tenantId)
                .stream()
                .map(TenantLeadFilterDto::fromEntity)
                .collect(Collectors.toList());
    }

    public TenantLeadFilterDto updateTenantLeadFilter(Long id, TenantLeadFilterDto dto) {
        TenantLeadFilterDto existing = findByIdAndTenantId(id, dto.getTenantId());

        TenantLeadFilter updated = dto.toEntity();
        updated.setId(existing.getId());
        updated.setTenantId(existing.getTenantId());
        updated.setActive(existing.isActive());
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setCreatedBy(existing.getCreatedBy());
        updated.setActive(true);

        return TenantLeadFilterDto.fromEntity(tenantLeadFilterRepository.save(updated));
    }

    public void deleteTenantLeadFilter(Long id, Long tenantId) {
        TenantLeadFilterDto existing = findByIdAndTenantId(id, tenantId);
        existing.setActive(false);
        tenantLeadFilterRepository.save(existing.toEntity());
    }

    private TenantLeadFilterDto findByIdAndTenantId(Long id, Long tenantId){
        TenantLeadFilter tenantLeadFilter = tenantLeadFilterRepository.findByIdAndTenantIdAndActiveTrue(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "TenantLeadFilter not found with id: " + id + " for tenant: " + tenantId));
        return TenantLeadFilterDto.fromEntity(tenantLeadFilter);
    }
}
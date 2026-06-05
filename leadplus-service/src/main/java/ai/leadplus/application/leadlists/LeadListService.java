package ai.leadplus.application.leadlists;

import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.leadcompany.LeadCompanyService;
import ai.leadplus.application.leadcompany.LeadCompanyWithContactCountDto;
import ai.leadplus.application.leadcontact.LeadContactService;
import ai.leadplus.application.leads.CompanyLeadSearchService;
import ai.leadplus.application.leads.ContactLeadSearchService;
import ai.leadplus.application.leads.LeadDto;
import ai.leadplus.application.users.UserDto;
import ai.leadplus.application.users.UserService;
import ai.leadplus.domain.common.LeadType;
import ai.leadplus.domain.leadlists.LeadList;
import ai.leadplus.domain.leadlists.LeadListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeadListService {

    private final LeadListRepository leadListRepository;
    private final LeadCompanyService leadCompanyService;
    private final LeadContactService leadContactService;
    private final LeadListSearchService leadListSearchService;
    private final UserService userService;
    private final ContactLeadSearchService contactLeadSearchService;
    private final CompanyLeadSearchService companyLeadSearchService;

    public LeadListDto createLeadList(LeadListDto dto, Long tenantId, Long workspaceId){
        validateSourceIds(dto.getSourceIds(), dto.getType());

        dto.setTenantId(tenantId);
        dto.setWorkspaceId(workspaceId);
        dto.setActive(true);
        LeadList saved = leadListRepository.save(dto.toEntity());
        return LeadListDto.fromEntity(saved);
    }

    public LeadListDto getById(Long id, Long tenantId, Long workspaceId) {
        LeadList leadList = findById(id, tenantId, workspaceId);
        return LeadListDto.fromEntity(leadList);
    }

    public Page<LeadListSearchDto> searchLeadLists(String tenantId,
                                                   String workspaceId,
                                                   String searchText,
                                                   LeadType type,
                                                   Pageable pageable) {

        Page<LeadList> leadLists =
                leadListSearchService.searchLeadLists(tenantId, workspaceId, searchText, type, pageable);

        List<Long> userIds = leadLists.getContent()
                .stream()
                .map(LeadList::getCreatedBy)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<UserDto> userDtos = userService.getUsersByIds(userIds);

        Map<Long, String> userMap = userDtos.stream()
                .collect(Collectors.toMap(UserDto::getId, UserDto::getName));

        List<LeadListSearchDto> content = leadLists.getContent()
                .stream()
                .map(leadList -> {

                    String username = userMap.getOrDefault(leadList.getCreatedBy(), null);

                    int sourceCount = leadList.getSourceIds() != null
                            ? leadList.getSourceIds().size()
                            : 0;

                    return LeadListSearchDto.fromEntity(
                            leadList,
                            username,
                            sourceCount
                    );
                })
                .toList();

        return new PageImpl<>(content, pageable, leadLists.getTotalElements());
    }

    public LeadListDto updateLeadList(LeadListDto dto, Long id, Long tenantId, Long workspaceId){
        LeadList existing = findById(id, tenantId, workspaceId);
        validateSourceIds(dto.getSourceIds(), dto.getType());

        existing.setName(dto.getName());
        existing.setType(dto.getType());
        existing.setSourceIds(dto.getSourceIds());
        LeadList saved = leadListRepository.save(existing);
        return LeadListDto.fromEntity(saved);
    }

    public void deleteLeadList(Long id, Long tenantId, Long workspaceId) {
        LeadList existing = findById(id, tenantId, workspaceId);
        existing.setActive(false);
        leadListRepository.save(existing);
    }

    public Page<LeadDto> getLeadContactsByListId(Long tenantId, Long workspaceId, Long listId, Pageable pageable) {
        LeadList leadList = findById(listId, tenantId, workspaceId);
        if (!LeadType.LEAD_CONTACT.equals(leadList.getType())) {
            throw new BadRequestException("Lead list is not of type LEAD_CONTACT");
        }
        return contactLeadSearchService.searchLeadsByIds(tenantId, leadList.getSourceIds(), pageable);
    }

    public Page<LeadCompanyWithContactCountDto> getLeadCompaniesByListId(Long tenantId, Long workspaceId, Long listId, Pageable pageable) {
        LeadList leadList = findById(listId, tenantId, workspaceId);
        if (!LeadType.LEAD_COMPANY.equals(leadList.getType())) {
            throw new BadRequestException("Lead list is not of type LEAD_COMPANY");
        }
        return companyLeadSearchService.searchLeadCompaniesByIds(leadList.getSourceIds(), null, null, pageable);
    }

    private LeadList findById(Long id, Long tenantId, Long workspaceId) {
        return leadListRepository.findByIdAndTenantIdAndWorkspaceIdAndActiveTrue(id, tenantId, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Lead List not found with id: " + id));
    }

    private void validateSourceIds(List<Long> sourceIds, LeadType type) {
        if(LeadType.LEAD_COMPANY.equals(type)){
            leadCompanyService.validateCompanyIds(sourceIds);
        }else if(LeadType.LEAD_CONTACT.equals(type)){
            leadContactService.validateContactIds(sourceIds);
        }
    }
}

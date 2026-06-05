package ai.leadplus.application.leadlists;

import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.leadcompany.LeadCompanyService;
import ai.leadplus.application.users.UserDto;
import ai.leadplus.application.users.UserService;
import ai.leadplus.domain.common.LeadType;
import ai.leadplus.domain.leadlists.LeadList;
import ai.leadplus.domain.leadlists.LeadListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class LeadListServiceTest {

    @Mock
    private LeadListRepository repository;

    @Mock
    private LeadCompanyService leadCompanyService;

    @Mock
    private LeadListSearchService searchService;

    @Mock
    private UserService userService;

    @InjectMocks
    private LeadListService service;

    private LeadList leadList;

    @BeforeEach
    void setUp() {
        leadList = LeadList.builder()
                .id(1L)
                .tenantId(1L)
                .workspaceId(2L)
                .name("Test List")
                .type(LeadType.LEAD_COMPANY)
                .sourceIds(List.of(1L, 2L))
                .active(true)
                .createdBy(1L)
                .build();
    }

    @Test
    void createLeadList_shouldValidateAndSave() {
        LeadListDto dto = LeadListDto.fromEntity(leadList);

        when(repository.save(any())).thenReturn(leadList);

        LeadListDto result = service.createLeadList(dto, 1L, 2L);

        verify(leadCompanyService).validateCompanyIds(dto.getSourceIds());
        verify(repository).save(any());

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void getById_shouldReturnDto() {
        when(repository.findByIdAndTenantIdAndWorkspaceIdAndActiveTrue(
                1L, 1L, 2L))
                .thenReturn(Optional.of(leadList));

        LeadListDto result = service.getById(1L, 1L, 2L);

        assertThat(result.getName()).isEqualTo("Test List");
    }

    @Test
    void getById_shouldThrowIfNotFound() {
        when(repository.findByIdAndTenantIdAndWorkspaceIdAndActiveTrue(
                any(), any(), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.getById(1L, 1L, 2L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateLeadList_shouldUpdateFields() {
        LeadListDto updateDto = LeadListDto.builder()
                .name("Updated")
                .type(LeadType.LEAD_COMPANY)
                .sourceIds(List.of(3L))
                .build();

        when(repository.findByIdAndTenantIdAndWorkspaceIdAndActiveTrue(
                any(), any(), any()))
                .thenReturn(Optional.of(leadList));

        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LeadListDto result = service.updateLeadList(updateDto, 1L, 1L, 2L);

        assertThat(result.getName()).isEqualTo("Updated");
        assertThat(result.getSourceIds()).containsExactly(3L);
    }

    @Test
    void deleteLeadList_shouldSetInactive() {
        when(repository.findByIdAndTenantIdAndWorkspaceIdAndActiveTrue(
                any(), any(), any()))
                .thenReturn(Optional.of(leadList));

        service.deleteLeadList(1L, 1L, 2L);

        assertThat(leadList.isActive()).isFalse();
        verify(repository).save(leadList);
    }

    @Test
    void searchLeadLists_shouldMapToSearchDto() {
        Page<LeadList> page = new PageImpl<>(List.of(leadList));

        when(searchService.searchLeadLists(
                any(), any(), any(), any(), any()))
                .thenReturn(page);

        when(userService.getUsersByIds(List.of(1L)))
                .thenReturn(List.of(
                        UserDto.builder()
                                .id(1L)
                                .name("John Doe")
                                .build()
                ));

        Page<LeadListSearchDto> result = service.searchLeadLists(
                "1",
                "2",
                null,
                null,
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getUsername())
                .isEqualTo("John Doe");
        assertThat(result.getContent().getFirst().getSourceCount())
                .isEqualTo(2);
    }
}

package ai.leadplus.application.leadsearchhistories;

import ai.leadplus.domain.leadcompanies.EmployeeRange;
import ai.leadplus.domain.leadsearchhistories.LeadSearchHistory;
import ai.leadplus.domain.leadsearchhistories.LeadSearchHistoryRepository;
import ai.leadplus.domain.common.LeadType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadSearchHistoryServiceTest {

    @Mock
    private LeadSearchHistoryRepository repository;

    @InjectMocks
    private LeadSearchHistoryService service;

    private LeadSearchHistoryDto dto;
    private LeadSearchHistory entity;

    @BeforeEach
    void setUp() {
        dto = LeadSearchHistoryDto.builder()
                .id(1L)
                .userId(1L)
                .type(LeadType.LEAD_COMPANY)
                .cities(List.of("New York"))
                .industries(List.of("Tech"))
                .employeeRanges(List.of(EmployeeRange.RANGE_0_500))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        entity = dto.toEntity();
    }

    @Test
    void createLeadSearchHistory_shouldSaveAndReturnDto() {
        when(repository.save(any(LeadSearchHistory.class))).thenReturn(entity);
        LeadSearchHistoryDto result = service.createLeadSearchHistory(dto);
        verify(repository, times(1)).save(any(LeadSearchHistory.class));

        assertThat(result.getUserId()).isEqualTo(dto.getUserId());
        assertThat(result.getType()).isEqualTo(dto.getType());
        assertThat(result.getCities()).containsExactly("New York");
    }

    @Test
    void getLeadSearchHistoryByUserId_shouldReturnMappedDtos() {
        when(repository.findByUserIdAndType(1L, LeadType.LEAD_COMPANY))
                .thenReturn(List.of(entity));
        List<LeadSearchHistoryDto> result =
                service.getLeadSearchHistoryByUserId(1L, LeadType.LEAD_COMPANY);
        verify(repository, times(1))
                .findByUserIdAndType(1L, LeadType.LEAD_COMPANY);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUserId()).isEqualTo(1L);
        assertThat(result.getFirst().getType()).isEqualTo(LeadType.LEAD_COMPANY);
    }

    @Test
    void getLeadSearchHistoryByUserId_shouldReturnEmptyListWhenNoResults() {
        when(repository.findByUserIdAndType(eq(0L), any())).thenReturn(List.of());
        List<LeadSearchHistoryDto> result =
                service.getLeadSearchHistoryByUserId(0L, LeadType.LEAD_CONTACT);
        assertThat(result).isEmpty();
    }
}

package ai.leadplus.application.leadlists;

import ai.leadplus.domain.common.LeadType;
import ai.leadplus.domain.leadlists.LeadList;
import ai.leadplus.domain.leadlists.LeadListRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadListSearchServiceTest {

    @Mock
    private LeadListRepository leadListRepository;

    @InjectMocks
    private LeadListSearchService service;

    @Test
    void searchLeadLists_shouldReturnPage() {

        LeadList lead = LeadList.builder()
                .id(1L)
                .name("Test")
                .active(true)
                .build();

        Pageable pageable = PageRequest.of(0,10);
        Page<LeadList> page = new PageImpl<>(List.of(lead), pageable, 1L);

        when(leadListRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);

        Page<LeadList> result =
                service.searchLeadLists("1","1","Test",
                        LeadType.LEAD_COMPANY,pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);

        verify(leadListRepository).findAll(any(Specification.class), eq(pageable));
    }
}

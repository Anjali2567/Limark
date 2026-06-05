package ai.leadplus.application.leads;

import ai.leadplus.application.datapacks.DataPackGate;
import ai.leadplus.domain.leadcompanies.LeadCompanyRepository;
import ai.leadplus.domain.leadcontacts.LeadContactRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LeadServiceTest {
    private LeadContactRepository leadContactRepository;
    private LeadCompanyRepository leadCompanyRepository;
    private LeadService leadService;



    @BeforeEach
    void setUp() {
        leadContactRepository = mock(LeadContactRepository.class);
        leadCompanyRepository = mock(LeadCompanyRepository.class);
        DataPackGate dataPackGate = new DataPackGate();
        EntityManager entityManager = mock(EntityManager.class);
        leadService = new LeadService(
                leadContactRepository,
                leadCompanyRepository,
                dataPackGate,
                entityManager
        );
    }

    @Test
    void getStatistics_returnsCorrectCounts() {
        when(leadContactRepository.count(any(Specification.class))).thenReturn(10L);
        when(leadCompanyRepository.count(any(Specification.class))).thenReturn(5L);
        
        LeadStatisticsDto stats = leadService.getStatistics();
        
        assertThat(stats.getTotalContacts()).isEqualTo(10L);
        assertThat(stats.getTotalCompanies()).isEqualTo(5L);
        assertThat(stats.getTotalEmails()).isEqualTo(10L);
        assertThat(stats.getTotalPhoneNumbers()).isEqualTo(5L);
    }

    @Test
    void getStatistics_returnsZerosWhenNoData() {
        when(leadContactRepository.count(any(Specification.class))).thenReturn(0L, 0L);
        when(leadCompanyRepository.count(any(Specification.class))).thenReturn(0L, 0L);
        
        LeadStatisticsDto stats = leadService.getStatistics();
        
        assertThat(stats.getTotalContacts()).isZero();
        assertThat(stats.getTotalCompanies()).isZero();
        assertThat(stats.getTotalEmails()).isZero();
        assertThat(stats.getTotalPhoneNumbers()).isZero();
    }
}

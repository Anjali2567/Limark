package ai.leadplus.application.tenantactivity;

import ai.leadplus.application.exception.AnalyticsException;
import com.google.analytics.data.v1beta.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private BetaAnalyticsDataClient analyticsClient;

    private AnalyticsService analyticsService;

    @BeforeEach
    void setup() {
        analyticsService = new AnalyticsService(java.util.Optional.of(analyticsClient));
        ReflectionTestUtils.setField(analyticsService, "propertyId", "12345");
    }

    @Test
    void shouldFetchTenantActivitySuccessfully_withoutTenantFilter() {
        RunReportResponse mockResponse = RunReportResponse.newBuilder().build();

        when(analyticsClient.runReport(any(RunReportRequest.class)))
                .thenReturn(mockResponse);

        RunReportResponse response = analyticsService
                .fetchTenantActivity("7daysAgo", "today", null);

        assertNotNull(response);
        verify(analyticsClient, times(1)).runReport(any(RunReportRequest.class));
    }

    @Test
    void shouldApplyTenantFilter_whenTenantIdProvided() {
        RunReportResponse mockResponse = RunReportResponse.newBuilder().build();

        ArgumentCaptor<RunReportRequest> captor =
                ArgumentCaptor.forClass(RunReportRequest.class);

        when(analyticsClient.runReport(any()))
                .thenReturn(mockResponse);

        analyticsService.fetchTenantActivity("7daysAgo", "today", "tenant_1");

        verify(analyticsClient).runReport(captor.capture());

        RunReportRequest request = captor.getValue();

        assertTrue(request.hasDimensionFilter());
        assertEquals("properties/12345", request.getProperty());
    }

    @Test
    void shouldThrowAnalyticsException_whenClientFails() {
        when(analyticsClient.runReport(any()))
                .thenThrow(new RuntimeException("GA4 error"));

        AnalyticsException exception = assertThrows(
                AnalyticsException.class,
                () -> analyticsService.fetchTenantActivity("7daysAgo", "today", "tenant_1")
        );

        assertTrue(exception.getMessage().contains("Error fetching tenant activity"));
    }
}

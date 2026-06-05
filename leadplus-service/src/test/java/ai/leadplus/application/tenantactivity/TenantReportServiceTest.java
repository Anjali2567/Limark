package ai.leadplus.application.tenantactivity;

import com.google.analytics.data.v1beta.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantReportServiceTest {

    @Mock
    private AnalyticsService analyticsService;

    private TenantReportService tenantReportService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        tenantReportService = new TenantReportService(java.util.Optional.of(analyticsService));
    }

    @Test
    void shouldMapAndSortTenantActivityReport() {
        Row row1 = buildRow("1", "10", "7200", "5", "0.5"); // 2h
        Row row2 = buildRow("2", "5", "3600", "2", "0.2");  // 1h

        RunReportResponse response = RunReportResponse.newBuilder()
                .addRows(row2) // Add row2 first to test sorting
                .addRows(row1) // Add row1 second
                .build();

        when(analyticsService.fetchTenantActivity(any(), any(), any()))
                .thenReturn(response);

        List<TenantActivityReport> result =
                tenantReportService.getTenantActivityReport("7daysAgo", "today", null);

        assertThat(result).hasSize(2);

        assertThat(result.get(0).getTenantId()).isEqualTo(1L);
        assertThat(result.get(0).getActiveHours()).isEqualTo(2.0);

        assertThat(result.get(1).getTenantId()).isEqualTo(2L);
        assertThat(result.get(1).getActiveHours()).isEqualTo(1.0);
    }

    @Test
    void shouldSetCorrectEngagementLabel() {
        Row activeRow = buildRow("1", "10", "3600", "5", "0.6");
        Row idleRow   = buildRow("2", "10", "3600", "2", "0.2");

        RunReportResponse response = RunReportResponse.newBuilder()
                .addRows(activeRow)
                .addRows(idleRow)
                .build();

        when(analyticsService.fetchTenantActivity(any(), any(), any()))
                .thenReturn(response);

        List<TenantActivityReport> result =
                tenantReportService.getTenantActivityReport("7daysAgo", "today", null);

        assertThat(result.get(0).getEngagementLabel())
                .isEqualTo("Actively using");

        assertThat(result.get(1).getEngagementLabel())
                .isEqualTo("Mostly idle");
    }

    @Test
    void shouldHandleInvalidNumbersGracefully() {
        Row row = buildRow("1", "abc", "xyz", "NaN", "invalid");

        RunReportResponse response = RunReportResponse.newBuilder()
                .addRows(row)
                .build();

        when(analyticsService.fetchTenantActivity(any(), any(), any()))
                .thenReturn(response);

        List<TenantActivityReport> result =
                tenantReportService.getTenantActivityReport("7daysAgo", "today", null);

        TenantActivityReport report = result.getFirst();

        assertThat(report.getSessionCount()).isEqualTo(0L);
        assertThat(report.getActiveHours()).isEqualTo(0.0);
        assertThat(report.getEngagementRate()).isEqualTo(0.0);
    }

    private Row buildRow(String tenantId,
                         String sessions,
                         String engagementDuration,
                         String engagedSessions,
                         String engagementRate) {

        return Row.newBuilder()
                .addDimensionValues(DimensionValue.newBuilder().setValue(tenantId))
                .addMetricValues(MetricValue.newBuilder().setValue(sessions))
                .addMetricValues(MetricValue.newBuilder().setValue(engagementDuration))
                .addMetricValues(MetricValue.newBuilder().setValue(engagedSessions))
                .addMetricValues(MetricValue.newBuilder().setValue(engagementRate))
                .build();
    }
}

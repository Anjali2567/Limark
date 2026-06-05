package ai.leadplus.application.tenantactivity;

import com.google.analytics.data.v1beta.Row;
import com.google.analytics.data.v1beta.RunReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TenantReportService {

    private final Optional<AnalyticsService> analyticsService;

    private static final double ENGAGEMENT_THRESHOLD = 0.4;

    public List<TenantActivityReport> getTenantActivityReport(
            String startDate, String endDate, String tenantId) {

        if (!analyticsService.isPresent()) {
            return List.of();
        }

        RunReportResponse response =
                analyticsService.get().fetchTenantActivity(startDate, endDate, tenantId);

        return response.getRowsList().stream()
                .map(this::mapHistoricalRow)
                .sorted(Comparator.comparingDouble(TenantActivityReport::getActiveHours).reversed())
                .collect(Collectors.toList());
    }

    private TenantActivityReport mapHistoricalRow(Row row) {
        String tenantId       = row.getDimensionValues(0).getValue();
        long sessions         = parseLong(row.getMetricValues(0).getValue());
        double engagementSecs = parseDouble(row.getMetricValues(1).getValue());
        long engagedSessions  = parseLong(row.getMetricValues(2).getValue());
        double engagementRate = parseDouble(row.getMetricValues(3).getValue());

        double activeHours = engagementSecs / 3600.0;

        return TenantActivityReport.builder()
                .tenantId(parseLong(tenantId))
                .sessionCount(sessions)
                .activeHours(Math.round(activeHours * 10.0) / 10.0)
                .engagedSessionCount(engagedSessions)
                .engagementRate(Math.round(engagementRate * 1000.0) / 10.0)
                .engagementLabel(engagementRate >= ENGAGEMENT_THRESHOLD ? "Actively using" : "Mostly idle")
                .build();
    }

    private long parseLong(String v) {
        try { return Long.parseLong(v); } catch (Exception e) { return 0L; }
    }

    private double parseDouble(String v) {
        try { return Double.parseDouble(v); } catch (Exception e) { return 0.0; }
    }
}
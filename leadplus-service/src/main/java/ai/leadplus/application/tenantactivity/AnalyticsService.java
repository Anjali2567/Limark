package ai.leadplus.application.tenantactivity;

import ai.leadplus.application.exception.AnalyticsException;
import com.google.analytics.data.v1beta.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.google.analytics.enabled", havingValue = "true", matchIfMissing = false)
public class AnalyticsService {

    @Value("${google.analytics.property-id}")
    private String propertyId;

    private final Optional<BetaAnalyticsDataClient> analyticsClient;

    public RunReportResponse fetchTenantActivity(String startDate, String endDate, String tenantId) {
        try {
            RunReportRequest.Builder requestBuilder = RunReportRequest.newBuilder()
                    .setProperty("properties/" + propertyId)
                    .addDateRanges(DateRange.newBuilder()
                            .setStartDate(startDate)
                            .setEndDate(endDate))
                    .addDimensions(Dimension.newBuilder().setName("customUser:tenant_id"))
                    .addMetrics(Metric.newBuilder().setName("sessions"))
                    .addMetrics(Metric.newBuilder().setName("userEngagementDuration"))
                    .addMetrics(Metric.newBuilder().setName("engagedSessions"))
                    .addMetrics(Metric.newBuilder().setName("engagementRate"));

            if (StringUtils.isNotBlank(tenantId)) {
                requestBuilder.setDimensionFilter(FilterExpression.newBuilder()
                        .setFilter(Filter.newBuilder()
                                .setFieldName("customUser:tenant_id")
                                .setStringFilter(Filter.StringFilter.newBuilder()
                                        .setMatchType(Filter.StringFilter.MatchType.EXACT)
                                        .setValue(tenantId)
                                        .build())
                                .build())
                        .build());
            }

            return analyticsClient.orElseThrow(() -> new AnalyticsException("Analytics client not available", null))
                    .runReport(requestBuilder.build());

        } catch (Exception e) {
            log.error("Error fetching tenant activity for tenantId={}, startDate={}, endDate={}",
                    tenantId, startDate, endDate, e);
            throw new AnalyticsException("Error fetching tenant activity. " + e.getMessage(), e);
        }
    }
}
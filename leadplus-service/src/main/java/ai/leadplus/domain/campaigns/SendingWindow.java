package ai.leadplus.domain.campaigns;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendingWindow {
    private LocalTime windowStart;
    private LocalTime windowEnd;
        private List<DayOfWeek> sendingDays;
}

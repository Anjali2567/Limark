package ai.leadplus.application.campaigns;

import ai.leadplus.application.campaigncontacts.CampaignContactInfoDto;
import ai.leadplus.application.timezonemappings.ContactTimezoneResolver;
import ai.leadplus.domain.campaigns.SendingWindow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * All times tested against America/New_York (EST = UTC-5, no DST — January dates used).
 * Window: 09:00–17:00, Mon–Fri.
 *
 * Key UTC anchors:
 *   09:00 ET = 14:00 UTC
 *   17:00 ET = 22:00 UTC
 */
@ExtendWith(MockitoExtension.class)
class SendingWindowServiceTest {

    @Mock
    private ContactTimezoneResolver timezoneResolver;

    @InjectMocks
    private SendingWindowService service;

    private static final String NEW_YORK = "America/New_York";
    private static final LocalTime WINDOW_START = LocalTime.of(9, 0);
    private static final LocalTime WINDOW_END = LocalTime.of(17, 0);
    private static final List<DayOfWeek> WEEKDAYS = List.of(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);

    // Wednesday Jan 17, 2024
    private static final LocalDateTime WED_BEFORE_START  = LocalDateTime.of(2024, 1, 17, 13, 50); // 08:50 ET
    private static final LocalDateTime WED_INSIDE        = LocalDateTime.of(2024, 1, 17, 15,  0); // 10:00 ET
    private static final LocalDateTime WED_AT_END        = LocalDateTime.of(2024, 1, 17, 22,  0); // 17:00 ET (boundary)
    private static final LocalDateTime WED_AFTER_END     = LocalDateTime.of(2024, 1, 17, 22,  1); // 17:01 ET
    // Friday Jan 19, 2024
    private static final LocalDateTime FRI_AFTER_END     = LocalDateTime.of(2024, 1, 19, 22,  1); // 17:01 ET Friday
    // Saturday Jan 20, 2024
    private static final LocalDateTime SAT_INSIDE_HOURS  = LocalDateTime.of(2024, 1, 20, 15,  0); // 10:00 ET Saturday

    private CampaignDto campaign;
    private CampaignContactInfoDto contact;

    @BeforeEach
    void setUp() {
        contact = CampaignContactInfoDto.builder()
                .locationState("new york")
                .locationCountry("united states")
                .build();
    }

    // --- No window ---

    @Test
    void noWindowConfigured_returnsBaseUtcUnchanged() {
        campaign = campaignWith(null);

        LocalDateTime result = service.nextValidSendTime(WED_INSIDE, contact, campaign);

        assertThat(result).isEqualTo(WED_INSIDE);
    }

    // --- Unknown timezone ---

    @Test
    void unknownTimezone_returnsBaseUtcUnchanged() {
        campaign = campaignWith(weekdayWindow());
        when(timezoneResolver.resolve("new york", "united states")).thenReturn(null);

        LocalDateTime result = service.nextValidSendTime(WED_INSIDE, contact, campaign);

        assertThat(result).isEqualTo(WED_INSIDE);
    }

    // --- Inside window ---

    @Test
    void insideWindow_returnsBaseUtcUnchanged() {
        campaign = campaignWith(weekdayWindow());
        when(timezoneResolver.resolve("new york", "united states")).thenReturn(NEW_YORK);

        LocalDateTime result = service.nextValidSendTime(WED_INSIDE, contact, campaign);

        assertThat(result).isEqualTo(WED_INSIDE);
    }

    // --- Before window start ---

    @Test
    void beforeWindowStart_snapsToWindowStartSameDay() {
        campaign = campaignWith(weekdayWindow());
        when(timezoneResolver.resolve("new york", "united states")).thenReturn(NEW_YORK);

        LocalDateTime result = service.nextValidSendTime(WED_BEFORE_START, contact, campaign);

        // 09:00 ET Wednesday = 14:00 UTC
        assertThat(result).isEqualTo(LocalDateTime.of(2024, 1, 17, 14, 0));
    }

    // --- After window end ---

    @Test
    void afterWindowEnd_snapsToWindowStartNextDay() {
        campaign = campaignWith(weekdayWindow());
        when(timezoneResolver.resolve("new york", "united states")).thenReturn(NEW_YORK);

        LocalDateTime result = service.nextValidSendTime(WED_AFTER_END, contact, campaign);

        // 09:00 ET Thursday Jan 18 = 14:00 UTC
        assertThat(result).isEqualTo(LocalDateTime.of(2024, 1, 18, 14, 0));
    }

    // --- At end boundary (exactly 17:00 ET) ---

    @Test
    void atWindowEndBoundary_snapsToWindowStartNextDay() {
        campaign = campaignWith(weekdayWindow());
        when(timezoneResolver.resolve("new york", "united states")).thenReturn(NEW_YORK);

        LocalDateTime result = service.nextValidSendTime(WED_AT_END, contact, campaign);

        // 09:00 ET Thursday Jan 18 = 14:00 UTC
        assertThat(result).isEqualTo(LocalDateTime.of(2024, 1, 18, 14, 0));
    }

    // --- Day not in sendingDays (Saturday) ---

    @Test
    void dayNotAllowed_advancesToNextValidDay() {
        campaign = campaignWith(weekdayWindow());
        when(timezoneResolver.resolve("new york", "united states")).thenReturn(NEW_YORK);

        LocalDateTime result = service.nextValidSendTime(SAT_INSIDE_HOURS, contact, campaign);

        // Saturday → advance to Sunday → not allowed → advance to Monday Jan 22
        // 09:00 ET Monday Jan 22 = 14:00 UTC
        assertThat(result).isEqualTo(LocalDateTime.of(2024, 1, 22, 14, 0));
    }

    // --- Friday after close → Monday ---

    @Test
    void fridayAfterClose_snapsToMondayWindowStart() {
        campaign = campaignWith(weekdayWindow());
        when(timezoneResolver.resolve("new york", "united states")).thenReturn(NEW_YORK);

        LocalDateTime result = service.nextValidSendTime(FRI_AFTER_END, contact, campaign);

        // Advances Fri→Sat→Sun→Mon Jan 22
        // 09:00 ET Monday Jan 22 = 14:00 UTC
        assertThat(result).isEqualTo(LocalDateTime.of(2024, 1, 22, 14, 0));
    }

    // --- null sendingDays = all days allowed ---

    @Test
    void nullSendingDays_treatsAllDaysAsAllowed() {
        SendingWindow allDaysWindow = SendingWindow.builder()
                .windowStart(WINDOW_START)
                .windowEnd(WINDOW_END)
                .sendingDays(null)
                .build();
        campaign = campaignWith(allDaysWindow);
        when(timezoneResolver.resolve("new york", "united states")).thenReturn(NEW_YORK);

        // Saturday 10:00 ET (inside hours) → inside window since all days allowed
        LocalDateTime result = service.nextValidSendTime(SAT_INSIDE_HOURS, contact, campaign);

        assertThat(result).isEqualTo(SAT_INSIDE_HOURS);
    }

    // --- helpers ---

    private SendingWindow weekdayWindow() {
        return SendingWindow.builder()
                .windowStart(WINDOW_START)
                .windowEnd(WINDOW_END)
                .sendingDays(WEEKDAYS)
                .build();
    }

    private CampaignDto campaignWith(SendingWindow window) {
        return CampaignDto.builder()
                .sendingWindow(window)
                .build();
    }
}

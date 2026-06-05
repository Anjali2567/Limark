package ai.leadplus.application.campaigns;

import ai.leadplus.application.campaigncontacts.CampaignContactInfoDto;
import ai.leadplus.application.timezonemappings.ContactTimezoneResolver;
import ai.leadplus.domain.campaigns.SendingWindow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendingWindowService {

    private final ContactTimezoneResolver timezoneResolver;

    /**
     * Returns the next UTC time at which an email may be sent for this contact,
     * given the campaign's sending window.
     *
     * Returns baseUTC unchanged if:
     * - no window is configured on the campaign, or
     * - the contact's timezone cannot be resolved (never block on unknown timezone).
     */
    public LocalDateTime nextValidSendTime(LocalDateTime baseUtc, CampaignContactInfoDto contact, CampaignDto campaign) {
        SendingWindow window = campaign.getSendingWindow();
        if (window == null) {
            return baseUtc;
        }

        String timezone = timezoneResolver.resolve(contact.getLocationState(), contact.getLocationCountry());
        if (timezone == null) {
            return baseUtc;
        }

        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(timezone);
        } catch (Exception e) {
            log.warn("Invalid timezone '{}' resolved for contact '{}', skipping window check.", timezone, contact.getId());
            return baseUtc;
        }
        LocalDateTime contactLocalTime = baseUtc.atZone(ZoneOffset.UTC).withZoneSameInstant(zoneId).toLocalDateTime();

        LocalTime currentTime = contactLocalTime.toLocalTime();
        DayOfWeek currentDay = contactLocalTime.getDayOfWeek();
        LocalTime windowStart = window.getWindowStart();
        LocalTime windowEnd = window.getWindowEnd();
        if (windowStart == null || windowEnd == null) {
            return baseUtc;
        }
        List<DayOfWeek> allowedDays = (window.getSendingDays() == null || window.getSendingDays().isEmpty())
                ? List.of(DayOfWeek.values())
                : window.getSendingDays();

        // Inside window — return unchanged
        if (allowedDays.contains(currentDay)
                && !currentTime.isBefore(windowStart)
                && currentTime.isBefore(windowEnd)) {
            return baseUtc;
        }

        // Determine the target date for the next send
        LocalDate targetDate = contactLocalTime.toLocalDate();

        boolean sameDay = allowedDays.contains(currentDay) && currentTime.isBefore(windowStart);
        if (!sameDay) {
            targetDate = targetDate.plusDays(1);
        }

        // Advance to the nearest allowed day (at most 7 iterations)
        for (int i = 0; i < 7; i++) {
            if (allowedDays.contains(targetDate.getDayOfWeek())) {
                break;
            }
            targetDate = targetDate.plusDays(1);
        }

        return LocalDateTime.of(targetDate, windowStart)
                .atZone(zoneId)
                .withZoneSameInstant(ZoneOffset.UTC)
                .toLocalDateTime();
    }
}

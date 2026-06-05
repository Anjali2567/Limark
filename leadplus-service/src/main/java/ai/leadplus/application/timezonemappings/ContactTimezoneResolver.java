package ai.leadplus.application.timezonemappings;

import ai.leadplus.domain.timezonemappings.LocationType;
import ai.leadplus.domain.timezonemappings.TimezoneMapping;
import ai.leadplus.domain.timezonemappings.TimezoneMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ContactTimezoneResolver {

    private final TimezoneMappingRepository repository;

    /**
     * Resolves an IANA timezone string from a contact's location.
     * Tries state first, then falls back to country.
     * Returns null if neither resolves (caller should treat as unknown — never block).
     */
    public String resolve(String state, String country) {
        if (StringUtils.hasText(state)) {
            Optional<TimezoneMapping> match = repository.findByLocationTypeAndLocationKeyIgnoreCaseAndActiveTrue(
                    LocationType.STATE, state.trim());
            if (match.isPresent()) {
                return match.get().getTimezone();
            }
        }
        if (StringUtils.hasText(country)) {
            Optional<TimezoneMapping> match = repository.findByLocationTypeAndLocationKeyIgnoreCaseAndActiveTrue(
                    LocationType.COUNTRY, country.trim());
            if (match.isPresent()) {
                return match.get().getTimezone();
            }
        }
        return null;
    }
}

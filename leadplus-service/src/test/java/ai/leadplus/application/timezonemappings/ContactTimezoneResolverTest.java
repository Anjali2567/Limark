package ai.leadplus.application.timezonemappings;

import ai.leadplus.domain.timezonemappings.LocationType;
import ai.leadplus.domain.timezonemappings.TimezoneMapping;
import ai.leadplus.domain.timezonemappings.TimezoneMappingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactTimezoneResolverTest {

    @Mock
    private TimezoneMappingRepository repository;

    @InjectMocks
    private ContactTimezoneResolver resolver;

    @Test
    void resolve_stateFullName_returnsStateTimezone() {
        when(repository.findByLocationTypeAndLocationKeyIgnoreCaseAndActiveTrue(LocationType.STATE, "california"))
                .thenReturn(Optional.of(mapping(LocationType.STATE, "california", "America/Los_Angeles")));

        String result = resolver.resolve("california", "United States");

        assertThat(result).isEqualTo("America/Los_Angeles");
    }

    @Test
    void resolve_unknownStateAbbreviation_fallsBackToCountry() {
        when(repository.findByLocationTypeAndLocationKeyIgnoreCaseAndActiveTrue(LocationType.STATE, "NY"))
                .thenReturn(Optional.empty());
        when(repository.findByLocationTypeAndLocationKeyIgnoreCaseAndActiveTrue(LocationType.COUNTRY, "United States"))
                .thenReturn(Optional.of(mapping(LocationType.COUNTRY, "united states", "America/New_York")));

        String result = resolver.resolve("NY", "United States");

        assertThat(result).isEqualTo("America/New_York");
    }

    @Test
    void resolve_unknownStateFallsBackToCountry() {
        when(repository.findByLocationTypeAndLocationKeyIgnoreCaseAndActiveTrue(LocationType.STATE, "XYZ"))
                .thenReturn(Optional.empty());
        when(repository.findByLocationTypeAndLocationKeyIgnoreCaseAndActiveTrue(LocationType.COUNTRY, "United Kingdom"))
                .thenReturn(Optional.of(mapping(LocationType.COUNTRY, "united kingdom", "Europe/London")));

        String result = resolver.resolve("XYZ", "United Kingdom");

        assertThat(result).isEqualTo("Europe/London");
    }

    @Test
    void resolve_unknownStateAndCountry_returnsNull() {
        when(repository.findByLocationTypeAndLocationKeyIgnoreCaseAndActiveTrue(LocationType.STATE, "XYZ"))
                .thenReturn(Optional.empty());
        when(repository.findByLocationTypeAndLocationKeyIgnoreCaseAndActiveTrue(LocationType.COUNTRY, "Narnia"))
                .thenReturn(Optional.empty());

        String result = resolver.resolve("XYZ", "Narnia");

        assertThat(result).isNull();
    }

    @Test
    void resolve_nullState_skipsStateQueryAndFallsBackToCountry() {
        when(repository.findByLocationTypeAndLocationKeyIgnoreCaseAndActiveTrue(LocationType.COUNTRY, "Japan"))
                .thenReturn(Optional.of(mapping(LocationType.COUNTRY, "japan", "Asia/Tokyo")));

        String result = resolver.resolve(null, "Japan");

        assertThat(result).isEqualTo("Asia/Tokyo");
        verify(repository, never()).findByLocationTypeAndLocationKeyIgnoreCaseAndActiveTrue(eq(LocationType.STATE), any());
    }

    @Test
    void resolve_emptyStateAndNullCountry_returnsNull() {
        String result = resolver.resolve("  ", null);

        assertThat(result).isNull();
        verifyNoInteractions(repository);
    }

    private TimezoneMapping mapping(LocationType type, String key, String timezone) {
        return TimezoneMapping.builder()
                .locationType(type)
                .locationKey(key)
                .timezone(timezone)
                .active(true)
                .build();
    }
}

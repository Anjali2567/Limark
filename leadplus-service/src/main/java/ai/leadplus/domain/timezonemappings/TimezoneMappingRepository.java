package ai.leadplus.domain.timezonemappings;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TimezoneMappingRepository extends JpaRepository<TimezoneMapping, Long> {

    Optional<TimezoneMapping> findByLocationTypeAndLocationKeyIgnoreCaseAndActiveTrue(LocationType locationType, String locationKey);
}

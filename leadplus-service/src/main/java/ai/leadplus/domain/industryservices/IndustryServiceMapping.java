package ai.leadplus.domain.industryservices;

import jakarta.persistence.Entity;

import lombok.*;
import jakarta.persistence.Id;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class IndustryServiceMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long industryId;
    private Long serviceId;
}

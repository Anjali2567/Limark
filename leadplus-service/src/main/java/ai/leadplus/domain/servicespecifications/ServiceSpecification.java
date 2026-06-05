package ai.leadplus.domain.servicespecifications;

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
public class ServiceSpecification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long serviceId;
    private Long specificationId;
}

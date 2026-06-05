package ai.leadplus.domain.apollocompanydata;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

import ai.leadplus.domain.common.ApolloDataType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ApolloCompanyData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long leadCompanyId;
    @Enumerated(EnumType.STRING)
    private ApolloDataType type;
    private String data;
    private Long specificationId;
    private LocalDateTime fetchedAt;
}

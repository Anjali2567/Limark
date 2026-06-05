package ai.leadplus.domain.apollospecification;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Convert;
import ai.leadplus.domain.common.StringListConverter;
import jakarta.persistence.EntityListeners;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import jakarta.persistence.Id;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "apollo_specification")
public class ApolloSpecification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private boolean personTitleEnabled;
    @Convert(converter = StringListConverter.class)
    private List<String> personTitles;
    private boolean personSeniorityEnabled;
    @Convert(converter = StringListConverter.class)
    private List<String> personSeniorities;
    @CreatedDate
    private LocalDateTime createdAt;
    @CreatedBy
    private Long createdBy;
}

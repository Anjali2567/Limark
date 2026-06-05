package ai.leadplus.domain.leadcompanyevents;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class LeadCompanyEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long leadCompanyId;
    @Enumerated(EnumType.STRING)
    private EventType type;
    private String title;
    private String summary;
    private String url;
    private String source;
    private Integer sentiment;
    private LocalDateTime publishedAt;
    private LocalDateTime detectedAt;
    private String uniqueHash;
    private boolean active;
}


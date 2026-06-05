package ai.leadplus.application.apollocontactdata;

import ai.leadplus.domain.apollocontactdata.ApolloContactData;
import ai.leadplus.domain.common.ApolloDataType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApolloContactDataDto {

    private Long id;
    private Long leadContactId;
    private ApolloDataType type;
    private String data;
    private Long specificationId;
    private LocalDateTime fetchedAt;

    public static ApolloContactDataDto fromEntity(ApolloContactData apolloContactData) {
        return ApolloContactDataDto.builder()
                .id(apolloContactData.getId())
                .leadContactId(apolloContactData.getLeadContactId())
                .type(apolloContactData.getType())
                .data(apolloContactData.getData())
                .specificationId(apolloContactData.getSpecificationId())
                .fetchedAt(apolloContactData.getFetchedAt())
                .build();
    }

    public ApolloContactData toEntity() {
        return ApolloContactData.builder()
                .id(id)
                .leadContactId(leadContactId)
                .type(type)
                .data(data)
                .specificationId(specificationId)
                .fetchedAt(fetchedAt)
                .build();
    }
}

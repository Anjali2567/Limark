package ai.leadplus.application.leadcompany;

import ai.leadplus.application.apollocompanydata.ApolloCompanyDataDto;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ApolloDataFetchedEvent extends ApplicationEvent {

    private final LeadCompanyDto leadCompanyDto;
    private final ApolloCompanyDataDto apolloCompanyDataDto;

    public ApolloDataFetchedEvent(Object source, LeadCompanyDto leadCompanyDto, ApolloCompanyDataDto apolloCompanyDataDto) {
        super(source);
        this.leadCompanyDto = leadCompanyDto;
        this.apolloCompanyDataDto = apolloCompanyDataDto;
    }
}

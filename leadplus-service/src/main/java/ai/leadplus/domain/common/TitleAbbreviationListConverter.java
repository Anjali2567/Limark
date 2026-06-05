package ai.leadplus.domain.common;

import ai.leadplus.domain.leadcontactnormalizedtitle.TitleAbbreviation;
import com.fasterxml.jackson.databind.type.TypeFactory;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class TitleAbbreviationListConverter extends TypedJsonConverter<List<TitleAbbreviation>> {
    public TitleAbbreviationListConverter() {
        super(TypeFactory.defaultInstance().constructCollectionType(List.class, TitleAbbreviation.class));
    }
}

package ai.leadplus.domain.common;

import ai.leadplus.domain.quotations.QuotationItem;
import com.fasterxml.jackson.databind.type.TypeFactory;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class QuotationItemListConverter extends TypedJsonConverter<List<QuotationItem>> {
    public QuotationItemListConverter() {
        super(TypeFactory.defaultInstance().constructCollectionType(List.class, QuotationItem.class));
    }
}

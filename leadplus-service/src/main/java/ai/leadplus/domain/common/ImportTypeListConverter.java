package ai.leadplus.domain.common;

import ai.leadplus.domain.leadfileimport.ImportType;
import jakarta.persistence.Converter;

@Converter
public class ImportTypeListConverter extends EnumListConverter<ImportType> {

    public ImportTypeListConverter() {
        super(ImportType.class);
    }
}

package ai.leadplus.domain.common;

import ai.leadplus.domain.tenants.Module;
import jakarta.persistence.Converter;

@Converter
public class ModuleListConverter extends EnumListConverter<Module> {

    public ModuleListConverter() {
        super(Module.class);
    }
}

package ai.leadplus.application.common.utils;

public final class PromptTemplateContextInjector {

    private PromptTemplateContextInjector() {
    }

    public static String injectRequired(String promptTemplate, String placeholder, String value, String promptName) {
        if (!promptTemplate.contains(placeholder)) {
            throw new IllegalStateException(
                    "Missing required placeholder %s in %s prompt template.".formatted(placeholder, promptName)
            );
        }
        return promptTemplate.replace(placeholder, value);
    }
}

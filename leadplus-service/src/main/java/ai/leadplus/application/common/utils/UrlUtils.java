package ai.leadplus.application.common.utils;

import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;

public final class UrlUtils {

    private UrlUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String extractRootDomain(String url) {
        if (!StringUtils.hasText(url)) {
            return null;
        }

        try {
            URI uri = new URI(url);
            String host = uri.getHost();

            if (host == null) {
                return null;
            }

            String[] parts = host.split("\\.");

            // Need at least 2 parts for a valid domain
            if (parts.length < 2) {
                return host;
            }

            // Return last two parts (domain + TLD)
            return parts[parts.length - 2] + "." + parts[parts.length - 1];

        } catch (URISyntaxException e) {
            return null;
        }
    }
}

package ai.leadplus.application.common.utils;

import ai.leadplus.application.exception.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
public class FileReader {

    public static String readFileContentFromClasspath(String classpathLocation) {
        ClassPathResource resource = new ClassPathResource(classpathLocation);
        log.info("Reading prompt file from classpath: {}", resource.getPath());
        try (InputStream inputStream = resource.getInputStream()) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to read prompt file from classpath: {}", classpathLocation, e);
            throw new IllegalStateException("Could not read prompt file", e);
        }
    }

    public static byte[] convertUrlToBytes(String urlString) {
        if (urlString == null || urlString.trim().isEmpty()) {
            throw new IllegalArgumentException("URL string cannot be null or empty");
        }

        try {
            URI uri = new URI(urlString);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();

            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            } else {
                throw new ServiceUnavailableException("HTTP Error: " + response.statusCode() + " for URL: " + urlString);
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL: " + urlString, e);
        } catch (Exception e) {
            throw new ServiceUnavailableException("Failed to download from URL: " + urlString);
        }
    }

    public static String convertUrlToBase64(String urlString) {
        byte[] bytes = convertUrlToBytes(urlString);
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static String getFileExtension(String filename) {
        if (!StringUtils.hasText(filename)) return "";

        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot == filename.length() - 1) return "";

        return filename.substring(lastDot + 1);
    }
}

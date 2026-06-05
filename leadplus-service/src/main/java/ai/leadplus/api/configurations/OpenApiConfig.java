package ai.leadplus.api.configurations;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)

@OpenAPIDefinition(
        info = @Info(
                title = "LeadPlus Service",
                version = "1.0.0",
                description = "API Documentation for the LeadPlus Service",
                contact = @Contact(
                        name = "Support",
                        email = "support@limarktech.com",
                        url = "https://www.limarktech.com"
                ),
                license = @License(
                        name = "Limark Technologies",
                        url = "https://www.limarktech.com"
                )),
        security = {
                @SecurityRequirement(name = "Bearer Authentication")
        },
        servers = {
                @Server(url = "http://localhost:8080/api", description = "Local"),
                @Server(url = "https://dev.leadplus.ai/api", description = "Development"),
        }
)
public class OpenApiConfig {
}

package app.budgetmanager.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Budget Manager API",
                version = "v1",
                description = "API for users, wallets, categories, tags and expenses",
                contact = @Contact(name = "Budget Manager Team"),
                license = @License(name = "Internal")
        )
)
public class OpenApiConfig {
}

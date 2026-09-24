package app.budgetmanager.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.List;
import java.util.Map;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
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

    private static final Map<String, Object> PAGEABLE_EXAMPLE = Map.of(
            "page", 0,
            "size", Paging.DEFAULT_SIZE,
            "sort", List.of("id")
    );

    @Bean
    public OpenApiCustomizer pageableParameterCustomizer() {
        return openApi -> {
            fixPageableSchema(openApi);
            if (openApi.getPaths() == null) {
                return;
            }
            openApi.getPaths().values().forEach(pathItem ->
                    pathItem.readOperations().forEach(operation -> {
                        if (operation.getParameters() == null) {
                            return;
                        }
                        operation.getParameters().forEach(this::normalizePageableParameter);
                    })
            );
        };
    }

    @SuppressWarnings("rawtypes")
    private void fixPageableSchema(OpenAPI openApi) {
        if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
            return;
        }
        Schema pageable = openApi.getComponents().getSchemas().get("Pageable");
        if (pageable == null || pageable.getProperties() == null) {
            return;
        }
        setNumericExample(pageable, "page", 0);
        setNumericExample(pageable, "size", Paging.DEFAULT_SIZE);
        Schema sort = (Schema) pageable.getProperties().get("sort");
        if (sort != null) {
            sort.setExample(List.of("id"));
            if (sort.getItems() != null) {
                sort.getItems().setExample("id");
            }
        }
        pageable.setExample(PAGEABLE_EXAMPLE);
    }

    @SuppressWarnings("rawtypes")
    private void setNumericExample(Schema pageable, String property, int value) {
        Schema field = (Schema) pageable.getProperties().get(property);
        if (field != null) {
            field.setExample(value);
            field.setDefault(value);
        }
    }

    private void normalizePageableParameter(Parameter parameter) {
        if ("pageable".equals(parameter.getName())) {
            parameter.setRequired(false);
            parameter.setExample(PAGEABLE_EXAMPLE);
        }
        if ("page".equals(parameter.getName())) {
            parameter.setExample(0);
        }
        if ("size".equals(parameter.getName())) {
            parameter.setExample(Paging.DEFAULT_SIZE);
        }
        if ("sort".equals(parameter.getName())) {
            parameter.setExample("id");
            if (parameter.getSchema() != null) {
                parameter.getSchema().setExample("id");
                if (parameter.getSchema().getItems() != null) {
                    parameter.getSchema().getItems().setExample("id");
                }
            }
        }
    }
}

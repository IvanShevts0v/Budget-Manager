package app.budgetmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Ответ API для сущностей с полями {@code id} и {@code name} (категории, теги).
 */
@Schema(description = "Entity with id and name")
public class NamedResponseDto {

    @Schema(description = "Entity id", example = "1")
    private Long id;

    @Schema(description = "Entity name", example = "Food")
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

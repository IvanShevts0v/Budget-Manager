package app.budgetmanager.dto;

/**
 * Ответ API для сущностей с полями {@code id} и {@code name} (категории, теги).
 */
public class NamedResponseDto {

    private Long id;
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

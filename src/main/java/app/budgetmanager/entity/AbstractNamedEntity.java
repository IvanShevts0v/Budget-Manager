package app.budgetmanager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/**
 * Общие поля справочников с уникальным именем (категория, тег).
 */
@MappedSuperclass
public abstract class AbstractNamedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    /**
     * Конструктор без аргументов требуется JPA/Hibernate.
     */
    protected AbstractNamedEntity() {
        // required by JPA; fields initialized by Hibernate
    }

    protected AbstractNamedEntity(Long id, String name) {
        this.id = id;
        this.name = name;
    }

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

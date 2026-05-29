package app.budgetmanager.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class AbstractNamedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    protected AbstractNamedEntity() {
    }

    protected AbstractNamedEntity(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}

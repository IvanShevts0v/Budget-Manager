package app.budgetmanager.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "tags")
public class Tag extends AbstractNamedEntity {

    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    private Set<Expense> expenses = new HashSet<>();

    public Tag() {
    }

    public Tag(Long id, String name) {
        super(id, name);
    }
}

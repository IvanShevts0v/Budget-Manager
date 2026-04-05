package app.budgetmanager.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tags")
public class Tag extends AbstractNamedEntity {

    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    private Set<Expense> expenses = new HashSet<>();

    /**
     * Требуется JPA/Hibernate для создания экземпляра сущности (рефлексия, прокси).
     */
    public Tag() {
        /* intentionally empty */
    }

    public Tag(Long id, String name) {
        super(id, name);
    }

    public Set<Expense> getExpenses() {
        return expenses;
    }

    public void setExpenses(Set<Expense> expenses) {
        this.expenses = expenses;
    }
}

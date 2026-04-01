package app.budgetmanager.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tags")
public class Tag extends AbstractNamedEntity {

    /**
     * Обратная сторона ManyToMany; LAZY; владелец связи — Expense.tags, каскад не используется.
     */
    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    private Set<Expense> expenses = new HashSet<>();

    public Tag() {
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

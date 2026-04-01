package app.budgetmanager.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
public class Category extends AbstractNamedEntity {

    /**
     * Обратная сторона связи; LAZY; без каскада — справочник не «владеет» расходами.
     */
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Expense> expenses = new ArrayList<>();

    public Category() {
    }

    public Category(Long id, String name) {
        super(id, name);
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public void setExpenses(List<Expense> expenses) {
        this.expenses = expenses;
    }
}

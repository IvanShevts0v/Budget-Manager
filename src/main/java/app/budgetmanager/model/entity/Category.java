package app.budgetmanager.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "categories")
public class Category extends AbstractNamedEntity {

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Expense> expenses = new ArrayList<>();

    public Category() {
    }

    public Category(Long id, String name) {
        super(id, name);
    }
}

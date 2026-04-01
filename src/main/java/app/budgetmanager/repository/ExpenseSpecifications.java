package app.budgetmanager.repository;

import app.budgetmanager.entity.Category;
import app.budgetmanager.entity.Expense;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Условия выборки расходов на стороне БД (без загрузки всей таблицы в память).
 */
public final class ExpenseSpecifications {

    private ExpenseSpecifications() {
    }

    public static Specification<Expense> matchesFilter(
            Long id,
            String description,
            BigDecimal amount,
            String category,
            LocalDate date
    ) {
        return (Root<Expense> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (query != null) {
                query.distinct(true);
            }
            List<Predicate> predicates = new ArrayList<>();
            if (id != null) {
                predicates.add(cb.equal(root.get("id"), id));
            }
            if (description != null && !description.isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("description")), description.toLowerCase()));
            }
            if (amount != null) {
                predicates.add(cb.equal(root.get("amount"), amount));
            }
            if (category != null && !category.isEmpty()) {
                Join<Expense, Category> catJoin = root.join("category", JoinType.INNER);
                predicates.add(cb.equal(cb.lower(catJoin.get("name")), category.toLowerCase()));
            }
            if (date != null) {
                predicates.add(cb.equal(root.get("date"), date));
            }
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}

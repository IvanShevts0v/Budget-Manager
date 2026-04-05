package app.budgetmanager.repository;

import app.budgetmanager.model.entity.Category;
import app.budgetmanager.model.entity.Expense;
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
import java.util.Optional;

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
            applyDistinct(query);
            List<Predicate> predicates = new ArrayList<>();
            predicateById(root, cb, id).ifPresent(predicates::add);
            predicateByDescription(root, cb, description).ifPresent(predicates::add);
            predicateByAmount(root, cb, amount).ifPresent(predicates::add);
            predicateByCategoryName(root, cb, category).ifPresent(predicates::add);
            predicateByDate(root, cb, date).ifPresent(predicates::add);
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static void applyDistinct(CriteriaQuery<?> query) {
        if (query != null) {
            query.distinct(true);
        }
    }

    private static Optional<Predicate> predicateById(Root<Expense> root, CriteriaBuilder cb, Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.of(cb.equal(root.get("id"), id));
    }

    private static Optional<Predicate> predicateByDescription(
            Root<Expense> root,
            CriteriaBuilder cb,
            String description
    ) {
        if (description == null || description.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(cb.equal(cb.lower(root.get("description")), description.toLowerCase()));
    }

    private static Optional<Predicate> predicateByAmount(Root<Expense> root, CriteriaBuilder cb, BigDecimal amount) {
        if (amount == null) {
            return Optional.empty();
        }
        return Optional.of(cb.equal(root.get("amount"), amount));
    }

    private static Optional<Predicate> predicateByCategoryName(
            Root<Expense> root,
            CriteriaBuilder cb,
            String category
    ) {
        if (category == null || category.isEmpty()) {
            return Optional.empty();
        }
        Join<Expense, Category> catJoin = root.join("category", JoinType.INNER);
        return Optional.of(cb.equal(cb.lower(catJoin.get("name")), category.toLowerCase()));
    }

    private static Optional<Predicate> predicateByDate(Root<Expense> root, CriteriaBuilder cb, LocalDate date) {
        if (date == null) {
            return Optional.empty();
        }
        return Optional.of(cb.equal(root.get("date"), date));
    }
}

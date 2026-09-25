package app.budgetmanager.repository;

import app.budgetmanager.dto.ExpenseSearchCriteria;
import app.budgetmanager.model.entity.Category;
import app.budgetmanager.model.entity.Expense;
import app.budgetmanager.model.entity.Tag;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ExpenseSpecifications {

    private ExpenseSpecifications() {
    }

    public static Specification<Expense> matchesFilter(ExpenseSearchCriteria criteria) {
        return (Root<Expense> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            applyDistinct(query);
            List<Predicate> predicates = new ArrayList<>();
            predicateById(root, cb, criteria.id()).ifPresent(predicates::add);
            predicateByDescription(root, cb, criteria.description()).ifPresent(predicates::add);
            predicateByAmount(root, cb, criteria.amount()).ifPresent(predicates::add);
            predicateByCategoryName(root, cb, criteria.category()).ifPresent(predicates::add);
            predicateByDate(root, cb, criteria.date()).ifPresent(predicates::add);
            predicateByWalletOwner(root, cb, criteria.walletOwnerUserId()).ifPresent(predicates::add);
            predicates.addAll(predicatesByTagNames(root, query, cb, criteria.tagNames()));
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
        if (description == null || description.isBlank()) {
            return Optional.empty();
        }
        String pattern = "%" + description.trim().toLowerCase()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_") + "%";
        return Optional.of(cb.like(cb.lower(root.get("description")), pattern, '\\'));
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

    private static Optional<Predicate> predicateByWalletOwner(
            Root<Expense> root,
            CriteriaBuilder cb,
            Long walletOwnerUserId
    ) {
        if (walletOwnerUserId == null) {
            return Optional.empty();
        }
        return Optional.of(cb.equal(root.get("wallet").get("user").get("id"), walletOwnerUserId));
    }

    private static List<Predicate> predicatesByTagNames(
            Root<Expense> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            List<String> tagNames
    ) {
        if (tagNames == null || tagNames.isEmpty() || query == null) {
            return List.of();
        }
        List<Predicate> predicates = new ArrayList<>();
        for (String tagName : tagNames) {
            if (tagName == null || tagName.isBlank()) {
                continue;
            }
            String normalized = tagName.trim().toLowerCase();
            Subquery<Integer> subquery = query.subquery(Integer.class);
            Root<Expense> correlated = subquery.correlate(root);
            Join<Expense, Tag> tagJoin = correlated.join("tags", JoinType.INNER);
            subquery.select(cb.literal(1));
            subquery.where(cb.equal(cb.lower(tagJoin.get("name")), normalized));
            predicates.add(cb.exists(subquery));
        }
        return predicates;
    }

    private static Optional<Predicate> predicateByDate(Root<Expense> root, CriteriaBuilder cb, LocalDate date) {
        if (date == null) {
            return Optional.empty();
        }
        return Optional.of(cb.equal(root.get("date"), date));
    }
}

package app.budgetmanager.repository;

import app.budgetmanager.entity.Expense;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

    /**
     * Фильтрация в БД + тот же граф, что и для одной сущности (без N+1).
     */
    @EntityGraph(attributePaths = {"category", "tags", "wallet", "wallet.user"})
    @Override
    List<Expense> findAll(Specification<Expense> spec);

    @EntityGraph(attributePaths = {"category", "tags", "wallet", "wallet.user"})
    @Query("SELECT DISTINCT e FROM Expense e WHERE e.id = :id")
    Optional<Expense> findByIdWithAssociations(@Param("id") Long id);
}

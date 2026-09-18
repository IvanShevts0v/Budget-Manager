package app.budgetmanager.repository;

import app.budgetmanager.dto.ExpenseFilterView;
import app.budgetmanager.model.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

    @EntityGraph(attributePaths = {"category", "tags", "wallet", "wallet.user"})
    @Override
    List<Expense> findAll();

    @EntityGraph(attributePaths = {"category", "tags", "wallet", "wallet.user"})
    @Override
    List<Expense> findAll(Specification<Expense> spec);

    @EntityGraph(attributePaths = {"category", "tags", "wallet", "wallet.user"})
    @Query("SELECT DISTINCT e FROM Expense e WHERE e.id = :id")
    Optional<Expense> findByIdWithAssociations(@Param("id") Long id);

    @EntityGraph(attributePaths = {"category", "tags", "wallet", "wallet.user"})
    @Override
    Page<Expense> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"category", "tags", "wallet", "wallet.user"})
    @Override
    Page<Expense> findAll(Specification<Expense> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "tags", "wallet", "wallet.user"})
    @Query("SELECT e FROM Expense e WHERE e.wallet.user.id = :userId")
    List<Expense> findByWalletOwnerUserId(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {"category", "tags", "wallet", "wallet.user"})
    @Query("SELECT e FROM Expense e WHERE e.wallet.user.id = :userId")
    Page<Expense> findByWalletOwnerUserId(@Param("userId") Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "tags", "wallet", "wallet.user"})
    @Query("SELECT DISTINCT e FROM Expense e JOIN e.tags t WHERE t.id = :tagId")
    List<Expense> findByTagId(@Param("tagId") Long tagId);

    @Query(
            value = """
                    SELECT e.id AS id,
                           e.description AS description,
                           e.amount AS amount,
                           e.date AS date,
                           c.name AS category,
                           w.id AS walletId,
                           w.name AS walletName,
                           u.id AS userId,
                           u.username AS userName,
                           listagg(t.name, ',') AS tagNames
                    FROM Expense e
                    LEFT JOIN e.category c
                    LEFT JOIN e.wallet w
                    LEFT JOIN w.user u
                    LEFT JOIN e.tags t
                    WHERE (:walletOwnerUserId IS NULL OR u.id = :walletOwnerUserId)
                    AND (:categoryName IS NULL OR c.name = :categoryName)
                    GROUP BY e.id, e.description, e.amount, e.date, c.name, w.id, w.name,
                             u.id, u.username
                    """,
            countQuery = """
                    SELECT COUNT(e) FROM Expense e
                    LEFT JOIN e.category c
                    LEFT JOIN e.wallet w
                    LEFT JOIN w.user u
                    WHERE (:walletOwnerUserId IS NULL OR u.id = :walletOwnerUserId)
                    AND (:categoryName IS NULL OR c.name = :categoryName)
                    """
    )
    Page<ExpenseFilterView> findAllWithFiltersJpql(
            @Param("walletOwnerUserId") Long walletOwnerUserId,
            @Param("categoryName") String categoryName,
            Pageable pageable
    );

    // Native: тот же один SELECT, string_agg в том же списке полей.
    @Query(
            value = """
                    SELECT e.id AS id,
                           e.description AS description,
                           e.amount AS amount,
                           e.date AS date,
                           c.name AS category,
                           w.id AS walletId,
                           w.name AS walletName,
                           u.id AS userId,
                           u.username AS userName,
                           string_agg(t.name, ',' ORDER BY t.name) AS tagNames
                    FROM expenses e
                    LEFT JOIN wallets w ON e.wallet_id = w.id
                    LEFT JOIN categories c ON e.category_id = c.id
                    LEFT JOIN users u ON w.user_id = u.id
                    LEFT JOIN expense_tags et ON et.expense_id = e.id
                    LEFT JOIN tags t ON t.id = et.tag_id
                    WHERE (:walletOwnerUserId IS NULL OR u.id = :walletOwnerUserId)
                    AND (:categoryName IS NULL OR c.name = :categoryName)
                    GROUP BY e.id, e.description, e.amount, e.date, c.name, w.id, w.name,
                             u.id, u.username
                    """,
            countQuery = """
                    SELECT COUNT(*) FROM expenses e
                    LEFT JOIN wallets w ON e.wallet_id = w.id
                    LEFT JOIN categories c ON e.category_id = c.id
                    LEFT JOIN users u ON w.user_id = u.id
                    WHERE (:walletOwnerUserId IS NULL OR u.id = :walletOwnerUserId)
                    AND (:categoryName IS NULL OR c.name = :categoryName)
                    """,
            nativeQuery = true
    )
    Page<ExpenseFilterView> findAllWithFiltersNative(
            @Param("walletOwnerUserId") Long walletOwnerUserId,
            @Param("categoryName") String categoryName,
            Pageable pageable
    );
}

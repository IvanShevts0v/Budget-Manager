package app.budgetmanager.repository;

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
    @Query("SELECT e FROM Expense e WHERE e.wallet.user.id = :userId")
    List<Expense> findByWalletOwnerUserId(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {"category", "tags", "wallet", "wallet.user"})
    @Query("SELECT DISTINCT e FROM Expense e JOIN e.tags t WHERE t.id = :tagId")
    List<Expense> findByTagId(@Param("tagId") Long tagId);

    @EntityGraph(attributePaths = {"category", "tags", "wallet", "wallet.user"})
    @Query(
            value = """
                    SELECT DISTINCT e FROM Expense e
                    LEFT JOIN e.category c
                    LEFT JOIN e.wallet w
                    LEFT JOIN w.user u
                    WHERE (:walletOwnerUserId IS NULL OR u.id = :walletOwnerUserId)
                    AND (:categoryName IS NULL OR c.name = :categoryName)
                    """,
            countQuery = """
                    SELECT COUNT(DISTINCT e) FROM Expense e
                    LEFT JOIN e.category c
                    LEFT JOIN e.wallet w
                    LEFT JOIN w.user u
                    WHERE (:walletOwnerUserId IS NULL OR u.id = :walletOwnerUserId)
                    AND (:categoryName IS NULL OR c.name = :categoryName)
                    """
    )
    Page<Expense> findAllWithFiltersJpql(
            @Param("walletOwnerUserId") Long walletOwnerUserId,
            @Param("categoryName") String categoryName,
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT e.* FROM expenses e
                    JOIN wallets w ON e.wallet_id = w.id
                    JOIN categories c ON e.category_id = c.id
                    WHERE (:walletOwnerUserId IS NULL OR w.user_id = :walletOwnerUserId)
                    AND (:categoryName IS NULL OR c.name = :categoryName)
                    """,
            countQuery = """
                    SELECT COUNT(*) FROM expenses e
                    JOIN wallets w ON e.wallet_id = w.id
                    JOIN categories c ON e.category_id = c.id
                    WHERE (:walletOwnerUserId IS NULL OR w.user_id = :walletOwnerUserId)
                    AND (:categoryName IS NULL OR c.name = :categoryName)
                    """,
            nativeQuery = true
    )
    Page<Expense> findAllWithFiltersNative(
            @Param("walletOwnerUserId") Long walletOwnerUserId,
            @Param("categoryName") String categoryName,
            Pageable pageable
    );
}

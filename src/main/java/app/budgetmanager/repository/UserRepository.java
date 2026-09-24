package app.budgetmanager.repository;

import app.budgetmanager.model.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    @EntityGraph(attributePaths = {"wallets", "wallets.expenses"})
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithWallets(@Param("id") Long id);
}

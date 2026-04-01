package app.budgetmanager.service;

import app.budgetmanager.dto.DemoRelatedSaveRequest;
import app.budgetmanager.entity.User;
import app.budgetmanager.entity.Wallet;
import app.budgetmanager.repository.UserRepository;
import app.budgetmanager.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Демонстрация границ транзакций при сохранении связанных сущностей.
 * <p>
 * Без {@code @Transactional} на методе сервиса каждый вызов {@code repository.save}
 * выполняется в своей транзакции (прокси репозитория) и фиксируется до следующего шага.
 * При ошибке после части шагов уже сохранённые строки остаются в БД.
 * </p>
 * <p>
 * С {@code @Transactional} на методе сервиса все шаги объединяются в одну транзакцию:
 * при исключении откатывается всё, что было сделано внутри метода.
 * </p>
 */
@Service
public class RelatedEntitySaveDemoService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    public RelatedEntitySaveDemoService(UserRepository userRepository, WalletRepository walletRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
    }

    /**
     * Нет внешней транзакции на уровне сервиса: User и Wallet сохраняются отдельными
     * транзакциями репозитория. Если {@code failAfterWallet == true}, после сохранения
     * кошелька выбрасывается исключение — User и Wallet уже закоммичены.
     */
    public void saveUserAndWalletPartialCommit(DemoRelatedSaveRequest request) {
        persistUserAndWallet(request);
    }

    /**
     * Одна транзакция на весь метод: при {@code failAfterWallet == true} откатываются
     * и пользователь, и кошелёк.
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveUserAndWalletFullRollback(DemoRelatedSaveRequest request) {
        persistUserAndWallet(request);
    }

    private void persistUserAndWallet(DemoRelatedSaveRequest request) {
        User user = new User();
        user.setUsername(request.username());
        User savedUser = userRepository.save(user);

        Wallet wallet = new Wallet();
        wallet.setName(request.walletName());
        wallet.setUser(savedUser);
        walletRepository.save(wallet);

        if (request.failAfterWallet()) {
            throw new IllegalStateException(
                    "Симуляция ошибки после сохранения User и Wallet");
        }
    }
}

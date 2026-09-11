package app.budgetmanager.cache;

import app.budgetmanager.dto.ExpenseResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Component
public class ExpenseFilterCache {

    private final Map<ExpenseQueryKey, Page<ExpenseResponseDto>> cache = new HashMap<>();

    public Page<ExpenseResponseDto> getOrCompute(
            ExpenseQueryKey key,
            Supplier<Page<ExpenseResponseDto>> loader
    ) {
        synchronized (cache) {
            return cache.computeIfAbsent(key, ignored -> loader.get());
        }
    }

    public void invalidate() {
        if (TransactionSynchronizationManager.isSynchronizationActive()
                && TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    clear();
                }
            });
            return;
        }
        clear();
    }

    private void clear() {
        synchronized (cache) {
            cache.clear();
        }
    }
}

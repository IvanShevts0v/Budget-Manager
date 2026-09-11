package app.budgetmanager.cache;

import app.budgetmanager.dto.ExpenseResponseDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExpenseFilterCacheTest {

    private final ExpenseFilterCache cache = new ExpenseFilterCache();

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void getOrComputeShouldReuseStoredPage() {
        ExpenseQueryKey key = new ExpenseQueryKey(1L, "Food", PageRequest.of(0, 10), false);
        AtomicInteger loads = new AtomicInteger();
        Page<ExpenseResponseDto> page = new PageImpl<>(List.of());

        cache.getOrCompute(key, () -> {
            loads.incrementAndGet();
            return page;
        });
        cache.getOrCompute(key, () -> {
            loads.incrementAndGet();
            return page;
        });

        assertEquals(1, loads.get());
    }

    @Test
    void invalidateWithoutTransactionShouldDropCachedValueImmediately() {
        ExpenseQueryKey key = new ExpenseQueryKey(1L, "Food", PageRequest.of(0, 10), false);
        AtomicInteger loads = new AtomicInteger();
        Page<ExpenseResponseDto> page = new PageImpl<>(List.of());

        cache.getOrCompute(key, () -> {
            loads.incrementAndGet();
            return page;
        });
        cache.invalidate();
        cache.getOrCompute(key, () -> {
            loads.incrementAndGet();
            return page;
        });

        assertEquals(2, loads.get());
    }
}

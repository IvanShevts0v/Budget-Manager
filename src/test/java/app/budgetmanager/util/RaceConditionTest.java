package app.budgetmanager.util;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RaceConditionTest {

    public static final int EXECUTIONS = 500;
    public static final int THREAD_COUNT = 100;
    public static final int EXPECTED_COUNT = EXECUTIONS * THREAD_COUNT;

    @Test // гонка: атомарный счётчик сохраняет все обновления
    void countersShouldDemonstrateRaceConditionAndAtomicFix() throws InterruptedException {
        AtomicCounter atomicCounter = new AtomicCounter();
        NonAtomicCounter nonAtomicCounter = new NonAtomicCounter();

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.execute(() -> {
                for (int j = 0; j < EXECUTIONS; j++) {
                    atomicCounter.increment();
                    nonAtomicCounter.increment();
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        int nonAtomicResult = nonAtomicCounter.get();
        int atomicResult = atomicCounter.get();

        System.out.println("Atomic result: " + atomicResult);
        System.out.println("Non-atomic result: " + nonAtomicResult);

        assertEquals(EXPECTED_COUNT, atomicResult);
        assertTrue(
                nonAtomicResult < EXPECTED_COUNT,
                "Non-atomic counter should lose updates under contention, got " + nonAtomicResult
        );
    }
}

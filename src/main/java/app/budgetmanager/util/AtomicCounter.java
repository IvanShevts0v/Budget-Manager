package app.budgetmanager.util;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class AtomicCounter {

    private final AtomicInteger counter = new AtomicInteger();

    public void increment() {
        counter.incrementAndGet();
    }

    public int get() {
        return counter.get();
    }
}

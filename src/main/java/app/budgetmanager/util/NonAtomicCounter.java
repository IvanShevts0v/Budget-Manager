package app.budgetmanager.util;

public class NonAtomicCounter {

    private int counter;

    public void increment() {
        counter++;
    }

    public int get() {
        return counter;
    }
}

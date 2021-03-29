package rocks.spaghetti.tedium;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.BooleanSupplier;

public class Promise {
    private final Queue<PromiseRunnable> steps = new ArrayDeque<>();

    public Promise then(Runnable runnable) {
        steps.add(() -> {
            runnable.run();
            return true;
        });
        return this;
    }

    public Promise waitFor(BooleanSupplier condition) {
        steps.add(condition::getAsBoolean);
        return this;
    }

    public boolean isDone() {
        return steps.isEmpty();
    }

    public void runStep() {
        if (steps.peek() != null && steps.peek().run()) {
            steps.poll();
        }
    }

    @FunctionalInterface
    private interface PromiseRunnable {
        boolean run();
    }
}

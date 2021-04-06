package rocks.spaghetti.tedium;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

public class Latch {
    private final ConcurrentLinkedQueue<CountDownLatch> waiting = new ConcurrentLinkedQueue<>();

    public void await() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        waiting.add(latch);
        latch.await();
    }

    public void release() {
        while (!waiting.isEmpty()) {
            waiting.poll().countDown();
        }
    }
}

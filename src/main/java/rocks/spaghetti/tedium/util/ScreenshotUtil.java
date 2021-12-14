package rocks.spaghetti.tedium.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.ScreenshotRecorder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class ScreenshotUtil {
    private ScreenshotUtil() { throw new IllegalStateException("Utility Class"); }

    private static final ExecutorQueue runInClientThread = new ExecutorQueue();

    public static void takeScreenshot(Consumer<NativeImage> callback) {
        runInClientThread.execute(() -> {
            Framebuffer framebuffer = MinecraftClient.getInstance().getFramebuffer();
            callback.accept(ScreenshotRecorder.takeScreenshot(framebuffer));
        });
    }

    public static NativeImage takeScreenshotBlocking() {
        final CountDownLatch latch = new CountDownLatch(1);
        final NativeImage[] imageHolder = { null };

        takeScreenshot(image -> {
            imageHolder[0] = image;
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Log.catching(e);
            Thread.currentThread().interrupt();
        }

        return imageHolder[0];
    }

    public static void onClientTick(MinecraftClient client) {
        runInClientThread.runNext();
    }

    private static class ExecutorQueue implements Executor {
        final Queue<Runnable> tasks = new ArrayDeque<>();
        Runnable active = null;

        @Override
        public synchronized void execute(@NotNull final Runnable runnable) {
            tasks.offer(runnable);
        }

        protected synchronized void runNext() {
            if ((active = tasks.poll()) != null) {
                active.run();
            }
        }
    }
}

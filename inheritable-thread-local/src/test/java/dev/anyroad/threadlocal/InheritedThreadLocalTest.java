package dev.anyroad.threadlocal;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
public class InheritedThreadLocalTest {
    @Test
    @DisplayName("Test basic InheritableThreadLocal behavior - get value in the child thread")
    public void inheritableThreadLocalBasic() throws InterruptedException {
        ThreadLocal<String> threadLocal = new InheritableThreadLocal<>();

        String mainThreadData = "main thread";
        threadLocal.set(mainThreadData);

        ThreadLocalData dataFromChildThread = new ThreadLocalData("original data");

        Thread childThread = new Thread(() -> dataFromChildThread.setData(threadLocal.get()));

        childThread.start();
        childThread.join();

        assertEquals(mainThreadData, dataFromChildThread.getData());
    }

    @Test
    @DisplayName("Modify value in parent ThreadLocal after starting child thread")
    public void inheritableThreadChangeInParentThread() throws InterruptedException {
        ThreadLocal<String> threadLocal = new InheritableThreadLocal<>();

        String mainThreadData = "main thread";
        threadLocal.set(mainThreadData);

        ThreadLocalData dataFromChildThread = new ThreadLocalData("original data");

        CountDownLatch threadStartedLatch = new CountDownLatch(1);
        CountDownLatch valueChangedLatch = new CountDownLatch(1);

        Thread childThread = new Thread(() -> {
            threadStartedLatch.countDown();
            try {
                valueChangedLatch.await();
            } catch (InterruptedException e) {
                fail("Exception during latch await: " + e);
            }
            dataFromChildThread.setData(threadLocal.get());

        });

        childThread.start();

        threadStartedLatch.await();

        threadLocal.set("main new thread");
        valueChangedLatch.countDown();

        childThread.join();

        assertEquals(mainThreadData, dataFromChildThread.getData());
    }

    @Test
    @DisplayName("Thread created in Thread Pool inherit value")
    public void InheritableThreadLocalWithThreadPool() throws InterruptedException {
        ThreadLocal<String> threadLocal = new InheritableThreadLocal<>();

        String mainThreadData = "main thread";
        threadLocal.set(mainThreadData);

        ThreadLocalData dataFromChildThread = new ThreadLocalData("original data");

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> dataFromChildThread.setData(threadLocal.get()));
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.SECONDS);

        assertEquals(mainThreadData, dataFromChildThread.getData());
    }

    @Test
    @DisplayName("Second runnable submitted to Thread Pool does not get updated value")
    public void InheritableThreadLocalWithThreadPoolSecondRunnable() throws InterruptedException {
        ThreadLocal<String> threadLocal = new InheritableThreadLocal<>();

        String mainThreadData = "main thread";
        threadLocal.set(mainThreadData);

        ThreadLocalData dataFromChildThread = new ThreadLocalData("original data");

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        CountDownLatch firstRunnableFinished = new CountDownLatch(1);

        executorService.submit(() -> {
            dataFromChildThread.setData(threadLocal.get());
            firstRunnableFinished.countDown();
        });

        firstRunnableFinished.await();

        assertEquals(mainThreadData, dataFromChildThread.getData());

        threadLocal.set("main new thread");

        executorService.submit(() -> dataFromChildThread.setData(threadLocal.get()));

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.SECONDS);

        assertEquals(mainThreadData, dataFromChildThread.getData());
    }
}

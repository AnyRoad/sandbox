package dev.anyroad.spring.context.test.commons;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RestController
public abstract class Controller {
    private final static String ATTRIBUTE_NAME = "incremented number";
    private final static int SCOPE = RequestAttributes.SCOPE_REQUEST;

    private final Executor threadPool;
    private final AtomicInteger counter = new AtomicInteger(0);

    public Controller(Executor threadPool) {
        this.threadPool = threadPool;
    }

    @GetMapping("/one-level-child-thread")
    public ControllerResult oneLevel() throws ExecutionException, InterruptedException {
        RequestAttributes originalRequestAttributes = getRequestAttributes();
        int counterValue = counter.incrementAndGet();

        originalRequestAttributes.setAttribute(ATTRIBUTE_NAME, counterValue, SCOPE);
        counterValue = (int) originalRequestAttributes.getAttribute(ATTRIBUTE_NAME, SCOPE);

        CompletableFuture<RequestAttributes> requestFuture =
                CompletableFuture.supplyAsync(this::getRequestAttributes, threadPool);

        return buildResponse(requestFuture, counterValue, originalRequestAttributes);
    }

    protected abstract RequestAttributes getRequestAttributes();

    @GetMapping("/second-level-child-thread")
    public ControllerResult secondLevel() throws ExecutionException, InterruptedException {
        RequestAttributes originalRequestAttributes = getRequestAttributes();
        int counterValue = counter.incrementAndGet();

        originalRequestAttributes.setAttribute(ATTRIBUTE_NAME, counterValue, SCOPE);
        counterValue = (int) originalRequestAttributes.getAttribute(ATTRIBUTE_NAME, SCOPE);

        CompletableFuture<RequestAttributes> requestFuture =
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return CompletableFuture.supplyAsync(this::getRequestAttributes, threadPool).get();
                    } catch (InterruptedException | ExecutionException e) {
                        log.error("Cannot process second level child thread request:", e);
                        return null;
                    }
                }, threadPool);

        return buildResponse(requestFuture, counterValue, originalRequestAttributes);
    }

    @GetMapping("/async/one-level-child-thread")
    public Callable<ControllerResult> oneLevelAsync() {
        RequestAttributes originalRequestAttributes = getRequestAttributes();
        int counterValue = counter.incrementAndGet();

        originalRequestAttributes.setAttribute(ATTRIBUTE_NAME, counterValue, SCOPE);
        int counterValueFromAttributes = (int) originalRequestAttributes.getAttribute(ATTRIBUTE_NAME, SCOPE);

        return () -> {
            CompletableFuture<RequestAttributes> requestFuture =
                    CompletableFuture.supplyAsync(this::getRequestAttributes, threadPool);

            return buildResponse(requestFuture, counterValueFromAttributes, originalRequestAttributes);
        };
    }

    @GetMapping("/async/second-level-child-thread")
    public Callable<ControllerResult> secondLevelAsync() {
        RequestAttributes originalRequestAttributes = getRequestAttributes();
        int counterValue = counter.incrementAndGet();

        originalRequestAttributes.setAttribute(ATTRIBUTE_NAME, counterValue, SCOPE);
        int counterValueFromAttributes = (int) originalRequestAttributes.getAttribute(ATTRIBUTE_NAME, SCOPE);

        return () -> {
            CompletableFuture<RequestAttributes> requestFuture =
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            return CompletableFuture.supplyAsync(this::getRequestAttributes, threadPool).get();
                        } catch (InterruptedException | ExecutionException e) {
                            log.error("Cannot process second level child thread request:", e);
                            return null;
                        }
                    }, threadPool);

            return buildResponse(requestFuture, counterValueFromAttributes, originalRequestAttributes);
        };
    }

    // for testing
    public void resetCounterAndThreadPool() {
        counter.set(0);
    }

    private ControllerResult buildResponse(CompletableFuture<RequestAttributes> requestFuture, int counterValue,
                                           RequestAttributes originalRequestAttributes)
            throws ExecutionException, InterruptedException {
        RequestAttributes inheritedRequestAttributes = requestFuture.get();

        int attribute;
        try {
            attribute = (int) inheritedRequestAttributes.getAttribute(ATTRIBUTE_NAME, SCOPE);
        } catch (Exception ex) {
            return ControllerResult.builder()
                    .success(false)
                    .execptionMessage(ex.getMessage())
                    .build();
        }
        return ControllerResult.builder()
                .success(true)
                .parentThreadValue(counterValue)
                .childThreadValue(attribute)
                .childThreadHasSameRequestAttributes(originalRequestAttributes == inheritedRequestAttributes)
                .build();
    }
}

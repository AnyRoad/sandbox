package dev.anyroad.spring.context.test.transmittable;

import com.alibaba.ttl.threadpool.TtlExecutors;
import dev.anyroad.spring.context.test.commons.Controller;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.context.transmittable.RequestContextHolder;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@SpringBootApplication(exclude = DispatcherServletAutoConfiguration.class)
public class TransmittableServletApp {
    public static void main(String[] args) {
        SpringApplication.run(TransmittableServletApp.class, args);
    }

    @Bean
    public Controller controller() {
        return new Controller(TtlExecutors.getTtlExecutorService(Executors.newFixedThreadPool(2))) {
            @Override
            protected RequestAttributes getRequestAttributes() {
                return RequestContextHolder.getRequestAttributes();
            }
        };
    }

    @Bean
    public AsyncTaskExecutor asyncTaskExecutor() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(2);
        executor.setThreadNamePrefix("my-async-mvc");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        return executor;
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer(AsyncTaskExecutor asyncTaskExecutor) {
        return new WebMvcConfigurer() {
            @Override
            public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
                configurer.setTaskExecutor(asyncTaskExecutor);
            }
        };
    }
}
package dev.anyroad.spring.context.test.release;

import dev.anyroad.spring.context.test.commons.Controller;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.DispatcherServlet;

import java.util.concurrent.Executors;

@SpringBootApplication
public class ReleaseVersionApp implements InitializingBean {
    @Autowired
    private ApplicationContext context;
    @Autowired
    private Environment environment;

    public static void main(String[] args) {
        SpringApplication.run(ReleaseVersionApp.class, args);
    }

    @Bean
    public Controller controller() {
        return new Controller(Executors.newFixedThreadPool(2)) {
            @Override
            protected RequestAttributes getRequestAttributes() {
                return RequestContextHolder.getRequestAttributes();
            }
        };
    }

    @Override
    public void afterPropertiesSet() {
        DispatcherServlet dispatcherServlet = context.getBean(DispatcherServlet.class);
        Boolean threadContextInheritable = environment.getProperty("threadContextInheritable", Boolean.class, false);
        dispatcherServlet.setThreadContextInheritable(threadContextInheritable);
    }
}
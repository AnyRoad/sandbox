package dev.anyroad.spring.context.test.transmittable;

import dev.anyroad.spring.context.test.commons.Controller;
import dev.anyroad.spring.context.test.commons.ControllerResult;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "spring.main.allow-bean-definition-overriding=true")
public abstract class BaseMvcTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private Controller controller;

    protected ControllerResult callApi(String url) {
        return restTemplate.getForObject("http://localhost:" + port + url, ControllerResult.class);
    }

    @AfterEach
    public void afterEach() {
        controller.resetCounterAndThreadPool();
    }
}

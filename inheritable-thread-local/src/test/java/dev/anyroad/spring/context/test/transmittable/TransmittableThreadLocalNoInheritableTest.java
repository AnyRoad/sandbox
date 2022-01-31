package dev.anyroad.spring.context.test.transmittable;

import dev.anyroad.spring.context.test.commons.ControllerResult;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.servlet.TransmittableDispatcherServlet;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(classes = {
        TransmittableServletApp.class,
        TransmittableDispatcherServlet.class,
        TransmittableDispatcherServletAutoConfiguration.class
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "threadContextInheritable=false")
class TransmittableThreadLocalNoInheritableTest extends BaseMvcTest {

    @Test
    public void shouldNotInheritThreadLocal() {
        for (int i = 1; i <= 10; ++i) {
            ControllerResult response = callApi("/one-level-child-thread");
            assertFalse(response.isSuccess());
        }
    }
}
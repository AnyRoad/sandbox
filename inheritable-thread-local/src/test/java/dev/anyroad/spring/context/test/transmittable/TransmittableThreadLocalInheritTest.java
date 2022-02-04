package dev.anyroad.spring.context.test.transmittable;

import dev.anyroad.spring.context.test.commons.ControllerResult;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.servlet.TransmittableDispatcherServlet;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {
        TransmittableServletApp.class,
        TransmittableDispatcherServlet.class,
        TransmittableDispatcherServletAutoConfiguration.class
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "threadContextInheritable=true")
class TransmittableThreadLocalInheritTest extends BaseMvcTest {

    @Test
    public void shouldInheritRequestContestInChildThread() {
        for (int i = 1; i <= 10; ++i) {
            ControllerResult response = callApi("/one-level-child-thread");
            assertEquals(i, response.getChildThreadValue());
            assertEquals(i, response.getParentThreadValue());
            assertTrue(response.isChildThreadHasSameRequestAttributes());
            assertTrue(response.isSuccess());
        }
    }

    @Test
    public void shouldInheritRequestContestInGrandChildThread() {
        for (int i = 1; i <= 10; ++i) {
            ControllerResult response = callApi("/second-level-child-thread");
            assertEquals(i, response.getChildThreadValue());
            assertEquals(i, response.getParentThreadValue());
            assertTrue(response.isSuccess());
        }
    }

    @Test
    public void shouldInheritRequestContestInChildThreadAsyncResponse() {
        for (int i = 1; i <= 10; ++i) {
            ControllerResult response = callApi("/async/one-level-child-thread");
            assertEquals(i, response.getChildThreadValue());
            assertEquals(i, response.getParentThreadValue());
            assertFalse(response.isChildThreadHasSameRequestAttributes());
            assertTrue(response.isSuccess());
        }
    }

    @Test
    public void shouldInheritRequestContestInGrandChildThreadAsyncResponse() {
        for (int i = 1; i <= 10; ++i) {
            ControllerResult response = callApi("/async/second-level-child-thread");
            assertEquals(i, response.getChildThreadValue());
            assertEquals(i, response.getParentThreadValue());
            assertFalse(response.isChildThreadHasSameRequestAttributes());
            assertTrue(response.isSuccess());
        }
    }
}
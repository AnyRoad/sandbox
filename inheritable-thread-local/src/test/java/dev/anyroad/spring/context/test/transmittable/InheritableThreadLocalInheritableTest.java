package dev.anyroad.spring.context.test.transmittable;

import dev.anyroad.spring.context.test.commons.ControllerResult;
import dev.anyroad.spring.context.test.release.ReleaseVersionApp;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {
        ReleaseVersionApp.class
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "threadContextInheritable=true")
class InheritableThreadLocalInheritableTest extends BaseMvcTest {

    @Test
    public void shouldInheritThreadLocalOnly2Times() {
        for (int i = 1; i <= 2; ++i) {
            ControllerResult response = callApi("/one-level-child-thread");
            assertTrue(response.isSuccess());
        }
        // after both threads are created values is not updated anymore and original Request in the
        // Context is not valid
        for (int i = 1; i <= 2; ++i) {
            ControllerResult response = callApi("/one-level-child-thread");
            assertFalse(response.isSuccess());
        }
    }
}
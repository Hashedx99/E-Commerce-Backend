package com.hashed.ecombend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test — verifies the Spring context loads cleanly on the dev profile.
 * If this test passes, all beans are wired correctly.
 */
@SpringBootTest
@ActiveProfiles("dev")
class EcombendApplicationTests {

    @Test
    void contextLoads() {
        // If the application context starts without throwing, this test passes.
    }
}

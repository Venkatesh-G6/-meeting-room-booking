package com.yourcompany.roombooking;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({"test", "dev"})
class RoombookingApplicationTests {

    @Test
    void contextLoads() {
    }
}

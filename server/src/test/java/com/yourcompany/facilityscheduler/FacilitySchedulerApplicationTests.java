package com.yourcompany.facilityscheduler;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({"test", "dev"})
class FacilitySchedulerApplicationTests {

    @Test
    void contextLoads() {
    }
}

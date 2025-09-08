package org.amv.trafficsoft.datahub.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.context.annotation.Import;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TrafficsoftDataHubApplication.class, TestConfig.class})
public class TrafficsoftDataHubApplicationTest {

    @Test
    public void contextLoads() {
    }

}

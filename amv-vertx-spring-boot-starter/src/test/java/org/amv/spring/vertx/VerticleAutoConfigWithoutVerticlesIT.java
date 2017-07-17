package org.amv.spring.vertx;

import io.vertx.rxjava.core.Vertx;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {
        VerticleAutoConfigWithoutVerticlesIT.TestApplictaion.class
})
public class VerticleAutoConfigWithoutVerticlesIT {
    @SpringBootApplication
    public static class TestApplictaion {

    }

    @Autowired
    ApplicationContext applicationContext;

    @Test
    public void contextLoads() {
    }

    @Test
    public void hasVertxBean() {
        final Vertx bean = applicationContext.getBean(Vertx.class);
        assertThat(bean, is(notNullValue()));
    }
}

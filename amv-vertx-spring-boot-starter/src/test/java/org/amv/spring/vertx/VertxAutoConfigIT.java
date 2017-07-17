package org.amv.spring.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Verticle;
import io.vertx.rxjava.core.Vertx;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {
        VertxAutoConfigIT.TestApplictaion.class
})
public class VertxAutoConfigIT {
    @SpringBootApplication
    public static class TestApplictaion {

        @Bean
        public Verticle emptyVerticle() {
            return new AbstractVerticle() {
            };
        }

        @Bean
        public Verticle anotherEmptyVerticle() {
            return new AbstractVerticle() {
            };
        }
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

    @Test
    public void hasVerticleBeans() {
        final Map<String, Verticle> verticles = applicationContext.getBeansOfType(Verticle.class);
        assertThat(verticles.keySet(), hasSize(2));
    }

    @Test
    public void hasPropertiesLoaded() {
        final VertxProperties bean = applicationContext.getBean(VertxProperties.class);
        assertThat(bean, is(notNullValue()));
        assertThat(bean.getWorkerPoolSize(), is(42));
        assertThat(bean.getInternalBlockingPoolSize(), is(1337));
    }
}

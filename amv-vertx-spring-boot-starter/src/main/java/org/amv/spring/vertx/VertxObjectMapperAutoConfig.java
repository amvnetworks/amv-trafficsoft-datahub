package org.amv.spring.vertx;

import io.vertx.core.json.Json;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VertxObjectMapperAutoConfig implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        Json.mapper.findAndRegisterModules();
        Json.prettyMapper.findAndRegisterModules();
    }
}

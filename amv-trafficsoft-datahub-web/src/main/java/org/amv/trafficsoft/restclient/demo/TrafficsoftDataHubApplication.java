package org.amv.trafficsoft.restclient.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@Slf4j
@SpringBootApplication
public class TrafficsoftDataHubApplication {

    public static void main(String[] args) {
        log.info("Starting {} ...", TrafficsoftDataHubApplication.class.getSimpleName());

        new SpringApplicationBuilder(TrafficsoftDataHubApplication.class)
                .web(WebApplicationType.SERVLET)
                .bannerMode(Banner.Mode.CONSOLE)
                .run(args);
    }
}

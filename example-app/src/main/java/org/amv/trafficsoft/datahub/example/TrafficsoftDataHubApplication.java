package org.amv.trafficsoft.datahub.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.system.ApplicationPidFileWriter;
import org.springframework.boot.system.EmbeddedServerPortFileWriter;

@Slf4j
@SpringBootApplication
public class TrafficsoftDataHubApplication {

    public static void main(String[] args) {
        log.info("Starting {} ...", TrafficsoftDataHubApplication.class.getSimpleName());

        new SpringApplicationBuilder(TrafficsoftDataHubApplication.class)
                .web(true)
                .bannerMode(Banner.Mode.CONSOLE)
                .listeners(applicationPidFileWriter(), embeddedServerPortFileWriter())
                .run(args);
    }

    private static ApplicationPidFileWriter applicationPidFileWriter() {
        return new ApplicationPidFileWriter("app.pid");
    }

    private static EmbeddedServerPortFileWriter embeddedServerPortFileWriter() {
        return new EmbeddedServerPortFileWriter("app.port");
    }
}

package org.amv.trafficsoft.xfcd.consumer.postgres;

import com.google.common.io.Files;
import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.io.File;
import java.io.IOException;

@TestConfiguration
@EnableTransactionManagement
public class EmbeddedPostgresConfig {
    @Bean(destroyMethod = "close")
    public EmbeddedPostgres embeddedPostgres() {
        File tempDir = Files.createTempDir();
        tempDir.deleteOnExit();

        try {
            return EmbeddedPostgres.builder()
                    .setDataDirectory(tempDir)
                    .start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
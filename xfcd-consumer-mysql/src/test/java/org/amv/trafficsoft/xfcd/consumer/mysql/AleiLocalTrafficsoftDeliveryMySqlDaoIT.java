package org.amv.trafficsoft.xfcd.consumer.mysql;

import org.amv.trafficsoft.xfcd.consumer.jdbc.AbstractTrafficsoftDeliveryDaoTest;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryRowMapper;
import org.flywaydb.core.Flyway;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

/**
 * This class solely exists to run a sanity check on windows as testing against
 * a real mysql database is currently done only on unix based systems.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AleiLocalTrafficsoftDeliveryMySqlDaoIT.AleiLocalMySqlConfig.class})
@Transactional
@Ignore
public class AleiLocalTrafficsoftDeliveryMySqlDaoIT extends AbstractTrafficsoftDeliveryDaoTest {

    @BeforeClass
    public static void skipNonAlei2LocalEnvironment() {
        boolean isAlei2User = "alei2".equals(System.getProperty("user.name"));
        boolean isWindowsOs = OperationSystemHelper.isWindows();

        Assume.assumeTrue(isAlei2User && isWindowsOs);
    }

    @Configuration
    public static class AleiLocalMySqlConfig {

        @Bean(destroyMethod = "shutdown")
        public DataSource dataSource() {
            final String url = String.format("jdbc:mysql://localhost:%d/%s?" +
                            "profileSQL=true",
                    3306,
                    "alei2_datahub_it");

            DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
            dataSourceBuilder.username("alei2_test_user");
            dataSourceBuilder.password("Thisisthepasswordfortheintegrationtestuser1");
            dataSourceBuilder.driverClassName(com.mysql.jdbc.Driver.class.getName());
            dataSourceBuilder.url(url);
            return dataSourceBuilder.build();
        }

        @Bean
        public PlatformTransactionManager transactionManager() {
            DataSourceTransactionManager txManager = new DataSourceTransactionManager(dataSource());
            return txManager;
        }

        @Bean
        public JdbcTemplate jdbcTemplate() {
            return new JdbcTemplate(dataSource());
        }

        @Bean
        public NamedParameterJdbcTemplate namedParameterJdbcTemplate() {
            return new NamedParameterJdbcTemplate(jdbcTemplate());
        }


        @PostConstruct
        void startSchemaMigration() {
            final Flyway flyway = new Flyway();
            flyway.setDataSource(dataSource());
            flyway.setLocations("classpath:/db/mysql/xfcd/migration");

            flyway.migrate();
        }
    }

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    protected TrafficsoftDeliveryMySqlDaoImpl deliveryDao() {
        return new TrafficsoftDeliveryMySqlDaoImpl(namedParameterJdbcTemplate,
                new TrafficsoftDeliveryRowMapper());
    }
}

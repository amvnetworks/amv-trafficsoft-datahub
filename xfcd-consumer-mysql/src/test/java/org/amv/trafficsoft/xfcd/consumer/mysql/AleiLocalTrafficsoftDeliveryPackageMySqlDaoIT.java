package org.amv.trafficsoft.xfcd.consumer.mysql;

import org.amv.trafficsoft.xfcd.consumer.jdbc.*;
import org.flywaydb.core.Flyway;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

/**
 * This class solely exists to run a sanity check on windows as testing against
 * a real mysql database is currently done only on unix based systems.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
        DirtiesContextTestExecutionListener.class,
        DependencyInjectionTestExecutionListener.class
})
@ContextConfiguration(classes = {AleiLocalTrafficsoftDeliveryPackageMySqlDaoIT.AleiLocalMySqlConfig.class})
@Transactional
public class AleiLocalTrafficsoftDeliveryPackageMySqlDaoIT extends AbstractTrafficsoftDeliveryPackageDaoTest {

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

    @Override
    protected TrafficsoftXfcdNodeJdbcDao nodeDao() {
        return new TrafficsoftXfcdNodeMySqlDaoImpl(namedParameterJdbcTemplate,
                new TrafficsoftXfcdNodeRowMapper());
    }

    @Override
    protected TrafficsoftXfcdStateJdbcDao stateDao() {
        return new TrafficsoftXfcdStateMySqlDaoImpl(namedParameterJdbcTemplate,
                new TrafficsoftXfcdStateRowMapper());
    }

    @Override
    protected TrafficsoftXfcdXfcdJdbcDao xfcdDao() {
        return new TrafficsoftXfcdXfcdMySqlDaoImpl(namedParameterJdbcTemplate,
                new TrafficsoftXfcdXfcdRowMapper());
    }
}

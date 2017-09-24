package org.amv.trafficsoft.xfcd.consumer.mysql;

import org.amv.trafficsoft.xfcd.consumer.jdbc.*;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {DaoDbUnitTestConfig.class})
public class TrafficsoftDeliveryPackageMySqlDaoImplTest extends AbstractTrafficsoftDeliveryPackageDaoTest {

    @BeforeClass
    public static void skipWindowsOs() {
        Assume.assumeFalse(System.getProperty("os.name").startsWith("Windows"));
    }

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    protected TrafficsoftDeliveryMySqlDaoImpl deliveryDao() {
        return new TrafficsoftDeliveryMySqlDaoImpl(namedParameterJdbcTemplate,
                new TrafficsoftDeliveryRowMapper());
    }

    protected TrafficsoftXfcdNodeJdbcDao nodeDao() {
        return new TrafficsoftXfcdNodeMySqlDaoImpl(namedParameterJdbcTemplate,
                new TrafficsoftXfcdNodeRowMapper());
    }

    protected TrafficsoftXfcdStateJdbcDao stateDao() {
        return new TrafficsoftXfcdStateMySqlDaoImpl(namedParameterJdbcTemplate,
                new TrafficsoftXfcdStateRowMapper());
    }

    protected TrafficsoftXfcdXfcdJdbcDao xfcdDao() {
        return new TrafficsoftXfcdXfcdMySqlDaoImpl(namedParameterJdbcTemplate,
                new TrafficsoftXfcdXfcdRowMapper());
    }
}

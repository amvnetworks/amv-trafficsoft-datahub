package org.amv.trafficsoft.xfcd.consumer.mysql;

import org.amv.trafficsoft.xfcd.consumer.jdbc.*;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {DaoDbUnitTestConfig.class})
public class TrafficsoftDeliveryPackageMySqlDaoImplTest extends AbstractTrafficsoftDeliveryPackageDaoTest {

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

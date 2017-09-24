package org.amv.trafficsoft.xfcd.consumer.sqlite;

import org.amv.trafficsoft.xfcd.consumer.jdbc.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {DaoDbUnitTestConfig.class})
public class TrafficsoftDeliveryPackageSqliteDaoImplTest extends AbstractTrafficsoftDeliveryPackageDaoTest {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    protected TrafficsoftDeliverySqliteDaoImpl deliveryDao() {
        return new TrafficsoftDeliverySqliteDaoImpl(namedParameterJdbcTemplate,
                new TrafficsoftDeliveryRowMapper());
    }

    protected TrafficsoftXfcdNodeJdbcDao nodeDao() {
        return new TrafficsoftXfcdNodeSqliteDaoImpl(namedParameterJdbcTemplate,
                new TrafficsoftXfcdNodeRowMapper());
    }

    protected TrafficsoftXfcdStateJdbcDao stateDao() {
        return new TrafficsoftXfcdStateSqliteDaoImpl(namedParameterJdbcTemplate,
                new TrafficsoftXfcdStateRowMapper());
    }

    protected TrafficsoftXfcdXfcdJdbcDao xfcdDao() {
        return new TrafficsoftXfcdXfcdSqliteDaoImpl(namedParameterJdbcTemplate,
                new TrafficsoftXfcdXfcdRowMapper());
    }
}

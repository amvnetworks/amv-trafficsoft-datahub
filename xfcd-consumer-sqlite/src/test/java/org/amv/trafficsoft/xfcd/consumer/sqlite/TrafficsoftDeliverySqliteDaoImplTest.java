package org.amv.trafficsoft.xfcd.consumer.sqlite;

import org.amv.trafficsoft.xfcd.consumer.jdbc.AbstractTrafficsoftDeliveryDaoTest;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryJdbcDao;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {DaoDbUnitTestConfig.class})
public class TrafficsoftDeliverySqliteDaoImplTest extends AbstractTrafficsoftDeliveryDaoTest {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    protected TrafficsoftDeliveryJdbcDao deliveryDao() {
        TrafficsoftDeliveryRowMapper imTrafficsoftDeliveryRowMapper = new TrafficsoftDeliveryRowMapper();

        return new TrafficsoftDeliverySqliteDaoImpl(namedParameterJdbcTemplate,
                imTrafficsoftDeliveryRowMapper);
    }
}

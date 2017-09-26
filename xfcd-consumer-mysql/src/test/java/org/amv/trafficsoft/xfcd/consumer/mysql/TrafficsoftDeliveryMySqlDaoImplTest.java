package org.amv.trafficsoft.xfcd.consumer.mysql;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.amv.trafficsoft.xfcd.consumer.jdbc.AbstractTrafficsoftDeliveryDaoTest;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryEntity;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryEntityMother;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryRowMapper;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

import java.time.Instant;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@ContextConfiguration(classes = {DaoDbUnitTestConfig.class})
public class TrafficsoftDeliveryMySqlDaoImplTest extends AbstractTrafficsoftDeliveryDaoTest {

    @BeforeClass
    public static void skipWindowsOs() {
        Assume.assumeFalse(OperationSystemHelper.isWindows());
    }

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    protected TrafficsoftDeliveryMySqlDaoImpl deliveryDao() {
        TrafficsoftDeliveryRowMapper imTrafficsoftDeliveryRowMapper = new TrafficsoftDeliveryRowMapper();

        return new TrafficsoftDeliveryMySqlDaoImpl(namedParameterJdbcTemplate,
                imTrafficsoftDeliveryRowMapper);
    }

    @Test
    @DatabaseSetup(value = "/sample_data_deliveries_bulk.xml")
    public void itShouldUpdateDeliveryIfExists() throws Exception {
        TrafficsoftDeliveryMySqlDaoImpl sut = deliveryDao();

        TrafficsoftDeliveryEntity trafficsoftDeliveryEntity = TrafficsoftDeliveryEntityMother.randomUnconfirmed();

        sut.save(trafficsoftDeliveryEntity);

        assertThat(trafficsoftDeliveryEntity, is(notNullValue()));
        assertThat(trafficsoftDeliveryEntity.getId(), is(greaterThanOrEqualTo(1L)));

        final Instant updatedAtAfterSave = sut.fetchUpdatedAtById(trafficsoftDeliveryEntity.getId())
                .orElse(null);

        assertThat(updatedAtAfterSave, is(nullValue()));

        // consume again to set update flag
        sut.save(trafficsoftDeliveryEntity);

        final Instant updatedAt = sut.fetchUpdatedAtById(trafficsoftDeliveryEntity.getId())
                .orElse(null);
        assertThat(updatedAt, is(notNullValue()));
    }
}

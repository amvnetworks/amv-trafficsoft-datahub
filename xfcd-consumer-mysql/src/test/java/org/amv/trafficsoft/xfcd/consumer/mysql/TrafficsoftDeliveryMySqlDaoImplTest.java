package org.amv.trafficsoft.xfcd.consumer.mysql;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.amv.trafficsoft.xfcd.consumer.jdbc.AbstractTrafficsoftDeliveryDaoTest;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryEntity;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryEntityMother;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryRowMapper;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@ContextConfiguration(classes = {DaoDbUnitTestConfig.class})
public class TrafficsoftDeliveryMySqlDaoImplTest extends AbstractTrafficsoftDeliveryDaoTest {

    private static final Logger LOG = LoggerFactory.getLogger(TrafficsoftDeliveryMySqlDaoImplTest.class);

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
        LOG.info("[DEBUG_LOG] Test start: itShouldUpdateDeliveryIfExists at {}", LocalDateTime.now(ZoneId.systemDefault()));
        TrafficsoftDeliveryMySqlDaoImpl sut = deliveryDao();

        TrafficsoftDeliveryEntity trafficsoftDeliveryEntity = TrafficsoftDeliveryEntityMother.randomUnconfirmed();
        LOG.info("[DEBUG_LOG] Created random unconfirmed entity: {}", trafficsoftDeliveryEntity);

        long t0 = System.currentTimeMillis();
        LOG.info("[DEBUG_LOG] Calling first save()... ");
        sut.save(trafficsoftDeliveryEntity);
        LOG.info("[DEBUG_LOG] First save() completed in {} ms.", System.currentTimeMillis() - t0);

        assertThat(trafficsoftDeliveryEntity, is(notNullValue()));
        assertThat(trafficsoftDeliveryEntity.getId(), is(greaterThanOrEqualTo(1L)));
        LOG.info("[DEBUG_LOG] Entity after first save has id={}", trafficsoftDeliveryEntity.getId());

        final Instant updatedAtAfterSave = sut.fetchUpdatedAtById(trafficsoftDeliveryEntity.getId())
                .orElse(null);
        LOG.info("[DEBUG_LOG] updatedAt after first save: {}", updatedAtAfterSave);

        assertThat(updatedAtAfterSave, is(nullValue()));

        // consume again to set update flag
        long t1 = System.currentTimeMillis();
        LOG.info("[DEBUG_LOG] Calling second save() to trigger update...");
        sut.save(trafficsoftDeliveryEntity);
        LOG.info("[DEBUG_LOG] Second save() completed in {} ms.", System.currentTimeMillis() - t1);

        final Instant updatedAt = sut.fetchUpdatedAtById(trafficsoftDeliveryEntity.getId())
                .orElse(null);
        LOG.info("[DEBUG_LOG] updatedAt after second save: {}", updatedAt);
        assertThat(updatedAt, is(notNullValue()));
    }
}

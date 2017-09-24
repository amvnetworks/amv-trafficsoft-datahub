package org.amv.trafficsoft.xfcd.consumer.mysql;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.TransactionDbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.google.common.collect.ImmutableList;
import org.amv.trafficsoft.xfcd.consumer.jdbc.*;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@ContextConfiguration(classes = {DaoDbUnitTestConfig.class})
public class TrafficsoftDeliveryMySqlDaoImplTest extends AbstractTrafficsoftDeliveryDaoTest {

    @BeforeClass
    public static void skipWindowsOs() {
        Assume.assumeFalse(System.getProperty("os.name").startsWith("Windows"));
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

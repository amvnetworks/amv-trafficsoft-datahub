package org.amv.trafficsoft.xfcd.consumer.jdbc;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.TransactionDbUnitTestExecutionListener;
import com.google.common.collect.ImmutableList;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackage;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackageImpl;
import org.amv.trafficsoft.rest.xfcd.model.*;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional(transactionManager = "trafficsoftDeliveryJdbcConsumerTransactionManager")
@TestExecutionListeners({
        DirtiesContextTestExecutionListener.class,
        DependencyInjectionTestExecutionListener.class,
        TransactionDbUnitTestExecutionListener.class,
        DbUnitTestExecutionListener.class
})
public abstract class AbstractTrafficsoftDeliveryPackageDaoTest {

    private TrafficsoftDeliveryPackageJdbcDao sut;

    @Before
    public void setUp() throws SQLException, IOException {
        this.sut = systemUnderTest();
    }

    protected TrafficsoftDeliveryPackageJdbcDao systemUnderTest() {
        return DelegatingTrafficsoftDeliveryPackageDao.builder()
                .deliveryDao(deliveryDao())
                .nodeDao(nodeDao())
                .stateDao(stateDao())
                .xfcdDao(xfcdDao())
                .build();
    }

    protected abstract TrafficsoftDeliveryJdbcDao deliveryDao();

    protected abstract TrafficsoftXfcdNodeJdbcDao nodeDao();

    protected abstract TrafficsoftXfcdStateJdbcDao stateDao();

    protected abstract TrafficsoftXfcdXfcdJdbcDao xfcdDao();


    @Test
    public void itShouldPersistToDatabase() throws Exception {
        TrafficsoftDeliveryPackageJdbcDao sut = systemUnderTest();

        final List<DeliveryRestDto> deliveries = DeliveryRestDtoMother.randomList();

        sut.save(TrafficsoftDeliveryPackageImpl.builder()
                .deliveries(deliveries)
                .contractId(RandomUtils.nextLong())
                .build());

        final List<Long> deliveryIds = deliveries.stream()
                .map(DeliveryRestDto::getDeliveryId)
                .collect(toImmutableList());

        List<TrafficsoftDeliveryEntity> fromDb = deliveryDao().findByIds(deliveryIds);

        assertThat(fromDb, hasSize(deliveryIds.size()));

        final List<Long> deliveryIdsFromDb = deliveries.stream()
                .map(DeliveryRestDto::getDeliveryId)
                .collect(toImmutableList());

        assertThat(deliveryIdsFromDb, is(equalTo(deliveryIds)));
    }


    @Test
    public void itShouldTestNullableValuesInMysql() throws Exception {
        TrafficsoftDeliveryPackageJdbcDao sut = systemUnderTest();

        ParameterRestDto xfcdWithNullValues = ParameterRestDto.builder()
                .param("ANY_XFCD")
                .value(null)
                .build();

        ParameterRestDto stateWithNullValues = ParameterRestDto.builder()
                .param("ANY_STATE")
                .value(null)
                .build();

        NodeRestDto nodeWithNullValues = NodeRestDto.builder()
                .id(1L)
                .timestamp(Date.from(Instant.now()))
                .satellites(RandomUtils.nextInt())
                .vdop(null)
                .hdop(BigDecimal.valueOf(RandomUtils.nextInt(0, 10)))
                .latitude(null)
                .longitude(BigDecimal.valueOf(RandomUtils.nextInt(0, 1000)))
                .heading(null)
                .altitude(BigDecimal.valueOf(RandomUtils.nextInt(0, 1_000_000)))
                .addXfcd(xfcdWithNullValues)
                .addState(stateWithNullValues)
                .build();

        DeliveryRestDto delivery = DeliveryRestDto.builder()
                .deliveryId(1L)
                .timestamp(Date.from(Instant.now()))
                .addTrack(TrackRestDto.builder()
                        .id(1L)
                        .vehicleId(1L)
                        .addNode(nodeWithNullValues)
                        .build())
                .build();

        TrafficsoftDeliveryPackage deliveryPackage = TrafficsoftDeliveryPackageImpl.builder()
                .deliveries(ImmutableList.of(delivery))
                .contractId(RandomUtils.nextLong())
                .build();

        sut.save(deliveryPackage);

        TrafficsoftDeliveryEntity deliveryFromDb = deliveryDao().findById(delivery.getDeliveryId())
                .orElseThrow(IllegalStateException::new);

        assertThat(deliveryFromDb, is(notNullValue()));

        TrafficsoftXfcdNodeEntity nodeFromDb = nodeDao().findByContractIdAndDeliveryId(deliveryPackage.getContractId(), delivery.getDeliveryId())
                .stream().findFirst().orElseThrow(IllegalStateException::new);

        assertThat(nodeFromDb, is(notNullValue()));

        assertThat(nodeFromDb.getSatelliteCount(), is(Optional.ofNullable(nodeWithNullValues.getSatellites())));

        // null values
        assertThat(nodeFromDb.getVerticalDilution(), is(Optional.empty()));
        assertThat(nodeFromDb.getHeading(), is(Optional.empty()));
        assertThat(nodeFromDb.getLatitude(), is(Optional.empty()));

        // non-null values
        assertThat(nodeFromDb.getAltitude(), is(not(Optional.empty())));
        assertThat(nodeFromDb.getHorizontalDilution(), is(not(Optional.empty())));
        assertThat(nodeFromDb.getLongitude(), is(not(Optional.empty())));
    }
}

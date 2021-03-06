package org.amv.trafficsoft.xfcd.consumer.jdbc;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.TransactionDbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
        DirtiesContextTestExecutionListener.class,
        DependencyInjectionTestExecutionListener.class,
        TransactionDbUnitTestExecutionListener.class,
        DbUnitTestExecutionListener.class
})
@Transactional
public abstract class AbstractTrafficsoftDeliveryDaoTest {

    private TrafficsoftDeliveryJdbcDao sut;

    @Before
    public void setUp() throws SQLException, IOException {
        this.sut = deliveryDao();
    }

    protected abstract TrafficsoftDeliveryJdbcDao deliveryDao();


    @Test
    @DatabaseSetup(value = "/sample_data_deliveries_bulk.xml")
    public void itShouldFindDeliveryByIdEmpty() {
        long nonExistingId = -1L;
        Optional<TrafficsoftDeliveryEntity> fetchedDelivery = this.sut.findById(nonExistingId);

        assertThat(fetchedDelivery, is(notNullValue()));
        assertThat(fetchedDelivery.isPresent(), is(false));
    }

    @Test
    @DatabaseSetup(value = "/sample_data_deliveries_bulk.xml")
    public void itShouldFindDeliveryById() {
        TrafficsoftDeliveryEntity fetchedDelivery = this.sut.findById(21125L)
                .orElseThrow(IllegalStateException::new);

        assertThat(fetchedDelivery, is(notNullValue()));
        assertThat(fetchedDelivery.getId(), is(21125L));
    }

    @Test
    @DatabaseSetup(value = "/sample_data_deliveries_bulk.xml")
    public void itShouldFindDeliveryByIds() {
        ImmutableList<Long> anyExistingIds = ImmutableList.<Long>builder()
                .add(21125L)
                .add(7269L)
                .add(10776L)
                .add(7281L)
                .add(8393L)
                .add(15532L)
                .build();
        List<Long> nonExistingIds = LongStream.range(-10, 0).boxed()
                .collect(toImmutableList());

        ImmutableList<Long> ids = ImmutableList.<Long>builder()
                .addAll(nonExistingIds)
                .addAll(anyExistingIds)
                .build();

        List<TrafficsoftDeliveryEntity> fetchedDeliveries = this.sut.findByIds(ids);

        assertThat(fetchedDeliveries, is(notNullValue()));
        assertThat(fetchedDeliveries, hasSize(is(anyExistingIds.size())));

        List<Long> fetchedDeliveryIds = fetchedDeliveries.stream()
                .map(TrafficsoftDeliveryEntity::getId)
                .collect(toImmutableList());
        assertThat(fetchedDeliveryIds.containsAll(anyExistingIds), is(true));
    }

    @Test
    @DatabaseSetup(value = "/sample_data_deliveries_bulk.xml")
    public void itShouldInsertDelivery() throws Exception {
        TrafficsoftDeliveryEntity trafficsoftDeliveryEntity = TrafficsoftDeliveryEntityMother.randomUnconfirmed();

        this.sut.save(trafficsoftDeliveryEntity);

        assertThat(trafficsoftDeliveryEntity, is(notNullValue()));
        assertThat(trafficsoftDeliveryEntity.getId(), is(greaterThanOrEqualTo(1L)));

        final TrafficsoftDeliveryEntity refetchedEntity = this.sut.findById(trafficsoftDeliveryEntity.getId())
                .orElseThrow(IllegalStateException::new);

        assertThat(refetchedEntity, is(notNullValue()));
        assertThat(refetchedEntity.getId(), is(trafficsoftDeliveryEntity.getId()));
    }

    @Test
    public void itShouldConfirmDeliveryById() throws Exception {
        TrafficsoftDeliveryEntity trafficsoftDeliveryEntity = TrafficsoftDeliveryEntityMother.randomUnconfirmed();

        this.sut.save(trafficsoftDeliveryEntity);

        this.sut.confirmDeliveryById(trafficsoftDeliveryEntity.getId());

        TrafficsoftDeliveryEntity fetchedDelivery = this.sut.findById(trafficsoftDeliveryEntity.getId())
                .orElseThrow(IllegalStateException::new);

        assertThat(fetchedDelivery.getConfirmedAt(), is(notNullValue()));
    }

    @Test
    public void itShouldConfirmDeliveriesByIds() throws Exception {
        TrafficsoftDeliveryEntity trafficsoftDeliveryEntity = TrafficsoftDeliveryEntityMother.randomUnconfirmed();
        TrafficsoftDeliveryEntity trafficsoftDeliveryEntity2 = TrafficsoftDeliveryEntityMother.randomUnconfirmed();

        this.sut.saveAll(Arrays.asList(trafficsoftDeliveryEntity, trafficsoftDeliveryEntity2));

        ImmutableList<Long> ids = ImmutableList.<Long>builder()
                .add(trafficsoftDeliveryEntity.getId())
                .add(trafficsoftDeliveryEntity2.getId())
                .build();

        this.sut.confirmDeliveriesByIds(ids);

        List<TrafficsoftDeliveryEntity> fetchedDeliveries = this.sut.findByIds(ids);

        boolean allConfirmed = fetchedDeliveries.stream()
                .allMatch(delivery -> delivery.getConfirmedAt() != null);
        assertThat(allConfirmed, is(true));
    }

    @Test
    public void itShouldFindNoUnconfirmedDeliveryIdsForNonExistingBpc() {
        int nonExistingBpcId = 1;

        List<Long> result = this.sut.findIdsOfUnconfirmedDeliveriesByBpcId(nonExistingBpcId);
        assertThat(result, is(notNullValue()));
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    @DatabaseSetup(value = "/sample_data_deliveries_bulk.xml")
    public void itShouldFindUnconfirmedDeliveryIdsForExistingBpc() {
        int existingBpcId = 13072316;

        List<Long> result = this.sut.findIdsOfUnconfirmedDeliveriesByBpcId(existingBpcId);
        assertThat(result, is(notNullValue()));
        assertThat(result, hasSize(2));
        assertThat(result, contains(19613L, 21125L));
    }
}

package org.amv.trafficsoft.xfcd.consumer.jdbc;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.TransactionDbUnitTestExecutionListener;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackageImpl;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDtoMother;
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
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
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
                .contractId(RandomUtils.nextInt())
                .build());

        final List<Long> deliveryIds = deliveries.stream()
                .map(DeliveryRestDto::getDeliveryId)
                .collect(Collectors.toList());

        List<TrafficsoftDeliveryEntity> fromDb = deliveryDao().findByIds(deliveryIds);

        assertThat(fromDb, hasSize(deliveryIds.size()));

        final List<Long> deliveryIdsFromDb = deliveries.stream()
                .map(DeliveryRestDto::getDeliveryId)
                .collect(Collectors.toList());

        assertThat(deliveryIdsFromDb, is(equalTo(deliveryIds)));
    }
}

package org.amv.trafficsoft.xfcd.consumer.mysql;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.TransactionDbUnitTestExecutionListener;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackageImpl;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDtoMother;
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
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DaoDbUnitTestConfig.class})
@TestExecutionListeners({
        DirtiesContextTestExecutionListener.class,
        DependencyInjectionTestExecutionListener.class,
        TransactionDbUnitTestExecutionListener.class,
        DbUnitTestExecutionListener.class
})
@Transactional
public class TrafficsoftDeliveryPackageSqliteDaoImplTest {

    @BeforeClass
    public static void skipWindowsOs() {
        Assume.assumeFalse(System.getProperty("os.name").startsWith("Windows"));
    }

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private TrafficsoftDeliveryPackageJdbcDao sut;
    private TrafficsoftDeliveryJdbcDao deliveryDao;

    @Before
    public void setUp() throws SQLException, IOException {
        TrafficsoftDeliveryRowMapper deliveryRowMapper = new TrafficsoftDeliveryRowMapper();
        TrafficsoftXfcdNodeRowMapper nodeRowMapper = new TrafficsoftXfcdNodeRowMapper();
        TrafficsoftXfcdStateRowMapper stateRowMapper = new TrafficsoftXfcdStateRowMapper();
        TrafficsoftXfcdXfcdRowMapper xfcdRowMapper = new TrafficsoftXfcdXfcdRowMapper();

        this.deliveryDao = new TrafficsoftDeliveryMySqlDaoImpl(namedParameterJdbcTemplate,
                deliveryRowMapper);
        TrafficsoftXfcdNodeJdbcDao nodeDao = new TrafficsoftXfcdNodeMySqlDaoImpl(namedParameterJdbcTemplate,
                nodeRowMapper);
        TrafficsoftXfcdStateJdbcDao stateDao = new TrafficsoftXfcdStateMySqlDaoImpl(namedParameterJdbcTemplate,
                stateRowMapper);
        TrafficsoftXfcdXfcdJdbcDao xfcdDao = new TrafficsoftXfcdXfcdMySqlDaoImpl(namedParameterJdbcTemplate,
                xfcdRowMapper);

        this.sut = DelegatingTrafficsoftDeliveryPackageDao.builder()
                .deliveryDao(deliveryDao)
                .nodeDao(nodeDao)
                .stateDao(stateDao)
                .xfcdDao(xfcdDao)
                .build();
    }

    @Test
    public void itShouldPersistToDatabase() throws Exception {
        final List<DeliveryRestDto> deliveries = DeliveryRestDtoMother.randomList();

        sut.save(TrafficsoftDeliveryPackageImpl.builder()
                .deliveries(deliveries)
                .build());

        final long deliveryId = deliveries.stream().findFirst()
                .orElseThrow(IllegalStateException::new)
                .getDeliveryId();

        TrafficsoftDeliveryEntity fromDb = this.deliveryDao.findById(deliveryId)
                .orElseThrow(IllegalStateException::new);

        assertThat(fromDb, is(equalTo(fromDb)));
    }
}

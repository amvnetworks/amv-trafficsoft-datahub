package org.amv.trafficsoft.xfcd.consumer.mysql;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryEntity;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryJdbcDao;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Slf4j
@Transactional(transactionManager = "trafficsoftDeliveryJdbcConsumerTransactionManager")
public class TrafficsoftDeliveryMySqlDaoImpl implements TrafficsoftDeliveryJdbcDao {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<TrafficsoftDeliveryEntity> rowMapper;

    public TrafficsoftDeliveryMySqlDaoImpl(NamedParameterJdbcTemplate jdbcTemplate,
                                           RowMapper<TrafficsoftDeliveryEntity> rowMapper) {
        this.jdbcTemplate = requireNonNull(jdbcTemplate);
        this.rowMapper = requireNonNull(rowMapper);
    }

    @Override
    public void saveAll(List<TrafficsoftDeliveryEntity> deliveries) {
        requireNonNull(deliveries);

        String sql = "INSERT INTO `amv_trafficsoft_xfcd_delivery` " +
                "(`CREATED_AT`, `ID`, `TS`) " +
                "VALUES (:now, :id, :ts) " +
                "ON DUPLICATE KEY UPDATE " +
                "`UPDATED_AT` = :now";

        deliveries.forEach(delivery -> {
            jdbcTemplate.update(sql, ImmutableMap.<String, Object>builder()
                    .put("now", Date.from(Instant.now()))
                    .put("id", delivery.getId())
                    .put("ts", Date.from(delivery.getTimestamp()))
                    .build());
        });
    }

    @Override
    public void confirmDeliveriesByIds(Collection<Long> ids) {
        requireNonNull(ids, "`ids` must not be null");

        if (ids.isEmpty()) {
            return;
        }

        String sql = "UPDATE `amv_trafficsoft_xfcd_delivery` " +
                "SET `CONFIRMED` = :now " +
                "WHERE `CONFIRMED` IS NULL AND " +
                "`ID` IN(:ids)";

        int affectedRows = jdbcTemplate.update(sql, ImmutableMap.<String, Object>builder()
                .put("now", Date.from(Instant.now()))
                .put("ids", ids)
                .build());

        if (log.isDebugEnabled()) {
            log.debug("confirmDeliveriesByIds({}) affected {} row(s)", ids, affectedRows);
        }
    }

    @Override
    public List<TrafficsoftDeliveryEntity> findByIds(List<Long> ids) {
        requireNonNull(ids, "`ids` must not be null");

        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        String sql = "SELECT `ID`, `TS`, `CONFIRMED` " +
                "FROM `amv_trafficsoft_xfcd_delivery` " +
                "WHERE `ID` in (:ids) " +
                "ORDER BY `ID` ASC";

        List<TrafficsoftDeliveryEntity> deliveries = jdbcTemplate.query(sql, ImmutableMap.<String, Object>builder()
                .put("ids", ids)
                .build(), rowMapper);

        return ImmutableList.copyOf(deliveries);
    }

    @Override
    public Optional<TrafficsoftDeliveryEntity> findById(long id) {
        String sql = "SELECT `ID`, `TS`, `CONFIRMED` " +
                "FROM `amv_trafficsoft_xfcd_delivery` " +
                "WHERE `ID` = :id";

        try {
            TrafficsoftDeliveryEntity delivery = jdbcTemplate.queryForObject(sql, ImmutableMap.<String, Object>builder()
                    .put("id", id)
                    .build(), rowMapper);

            return Optional.of(delivery);
        } catch (EmptyResultDataAccessException e) {
            log.warn("", e);
            return Optional.empty();
        }
    }

    @Override
    public List<Long> findIdsOfUnconfirmedDeliveriesByBpcId(int bpcId) {
        String sql = "SELECT DISTINCT d.`ID` AS `ID` FROM `amv_trafficsoft_xfcd_delivery` d, `amv_trafficsoft_xfcd_node` n " +
                "WHERE d.`CONFIRMED` IS NULL AND " +
                "n.`BPC_ID` = :bpcId AND " +
                "d.`ID` = n.`IMXFCD_D_ID` " +
                "ORDER BY `ID`";

        List<Long> unconfirmedDeliveryIds = jdbcTemplate.queryForList(sql, ImmutableMap.<String, Object>builder()
                .put("bpcId", bpcId)
                .build(), Long.class);

        if (log.isDebugEnabled()) {
            log.debug("Found {} unconfirmed deliveries by bpcId '{}'", unconfirmedDeliveryIds.size(), bpcId);
        }

        return unconfirmedDeliveryIds;
    }

    @VisibleForTesting
    Optional<Instant> fetchUpdatedAtById(long id) {
        String sql = "SELECT `UPDATED_AT` " +
                "FROM `amv_trafficsoft_xfcd_delivery` " +
                "WHERE `ID` = :id";

        try {
            Timestamp updatedAtOrNull = jdbcTemplate.queryForObject(sql, ImmutableMap.<String, Object>builder()
                    .put("id", id)
                    .build(), (rs, rowNum) -> {
                return rs.getTimestamp("UPDATED_AT");
            });

            return Optional.ofNullable(updatedAtOrNull)
                    .map(Timestamp::toInstant);
        } catch (EmptyResultDataAccessException e) {
            log.warn("", e);
            return Optional.empty();
        }
    }
}

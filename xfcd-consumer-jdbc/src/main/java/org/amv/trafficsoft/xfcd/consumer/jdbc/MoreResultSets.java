package org.amv.trafficsoft.xfcd.consumer.jdbc;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * This is a helper class to work around a problem in MySql:
 * getObject(${identifier}, Long.class) will return 0 in MySql when the value is actually NULL.
 * With this approach it is ensured, that we will in fact really get NULL.
 */
public final class MoreResultSets {
    private MoreResultSets() {
        throw new UnsupportedOperationException();
    }

    public static Optional<Long> getLong(ResultSet rs, String columnLabel) throws SQLException {
        return Optional.ofNullable(rs.getObject(columnLabel))
                .map(Object::toString)
                .map(Longs::tryParse);
    }

    public static Optional<Integer> getInteger(ResultSet rs, String columnLabel) throws SQLException {
        return Optional.ofNullable(rs.getObject(columnLabel))
                .map(Object::toString)
                .map(Ints::tryParse);
    }
}

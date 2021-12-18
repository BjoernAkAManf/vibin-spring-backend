package com.vibinofficial.backend.utils;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;
import org.springframework.stereotype.Component;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@Component
@MappedTypes({UUID.class})
public final class UUIDTypeHandler implements TypeHandler<UUID> {
    @Override
    public void setParameter(final PreparedStatement ps, final int param, final UUID value, final JdbcType jdbcType) throws SQLException {
        ps.setString(param, value.toString());
    }

    @Override
    public UUID getResult(final ResultSet rs, final String columnName) throws SQLException {
        return toUUID(rs.getString(columnName));
    }

    @Override
    public UUID getResult(final ResultSet rs, final int columnIndex) throws SQLException {
        return toUUID(rs.getString(columnIndex));
    }

    @Override
    public UUID getResult(final CallableStatement cs, final int columnIndex) throws SQLException {
        return toUUID(cs.getString(columnIndex));
    }

    private UUID toUUID(final String str) {
        return UUID.fromString(str);
    }
}

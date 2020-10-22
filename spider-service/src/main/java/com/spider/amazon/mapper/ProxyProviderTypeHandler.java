package com.spider.amazon.mapper;

import com.spider.amazon.model.ProxyProvider;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedJdbcTypes(JdbcType.VARCHAR)
public class ProxyProviderTypeHandler extends BaseTypeHandler<ProxyProvider> {
    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, ProxyProvider provider, JdbcType jdbcType) throws SQLException {
        preparedStatement.setString(i, provider.getValue());
    }

    @Override
    public ProxyProvider getNullableResult(ResultSet resultSet, String s) throws SQLException {
        return ProxyProvider.fromString(resultSet.getString(s));
    }

    @Override
    public ProxyProvider getNullableResult(ResultSet resultSet, int i) throws SQLException {
        return ProxyProvider.fromString(resultSet.getString(i));
    }

    @Override
    public ProxyProvider getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        return ProxyProvider.fromString(callableStatement.getString(i));
    }
}

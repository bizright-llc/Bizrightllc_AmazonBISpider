package com.spider.amazon.interceptor;

import com.spider.amazon.cons.AmazonAdNodeType;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AmazonAdNodeTypeHandler extends BaseTypeHandler<AmazonAdNodeType> {
    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, AmazonAdNodeType topic, JdbcType jdbcType) throws SQLException {
        preparedStatement.setString(i, topic.getValue());
    }

    @Override
    public AmazonAdNodeType getNullableResult(ResultSet resultSet, String columnName) throws SQLException {
        return AmazonAdNodeType.fromString(resultSet.getString(columnName));
    }

    @Override
    public AmazonAdNodeType getNullableResult(ResultSet resultSet, int columnIndex) throws SQLException {
        return AmazonAdNodeType.fromString(resultSet.getString(columnIndex));
    }

    @Override
    public AmazonAdNodeType getNullableResult(CallableStatement callableStatement, int columnIndex) throws SQLException {
        return AmazonAdNodeType.fromString(callableStatement.getString(columnIndex));
    }
}

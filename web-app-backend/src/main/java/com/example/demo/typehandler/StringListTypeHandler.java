package com.example.demo.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@MappedJdbcTypes(JdbcType.ARRAY) // JDBC ARRAY 타입에 매핑
@MappedTypes(List.class)         // Java List 타입에 매핑
public class StringListTypeHandler extends BaseTypeHandler<List<String>> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType) throws SQLException {
        // List<String>를 DB Array로 변환 (INSERT/UPDATE 시 필요하다면 구현)
        // 여기서는 SELECT만 다루므로 일단 비워둠
        // Connection conn = ps.getConnection();
        // Array array = conn.createArrayOf("varchar", parameter.toArray());
        // ps.setArray(i, array);
        throw new UnsupportedOperationException("List<String> to SQL Array conversion not fully implemented yet.");
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return convertSqlArrayToList(rs.getArray(columnName));
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return convertSqlArrayToList(rs.getArray(columnIndex));
    }

    @Override
    public List<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return convertSqlArrayToList(cs.getArray(columnIndex));
    }

    // 핵심 변환 로직
    private List<String> convertSqlArrayToList(Array sqlArray) throws SQLException {
        if (sqlArray == null) {
            return Collections.emptyList(); // null 대신 빈 리스트 반환 (선호에 따라 null 반환 가능)
        }
        // JDBC Array를 Java String[]로 가져옴
        Object arrayObject = sqlArray.getArray();
        if (arrayObject instanceof String[]) {
            // String[]를 List<String>으로 변환
            return Arrays.asList((String[]) arrayObject);
        }
        // 예상치 못한 타입일 경우 (예: 다른 배열 타입) 빈 리스트 또는 null 반환
        System.err.println("Unexpected array type returned from DB: " + arrayObject.getClass().getName());
        return Collections.emptyList();
    }
}
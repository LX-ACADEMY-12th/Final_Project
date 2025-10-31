package com.example.demo.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.*;
import java.util.ArrayList; // 👈 [수정] ArrayList 사용
import java.util.Collections;
import java.util.List;
import java.util.Arrays; // Fallback용

@MappedJdbcTypes(JdbcType.ARRAY)
@MappedTypes(List.class)
public class StringListTypeHandler extends BaseTypeHandler<List<String>> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType) throws SQLException {
        // [완료] 쓰기(INSERT) 로직 (수정 불필요)
        String[] stringArray = parameter.toArray(new String[0]);
        Array sqlArray = ps.getConnection().createArrayOf("text", stringArray);
        ps.setArray(i, sqlArray);
    }

    // --- 읽기(SELECT) 로직 ---
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

    //
    // 💥 [핵심 수정] .getArray() 대신 .getResultSet()을 사용하여 안정성 확보 💥
    //
    private List<String> convertSqlArrayToList(Array sqlArray) throws SQLException {
        if (sqlArray == null) {
            return Collections.emptyList();
        }

        List<String> list = new ArrayList<>();
        ResultSet rs = null;
        try {
            // 1. (권장) SQL Array를 ResultSet으로 변환하여 직접 순회
            rs = sqlArray.getResultSet();

            while (rs.next()) {
                // 배열 요소는 2번째 인덱스(VALUE)에 있습니다.
                list.add(rs.getString(2));
            }
            return list;

        } catch (Exception e) {
            // 2. (Fallback) .getResultSet() 실패 시 기존 .getArray() 방식 시도
            System.err.println("Failed to convert SQL Array using getResultSet, falling back to getArray(). Error: " + e.getMessage());
            try {
                Object arrayObject = sqlArray.getArray();
                if (arrayObject instanceof String[]) {
                    return Arrays.asList((String[]) arrayObject);
                }
                // [[Ljava.lang.String; (String[][]) 타입이 넘어오는 경우
                if (arrayObject instanceof String[][]) {
                    // 2차원 배열이면 첫 번째 요소(1차원 배열)의 첫 번째 값만 반환 (임시방편)
                    String[][] multiDimArray = (String[][]) arrayObject;
                    if (multiDimArray.length > 0 && multiDimArray[0].length > 0) {
                        return Arrays.asList(multiDimArray[0]);
                    }
                }
            } catch (Exception e2) {
                System.err.println("Fallback getArray() also failed.");
                return Collections.emptyList();
            }
            System.err.println("Unexpected array type returned from DB: " + (sqlArray.getArray() != null ? sqlArray.getArray().getClass().getName() : "null"));
            return Collections.emptyList();

        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) { /* 무시 */ }
            }
            try {
                sqlArray.free();
            } catch (SQLException e) { /* 무시 */ }
        }
    }
}
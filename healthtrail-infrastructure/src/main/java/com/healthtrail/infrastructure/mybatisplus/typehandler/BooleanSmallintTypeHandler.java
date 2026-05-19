package com.healthtrail.infrastructure.mybatisplus.typehandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

/**
 * 把业务代码里的 Boolean 标记稳定映射到数据库里的 SMALLINT(0/1)。
 *
 * <p>当前健康业务同时兼容多套数据库脚本：
 * 1. MySQL 侧历史上把该字段定义成 `tinyint(1)`
 * 2. PostgreSQL / H2 测试环境里统一落成 `SMALLINT`
 *
 * <p>如果直接让 MyBatis 按 Boolean 原生写入，PostgreSQL 会把参数当成 boolean，
 * 最终出现“字段是 smallint，但表达式是 boolean”的类型冲突。
 * 因此这里统一收口成显式的 `0 / 1` 映射，保证：
 * 1. 业务层仍然保持最自然的 Boolean 语义；
 * 2. MySQL、PostgreSQL、H2 三套环境都能用同一套实体定义；
 * 3. 后续查询回填时，仍然能直接得到 Boolean，不需要在应用服务里手工转换。
 */
@MappedTypes(Boolean.class)
@MappedJdbcTypes(JdbcType.SMALLINT)
public class BooleanSmallintTypeHandler extends BaseTypeHandler<Boolean> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Boolean parameter, JdbcType jdbcType)
        throws SQLException {
        ps.setShort(i, Boolean.TRUE.equals(parameter) ? (short) 1 : (short) 0);
    }

    @Override
    public Boolean getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseDatabaseFlag(rs.getObject(columnName));
    }

    @Override
    public Boolean getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseDatabaseFlag(rs.getObject(columnIndex));
    }

    @Override
    public Boolean getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseDatabaseFlag(cs.getObject(columnIndex));
    }

    /**
     * 兼容不同驱动返回的 0/1 / true/false / t/f 形式。
     *
     * <p>大多数情况下 PostgreSQL / H2 会直接返回数值类型，
     * 但为了减少驱动差异带来的脆弱性，这里顺手兼容了字符串和原生 Boolean。
     */
    private Boolean parseDatabaseFlag(Object rawValue) {
        if (rawValue == null) {
            return null;
        }
        if (rawValue instanceof Boolean) {
            return (Boolean) rawValue;
        }
        if (rawValue instanceof Number) {
            return ((Number) rawValue).intValue() != 0;
        }

        String normalizedValue = String.valueOf(rawValue).trim();
        if (normalizedValue.isEmpty()) {
            return null;
        }
        if ("1".equals(normalizedValue)
            || "true".equalsIgnoreCase(normalizedValue)
            || "t".equalsIgnoreCase(normalizedValue)
            || "y".equalsIgnoreCase(normalizedValue)) {
            return Boolean.TRUE;
        }
        if ("0".equals(normalizedValue)
            || "false".equalsIgnoreCase(normalizedValue)
            || "f".equalsIgnoreCase(normalizedValue)
            || "n".equalsIgnoreCase(normalizedValue)) {
            return Boolean.FALSE;
        }

        throw new IllegalArgumentException("Unsupported SMALLINT boolean flag value: " + rawValue);
    }
}

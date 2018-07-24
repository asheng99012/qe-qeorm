package qeorm;

import qeorm.utils.JsonUtils;

import java.util.*;

/**
 * Created by ashen on 2017-2-4.
 */
public class SqlExecutor {

    public static <T> List<T> execSql(String sql, Object obj, Class<T> returnType) {
        Map<String, Object> map = JsonUtils.convert(obj, Map.class);
        return execSql(sql, map, returnType, null);
    }

    public static <T> List<T> execSql(String sql, Object obj, Class<T> returnType, String dbName) {
        Map<String, Object> map = JsonUtils.convert(obj, Map.class);
        return execSql(sql, map, returnType, dbName);
    }

    public static <T> List<T> execSql(String sql, Class<T> returnType, String dbName) {
        return execSql(sql, null, returnType, dbName);
    }

    public static <T> List<T> execSql(String sql, Class<T> returnType) {
        return execSql(sql, null, returnType);
    }

    public static <T> List<T> execSql(String sql, Map<String, Object> map, Class<T> returnType) {
        return execSql(sql, map, returnType, null);
    }

    public static <T> List<T> execSql(String sql, Map<String, Object> map, Class<T> returnType, String dbName) {
        String id = sql.replaceAll("\\s+", ".");
        if (SqlConfigManager.getSqlConfig(id) == null) {
            SqlConfig sqlConfig = new SqlConfig();
            sqlConfig.setId(id);
            sqlConfig.setSql(sql);
            sqlConfig.setDbName(dbName);
            sqlConfig.setReturnType(returnType.getName());
            SqlConfigManager.parseSql(sqlConfig);
            SqlConfigManager.addSqlConfig(sqlConfig);
        }
        SqlResult sqlResult = exec(SqlConfigManager.getSqlConfig(id), map);
        if (sqlResult.isOk())
            return (List<T>) sqlResult.getResult();
        return null;
    }

    public static SqlResult exec(String sqlId) {
        return exec(sqlId, null);
    }

    public static SqlResult exec(String sqlId, Object obj) {
        return exec(SqlConfigManager.getSqlConfig(sqlId), JsonUtils.convert(obj, Map.class));
    }

    public static SqlResult exec(String sqlId, Map<String, Object> map) {
        return exec(SqlConfigManager.getSqlConfig(sqlId), map);
    }

    public static SqlResult exec(SqlConfig sqlConfig, Map<String, Object> map) {
        SqlResult result = new SqlResultExecutor(sqlConfig, map).exec();
        return result;
    }

}


package qeorm;
import qeorm.utils.JsonUtils;

import java.util.*;

/**
 * Created by ashen on 2017-2-4.
 */
public class SqlExecutor {


    public static SqlResult execSql(String sql, Object obj) {
        return execSql(sql, JsonUtils.convert(obj, Map.class), null);
    }

    public static SqlResult execSql(String sql, Object obj, String dbName) {
        return execSql(sql, JsonUtils.convert(obj, Map.class), dbName);
    }

    public static SqlResult execSql(String sql, String dbName) {
        return execSql(sql, null, dbName);
    }

    public static SqlResult execSql(String sql) {
        return execSql(sql, null, null);
    }

    public static SqlResult execSql(String sql, Map<String, Object> map) {
        return execSql(sql, map, null);
    }

    public static SqlResult execSql(String sql, Map<String, Object> map, String dbName) {
        String id = sql.replaceAll("\\s+", ".");
        if (SqlConfigManager.getSqlConfig(id) == null) {
            SqlConfig sqlConfig = new SqlConfig();
            sqlConfig.setId(id);
            sqlConfig.setSql(sql);
            sqlConfig.setDbName(dbName);
            SqlConfigManager.parseSql(sqlConfig);
            SqlConfigManager.addSqlConfig(sqlConfig);
        }
        return exec(SqlConfigManager.getSqlConfig(id), map);
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


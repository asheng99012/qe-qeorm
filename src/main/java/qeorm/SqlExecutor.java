package qeorm;

import com.alibaba.druid.pool.DruidAbstractDataSource;
import com.google.common.base.Strings;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import qeorm.utils.JsonUtils;

import javax.sql.DataSource;
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

    //---------------------
    public static <T> T execSqlForObject(String sql, Object obj, Class<T> returnType) {
        Map<String, Object> map = JsonUtils.convert(obj, Map.class);
        return execSqlForObject(sql, map, returnType, null);
    }

    public static <T> T execSqlForObject(String sql, Object obj, Class<T> returnType, String dbName) {
        Map<String, Object> map = JsonUtils.convert(obj, Map.class);
        return execSqlForObject(sql, map, returnType, dbName);
    }

    public static <T> T execSqlForObject(String sql, Class<T> returnType, String dbName) {
        return execSqlForObject(sql, null, returnType, dbName);
    }

    public static <T> T execSqlForObject(String sql, Class<T> returnType) {
        return execSqlForObject(sql, null, returnType);
    }

    public static <T> T execSqlForObject(String sql, Map<String, Object> map, Class<T> returnType) {
        return execSqlForObject(sql, map, returnType, null);
    }

    public static <T> T execSqlForObject(String sql, Map<String, Object> map, Class<T> returnType, String dbName) {
        String id = sql.replaceAll("\\s+", ".");
        SqlConfig sqlConfig;
        if (SqlConfigManager.getSqlConfig(id) == null) {
            sqlConfig = new SqlConfig();
            sqlConfig.setId(id);
            sqlConfig.setSql(sql);
            sqlConfig.setDbName(dbName);
            sqlConfig.setReturnType(returnType.getName());
            SqlConfigManager.parseSql(sqlConfig);
            SqlConfigManager.addSqlConfig(sqlConfig);
        }
        sqlConfig = SqlConfigManager.getSqlConfig(id);
        SqlResult sqlResult = exec(sqlConfig, map);
        if (sqlResult.isOk()) {
            Object val = sqlResult.getResult();
            if (val instanceof List)
                return (T) ((List) val).get(0);
            else return (T) val;
        }
        return null;
    }
    //------------

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

        return getExecutor(sqlConfig).init(sqlConfig, map).exec();
    }

    public static SqlResultExecutor getExecutor(SqlConfig sqlConfig) {
        if (!Strings.isNullOrEmpty(sqlConfig.getProxy())) {
            return new ProxyExecutor();
        }
        NamedParameterJdbcDaoSupport jdbc = (NamedParameterJdbcDaoSupport) SqlSession.instance.getSupport(sqlConfig.getDbName());
        DruidAbstractDataSource dataSource = (DruidAbstractDataSource) jdbc.getDataSource();
        String url = dataSource.getUrl();
        return getExecutor(url);
    }

    public static SqlResultExecutor getExecutor(String url) {
        String className = "qeorm.";
        if (url.indexOf("elasticsearch") > -1)
            className = className + "EsResultExecutor";
        else if (url.indexOf("mongodb") > -1)
            className = className + "MongoDbExecutor";
        else
            className = className + "SqlResultExecutor";
        try {
            return (SqlResultExecutor) Class.forName(className).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }

}


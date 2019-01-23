package qeorm;

import com.alibaba.druid.pool.DruidDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import qeorm.AbstractRegexOperator;
import qeorm.utils.JsonUtils;
import qeorm.StringFormat;
import qeorm.utils.Wrap;

import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;

/**
 * Created by ashen on 2017-2-4.
 */
public class EsResultExecutor extends SqlResultExecutor {
    private Logger logger = LoggerFactory.getLogger(EsResultExecutor.class);


    @Override
    public <T> T exec(Map<String, Object> map) {
        List<Map<String, Object>> list = null;
        try {
            list = execSqlOnEs(map);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        String sqlType = getResult().getSqlConfig().getSqlType();

        if (sqlType.equals(SqlConfig.COUNT)) {
            return (T) getInt(list);
        } else if (sqlType.equals(SqlConfig.UPDATE) || sqlType.equals(SqlConfig.DELETE)) {
            return (T) getInt(list);
        } else if (sqlType.equals(SqlConfig.INSERT)) {
            return (T) getId(list);
        } else if (sqlType.equals(SqlConfig.SELECT)) {
            return (T) list;
        } else {
            return (T) list;
        }
    }

    public List<Map<String, Object>> execSqlOnEs(Map<String, Object> map) throws SQLException {
        String sql = getResult().getSql();
        Wrap wrap = Wrap.getWrap(map);
        sql = StringFormat.format(sql, new AbstractRegexOperator() {
            @Override
            public String getPattern() {
                return ":([\\.a-zA-Z\\d_]+)";
            }

            @Override
            public String exec(Matcher m) {
                return "'" + JsonUtils.toJson(wrap.getValue(m.group(1))) + "'";
            }
        });
        logger.info("要在数据库{}上执行的sql：{} , 参数为：{}", getResult().sqlConfig.getDbName(), sql, JsonUtils.toJson(map));
        NamedParameterJdbcDaoSupport jdbc = (NamedParameterJdbcDaoSupport) SqlSession.instance.getSupport(getResult().sqlConfig.getDbName());
        DruidDataSource dataSource = (DruidDataSource) jdbc.getDataSource();
        Connection connection = dataSource.getConnection();
        PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet resultSet = ps.executeQuery();
        List<Map<String, Object>> list = new ArrayList<>();
        ResultSetMetaData md = resultSet.getMetaData();// 获得结果集结构信息（元数据）
        int columnCount = md.getColumnCount();// ResultSet列数
        // ResultSet转List<Map>数据结构
        // next用于移动到ResultSet的下一行，使下一行成为当前行
        while (resultSet.next()) {
            Map<String, Object> data = new HashMap<>();
            for (int i = 0; i < columnCount; i++) {// 遍历获取对当前行的每一列的键值对，put到map中
                // rs.getObject(i) 获得当前行某一列字段的值
                data.put(md.getColumnName(i + 1).toLowerCase(), resultSet.getObject(i));
            }
            list.add(data);
        }
        ps.close();
        connection.close();
        return list;
    }

    public Object getInt(List<Map<String, Object>> list) {
        if (list.size() == 0) return null;
        Object val = null;
        Map<String, Object> map = list.get(0);
        for (String key : map.keySet()) {
            if (!key.equals("_id")) {
                val = map.get(key);
                break;
            }
        }
        if (val != null) {
            if (val instanceof Double)
                return ((Double) val).intValue();
            val = Integer.parseInt(val.toString());
        }
        return val;
    }

    public Object getId(List<Map<String, Object>> list) {
        if (list.size() == 0) return null;
        Object val = null;
        Map<String, Object> map = list.get(0);
        for (String key : map.keySet()) {
            if (key.equals("_id")) {
                val = map.get(key);
                break;
            }
        }
        return val;
    }


}

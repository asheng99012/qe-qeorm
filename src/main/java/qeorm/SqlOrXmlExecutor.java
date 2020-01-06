package qeorm;


import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import qeorm.utils.Iterables;
import qeorm.utils.XmlUtils;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SqlOrXmlExecutor {
    private static Logger log = LoggerFactory.getLogger(SqlOrXmlExecutor.class);
    public static ThreadLocal<SqlConfig> local = new ThreadLocal<>();

    public static QePage exec(String sqlOrXml, String countSql, Map<String, Object> params, String dbName) {
        return exec(sqlOrXml, countSql, params, true, dbName);
    }

    public static QePage exec(String sqlOrXml, String countSql, Map<String, Object> params, boolean needCount, String dbName) {
        try {
            String executorName = SqlExecutor.getExecutorByDbname(dbName).getClass().getName();
            if (executorName.contains("EsResultExecutor") || isGroupBy(sqlOrXml)) {
                params.remove("pn");
            }
            return execSqlOrXml(sqlOrXml, countSql, params, needCount, dbName);
        } finally {
            local.remove();
        }
    }

    //模拟 select into
    public static void selectInto(String sqlOrXml, Map<String, Object> params, String fromDbName, String toDbName, String toTableName, int pageSize) {
        iterablesExec(sqlOrXml, params, fromDbName, pageSize, (list) -> {
            SqlExecutor.batchInsert(toDbName, toTableName, list);
        });
    }

    //分页循环执行sql
    public static void iterablesExec(String sqlOrXml, Map<String, Object> params, String dbName, int pageSize, ExecList execList) {
        try {
            String executorName = SqlExecutor.getExecutorByDbname(dbName).getClass().getName();
            if (executorName.contains("HiveResultExecutor")
                    || isGroupBy(sqlOrXml)) {
                Iterables.<Map>chunk(execSqlOrXml(sqlOrXml, null, params, false, dbName), pageSize).forEach(list -> {
                    execList.exec((List) list);
                });
            } else {
                params.put("pn", 1);
                params.put("ps", pageSize);
                Iterables.<Map>chunk(() -> execSqlOrXml(sqlOrXml, null, params, false, dbName)).forEach(list -> {
                    execList.exec(list);
                    if (sqlOrXml.contains("{minid}")) {
                        params.put("minid", list.get(list.size() - 1).get("id"));
                    } else if (sqlOrXml.contains("{maxid}")) {
                        params.put("maxid", list.get(list.size() - 1).get("id"));
                    } else {
                        params.put("pn", Integer.parseInt(params.get("pn").toString()) + 1);
                    }
                });
            }
        } finally {
            local.remove();
        }
    }

    //执行xml 或 sql
    private static QePage execSqlOrXml(String sqlOrXml, String countSql, Map<String, Object> params, boolean needCount, String dbName) {
        if (sqlOrXml.contains("<sqlConfig")) {
            try {
                return execXml(sqlOrXml, countSql, params, needCount, dbName);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return null;
            }
        } else {
            return execSql(sqlOrXml, countSql, params, needCount, dbName);
        }
    }

    //执行sql
    private static QePage execSql(String sql, String countSql, Map<String, Object> params, boolean needCount, String dbName) {
        String executorName = SqlExecutor.getExecutorByDbname(dbName).getClass().getName();
        List ret = SqlExecutor.execSql(sql, params, Map.class, dbName);
        if (ret instanceof QePage && ((QePage) ret).getTotal() != null) {
            return (QePage) ret;
        }
        QePage page = new QePage();
        page.addAll(ret);
        if (!needCount) {
            return page;
        }
        Integer count = 10000;
        if (executorName.contains("MongoDbExecutor") || executorName.contains("EsResultExecutor")) {
            if (!Strings.isNullOrEmpty(countSql)) {
                count = SqlExecutor.execSqlForObject(countSql, params, Integer.class, dbName);
            }
            page.setTotal(count.longValue());
            return (QePage) ret;
        }
        if (executorName.contains("SqlResultExecutor") || executorName.contains("HiveResultExecutor")) {
            if (!Strings.isNullOrEmpty(countSql)) {
                count = SqlExecutor.execSqlForObject(countSql, params, Integer.class, dbName);
            } else {
                count = SqlExecutor.execSqlForObject("select count(*) from (" + sql + " ) tt", params, Integer.class, dbName);
            }
            page.setTotal(count.longValue());
            return page;
        }
        return null;
    }

    //执行xml
    private static QePage execXml(String xml, String countSql, Map<String, Object> params, boolean needCount, String dbName) throws Exception {
        String executorName = SqlExecutor.getExecutorByDbname(dbName).getClass().getName();
        SqlConfig config = local.get();
        if (config == null) {
            Element element = XmlUtils.parseXmlString(xml);
            element.setAttribute("id", UUID.randomUUID().toString().replaceAll("-", ""));
            config = SqlConfigManager.createSqlConfig(element, new HashMap() {{
                put("nameSpace", "");
            }});
            if (Strings.isNullOrEmpty(config.getDbName()))
                config.setDbName(dbName);
            local.set(config);
        }
        List ret = SqlExecutor.exec(config, params).getResult();
        if (ret instanceof QePage && ((QePage) ret).getTotal() != null) {
            return (QePage) ret;
        }
        QePage page = new QePage();
        page.addAll(ret);
        if (!needCount) {
            return page;
        }
        Integer count = 100000;

        if (executorName.contains("MongoDbExecutor") || executorName.contains("EsResultExecutor")) {
            if (!Strings.isNullOrEmpty(countSql)) {
                count = SqlExecutor.execSqlForObject(countSql, params, Integer.class, dbName);
            }
            page.setTotal(count.longValue());
            return (QePage) ret;
        }
        if (executorName.contains("SqlResultExecutor") || executorName.contains("HiveResultExecutor")) {
            if (!Strings.isNullOrEmpty(countSql)) {
                count = SqlExecutor.execSqlForObject(countSql, params, Integer.class, dbName);
            } else {
                count = SqlExecutor.execSqlForObject("select count(*) from (" + config.getSql() + " ) tt", params, Integer.class, config.getDbName());
            }
            page.setTotal(count.longValue());
            return page;
        }

        if (!Strings.isNullOrEmpty(countSql)) {
            count = SqlExecutor.execSqlForObject(countSql, params, Integer.class, dbName);
        }
        page.setTotal(count.longValue());
        return page;

    }


    private static boolean isGroupBy(String sqlOrXml) {
        String sql = sqlOrXml;
        if (sqlOrXml.contains("<sqlConfig")) {
            Element element = null;
            try {
                element = XmlUtils.parseXmlString(sqlOrXml);
                element.setAttribute("id", UUID.randomUUID().toString().replaceAll("-", ""));
                SqlConfig config = SqlConfigManager.createSqlConfig(element, new HashMap() {{
                    put("nameSpace", "");
                }});
                sql = config.getSql();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

        }
        return sql.matches("(?i)([\\s\\S]*)\\s+group\\s+by\\s*([\\s\\S]*)");
    }

    @FunctionalInterface
    public static interface ExecList<T> {
        void exec(List<T> list);
    }

}

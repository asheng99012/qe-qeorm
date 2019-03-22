package qeorm;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import qeorm.intercept.IFunIntercept;
import qeorm.intercept.ObjectToJsonString;
import qeorm.utils.ExtendUtils;
import qeorm.utils.JsonUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by ashen on 2017-2-4.
 */
public class SqlResultExecutor {

    private static SqlSession sqlSession;

    public static void setSqlSession(SqlSession _sqlSession) {
        sqlSession = _sqlSession;
    }

    private Logger logger = LoggerFactory.getLogger(SqlResultExecutor.class);
    SqlResult result;
    Map<String, Object> oParams;


    public SqlResult getResult() {
        return result;
    }

    public SqlResultExecutor() {
    }

    public SqlResultExecutor init(SqlConfig sqlConfig, Map<String, Object> map) {
        oParams = cloneMap(map);
        result = new SqlResult();
        result.setSqlConfig(sqlConfig);
        result.setParams(cloneMap(map));
        dealParamIntercepts();
        return this;
    }

    public SqlResult exec() {
        String sql = result.getSqlConfig().getSql();
        TimeWatcher.watch("生成sql:" + sql, new Action() {
            @Override
            public void apply() {
                if (sql.matches(SqlConfigManager.isInsertPattern)) {
                    createInsertSql();
                } else {
                    createSelectSql();
                }
            }
        });

        TimeWatcher.watch("在数据库" + result.sqlConfig.getDbName() + "上执行" + result.getSql(), new Action() {
            @Override
            public void apply() {
                result.setResult(exec(result.getParams()));
            }
        });
        if (!result.sqlConfig.isPrimitive()) dealFunIntercept(result.getResult());
        if (result.getResult() != null) {
            if (oParams != null && oParams.containsKey("pn"))
                oParams.remove("pn");
        }
        dealSqlIntercepts();
        if (!result.sqlConfig.isPrimitive())
            dealReturnType();
        else
            result.setResult(JsonUtils.convert(result.getResult(), result.getSqlConfig().getKlass()));

        return result;
    }

    public <T> T exec(Map<String, Object> map) {
        String sql = result.getSql();
        String sqlType = result.getSqlConfig().getSqlType();
        logger.info("要在数据库{}上执行的sql：{} , 参数为：{}", result.sqlConfig.getDbName(), sql, JsonUtils.toJson(map));
        NamedParameterJdbcOperations jdbc = sqlSession.getJdbcTemplate(this.result.sqlConfig.getDbName());
        if (sqlType.equals(SqlConfig.CURSOR)) {
            return (T) jdbc.query(sql, map, new RowCallbackHandlerResultSetExtractor(result.getSqlConfig().getRowCallbacks(), result));
        } else if (sqlType.equals(SqlConfig.COUNT)) {
            return (T) jdbc.queryForObject(sql, map, Integer.class);
        } else if (sqlType.equals(SqlConfig.UPDATE) || sqlType.equals(SqlConfig.DELETE)) {
            Object ret = jdbc.update(sql, map);
            CacheManager.instance.edit(result.getSqlConfig().getTableNameList());
            return (T) ret;
        } else if (sqlType.equals(SqlConfig.INSERT)) {
            KeyHolder keyholder = new GeneratedKeyHolder();
            jdbc.update(sql, new MapSqlParameterSource(map), keyholder);
            Object ret = keyholder.getKey().intValue();
            CacheManager.instance.edit(result.getSqlConfig().getTableNameList());
            return (T) ret;
        } else if (result.sqlConfig.isPrimitive())
            return (T) jdbc.queryForObject(sql, map, result.getSqlConfig().getKlass());
        else if (sqlType.equals(SqlConfig.SELECT)) {
            return (T) jdbc.queryForList(sql, map);
        } else {
            return (T) jdbc.queryForList(sql, map);
        }
    }

    private void createSelectSql() {
        String sql = result.getSqlConfig().getSql();
        Map<String, Object> map = result.getParams();
        for (SqlAnalysisNode node : result.getSqlConfig().getGroup()) {
            String temp = "";
            if (map.containsKey(node.getParam())) {
                if (node.isBy()) {
                    if (result.getSqlConfig().getSqlType().equals(SqlConfig.SELECT)) temp = node.getWhole().replace(
                            node.getParamWhole(),
                            String.valueOf(map.get(node.getParam())).replaceAll("'", "\\'"));
                    else
                        temp = " ";
                } else if (node.isLike()) {
                    temp = node.getWhole().replace(
                            node.getPrefix() + node.getParamWhole() + node.getSuffix(),
                            ":" + node.getParam());
                    map.put(node.getParam(),
                            node.getPrefix().replaceAll("'", "")
                                    + map.get(node.getParam())
                                    + node.getSuffix().replaceAll("'", ""));
                } else if (node.isIn() || node.isNotIn()) {
                    Object val = map.get(node.getParam());
                    Object[] vs = null;
                    if (val != null) {
                        if (val instanceof String) {
                            Collection t = Splitter.on(",").splitToList(String.valueOf(val));
                            vs = Iterators.toArray(t.iterator(), Object.class);
                        } else if (val instanceof Iterable) {
                            Iterable t = (Iterable) val;
                            vs = Iterators.toArray(t.iterator(), Object.class);
                        } else if (val.getClass().isArray()) {
                            vs = (Object[]) val;
                        } else {
                            vs = new Object[]{val};
                        }

                    }
                    if (vs != null) {
                        List<String> ps = Lists.newArrayList();
                        String paramName = node.getParam();
                        String key;
                        for (int l = vs.length, i = 0; i < l; i++) {
                            key = paramName + "_" + i;
                            ps.add(":" + key);
                            map.put(key, vs[i]);
                        }
                        String _ps = Joiner.on(",").join(ps);
                        temp = node.getWhole().replace(node.getParamWhole(), _ps);
                    } else {
                        temp = " 1=1 ";
                    }
                } else {
                    temp = node.getWhole().replace(node.getParamWhole(), ":" + node.getParam());
                }
            } else {
                if (!node.isBy())
                    temp = " 1=1 ";
            }
            sql = sql.replace(node.getWhole(), " " + temp + " ");
        }

        for (SqlAndOrNode node : result.getSqlConfig().getAndOrNodes()) {
            String temp = "";
            if (map.containsKey(node.getParam1()) && map.containsKey(node.getParam2())) {
                temp = node.getWhole().replace(node.getParamWhole1(), ":" + node.getParam1());
                temp = temp.replace(node.getParamWhole2(), ":" + node.getParam2());
            } else
                temp = " 1=1 ";
            sql = sql.replace(node.getWhole(), temp);
        }


        if (result.getSqlConfig().getSqlType().equals(SqlConfig.SELECT) && map.containsKey("ps") && map.containsKey("pn") && map.get("ps") != null && map.get("pn") != null) {
            int pn = Integer.valueOf(map.get("pn").toString());
            int ps = Integer.valueOf(map.get("ps").toString());
            int start = ps * (pn - 1);
            sql = sql + " limit " + start + " , " + ps;
        }
        sql=replaceWhere(sql);

//        sql = sql.replaceAll("(?i)1=1\\s*or\\s+", " ");
//        sql = sql.replaceAll("\\(+\\s*1=1\\s*\\)", " 1=1 ");
//        sql = sql.replaceAll("(?i)and\\s*1=1\\s+", " ");
//        sql = sql.replaceAll("(?i)or\\s*1=1\\s+", " ");
//        sql = sql.replaceAll("\\(+\\s*1=1\\s*\\)", " 1=1 ");
////        sql = sql.replaceAll("(?i)count\\s*\\([^\\)]+\\s*\\)", " count(1) ");
//        //update
//        sql = sql.replaceAll(",\\s*1=1\\s*", " ");
//        sql = sql.replaceAll("1=1\\s*,", " ");

        sql = sql.replaceAll("\\s+", " ");
        sql = sql.replaceAll("\\s+\\(\\s+\\)", "()");
        sql = sql.replaceAll("\\(\\s+", "(");
        sql = sql.replaceAll("\\s+\\)", ")");
        String type = result.getSqlConfig().getSqlType();
        if ((type.equals(SqlConfig.UPDATE) || type.equals(SqlConfig.DELETE))
                && sql.toLowerCase().endsWith("where 1=1 ")) {
            throw new SqlErrorException("更新语句缺少条件，会造成全表跟新：" + sql);
        }
        result.setSql(sql);
    }


    public String replaceWhere(String sql) {
        sql = sql.replaceAll(",\\s*1=1\\s*", " ");
        sql = sql.replaceAll("1=1\\s*,", " ");
        sql = sql.replaceAll("(?i)1=1\\s+or\\s+", " ");
        sql = sql.replaceAll("(?i)\\s+or\\s+1=1\\s*", " ");
        sql = sql.replaceAll("(?i)\\s+and\\s+1=1\\s*", " ");
        sql = sql.replaceAll("\\(+\\s*1=1\\s*\\)", " 1=1 ");
        if (sql.matches(".*,\\s*1=1\\s*.*")
                || sql.matches(".*1=1\\s*,.*")
                || sql.matches("(?i).*1=1\\s+or\\s+.*")
                || sql.matches("(?i).*\\s+or\\s+1=1\\s*.*")
                || sql.matches("(?i).*\\s+and\\s+1=1\\s*.*")
                || sql.matches(".*\\(+\\s*1=1\\s*\\).*"))
            sql = replaceWhere(sql);
        return sql;
    }

    private void createInsertSql() {
        String sql = result.getSqlConfig().getSql();
        List<String> ffList = result.getSqlConfig().getFfList();
        Map<String, Object> map = result.getParams();
        int index = 0;
        for (SqlAnalysisNode node : result.getSqlConfig().getGroup()) {
            if (map.containsKey(node.getParam())) {
                sql = sql.replace(node.getWhole(), ":" + node.getParam());
            } else {
                sql = sql.replace(node.getWhole(), "");
//                sql = sql.replace("`" + node.getParam() + '`', "");
                sql = sql.replace(ffList.get(index), "");

            }
            index = index + 1;
        }
        sql = sql.replaceAll(",(\\s*,)+", ",");
//        sql = sql.replaceAll("\\s\\s", "");
        sql = sql.replaceAll("\\(\\s*,", "(");
        sql = sql.replaceAll(",\\s*\\)", ")");
        result.setSql(sql);
    }

    private void dealParamIntercepts() {
        List<Pair<String, IFunIntercept>> list = result.getSqlConfig().getParamIntercepts();
        logger.info("有{}个paramIntercept需要处理", list.size());
        if (!list.isEmpty()) {
            for (Pair<String, IFunIntercept> intercept : list) {
                intercept.getValue().intercept(intercept.getKey(), result.getParams(), result);
            }
        }
    }

    private void dealFunIntercept(Object dataSet) {
        List<Pair<String, IFunIntercept>> list = result.getSqlConfig().getFunIntercepts();
        logger.info("有{}个funIntercept需要处理", list.size());
        logger.info("funIntercepts list :" + JsonUtils.toJson(list));
        if (!list.isEmpty() && dataSet instanceof List) {
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) dataSet;
            for (Map<String, Object> data : dataList) {
                for (Pair<String, IFunIntercept> intercept : list) {
                    if (intercept != null) {
                        logger.info(intercept.getValue().getClass().getName());
                        intercept.getValue().intercept(intercept.getKey(), data, result);
                    }
                }
            }
        }
    }

    private void dealSqlIntercepts() {
        Map<String, Object> map = result.getParams();
        String key = "withRelation";
        if (!map.containsKey(key) || Boolean.valueOf(map.get("withRelation").toString())) {
            List<SqlConfig> list = result.getSqlConfig().getSqlIntercepts();
            logger.info("有{}个sqlIntercept需要处理", list.size());
            if (!list.isEmpty()) {
                for (SqlConfig sqlConfig : list) {
                    if (sqlConfig.getId().equals(result.getSqlConfig().getId())) continue;
                    this.dealSqlIntercept(sqlConfig);
                }
            }
        }
    }

    private void dealSqlIntercept(SqlConfig sqlConfig) {
        SqlConfig realSqlConfig;
        if (!Strings.isNullOrEmpty(sqlConfig.getRefId()))
            realSqlConfig = SqlConfigManager.getSqlConfig(sqlConfig.getRefId());
        else realSqlConfig = sqlConfig;
        Map<String, Object> map = cloneMap(oParams);
        List<String> rs = Splitter.on("|").splitToList(sqlConfig.getRelationKey());
        if (!Strings.isNullOrEmpty(sqlConfig.getRelationKey())) {
            String relationKey = rs.get(0);
            if (relationKey.equals(SqlConfig.RESULTASINT))
                map.put(relationKey, Integer.valueOf(result.getResult().toString()));
            else {
                List<Object> _list = fetchAsArray((List<Map<String, Object>>) result.getResult(), relationKey);
                if (_list.size() == 0) return;
                map.put(relationKey, _list);
            }

        }
        SqlResult _ret = SqlExecutor.exec(realSqlConfig, map);
        result.setChilds(_ret);
        Object ret = _ret.getResult();

        if (Strings.isNullOrEmpty(sqlConfig.getExtend())) return;
        if (ret instanceof List) {
            List<Map<String, Object>> list = (List<Map<String, Object>>) ret;
            if (list.isEmpty()) return;
            String type = sqlConfig.getExtend();
            List<Map<String, Object>> data = (List<Map<String, Object>>) result.getResult();
            String mappby = TableStruct.getRealMappBy(list.get(0), rs.get(1));
            if (type.equals(ExtendUtils.EXTEND))
                data = ExtendUtils.extend(data, list, rs.get(0), mappby);
            else if (type.equals(ExtendUtils.ONE2ONE))
                data = ExtendUtils.extendOne2One(data, list, rs.get(0), mappby, sqlConfig.getFillKey());
            else if (type.equals(ExtendUtils.ONE2MANY))
                data = ExtendUtils.extendOne2Many(data, list, rs.get(0), mappby, sqlConfig.getFillKey());
            result.setResult(data);
        }
    }

    private void dealReturnType() {
        logger.info("dealReturnType");
        if (!Strings.isNullOrEmpty(result.getSqlConfig().getReturnType()) && result.getSqlConfig().getSqlType().equals(SqlConfig.SELECT)) {
            Class clz = result.getSqlConfig().getKlass();
            List<Map> datas = (List<Map>) result.getResult();
            List list = new ArrayList();
            for (Map map : datas) {
                list.add(JsonUtils.convert(map, clz));
            }
            result.setResult(list);
        }
    }

    private static Map<String, Object> cloneMap(Map<String, Object> map) {
        Map<String, Object> params = Maps.newHashMap();
        if (map != null) params.putAll(map);
        return params;
    }

    public static List<Object> fetchAsArray(List<Map<String, Object>> list, String key) {
        List<Object> params = Lists.newArrayList();
        Map<Object, Boolean> hash = Maps.newHashMap();
        for (Map<String, Object> map : list) {
            if (!hash.containsKey(map.get(key))) {
                params.add(map.get(key));
                hash.put(map.get(key), true);
            }
        }

        return params;
    }


}

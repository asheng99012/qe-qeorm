package qeorm;

import com.alibaba.fastjson.TypeReference;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import qeorm.intercept.IFunIntercept;
import qeorm.intercept.IRowCallback;
import qeorm.utils.JsonUtils;
import qeorm.utils.XmlUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static qeorm.utils.XmlUtils.getAttributes;

/**
 * Created by ashen on 2017-2-3.
 */
public class SqlConfigManager {
    private static Logger logger = LoggerFactory.getLogger(SqlConfigManager.class);
    private static Map<String, SqlConfig> sqlConfigMap = new ConcurrentHashMap<>();

    private static int dsIdenty = 3;

    public static String sqlSelectPattern = "(\\s+|,)(?i)([`\\.'a-zA-Z\\d_]+){1}\\s*(!=|>=|=<|=>|<=|<|>|=|\\s+like\\s+|\\s+in\\s+|\\s+not\\s+in\\s+|\\s+by\\s+){1}\\s*([\\(%'`\\.'a-zA-Z\\d_\\+\\-\\s]+){0,1}\\s*(\\{\\s*([a-zA-Z\\d_]+)\\s*\\}){1}(\\s*[\\)%']+){0,1}";
    public static String sqlInsertPattern = "\\{([a-zA-Z\\d_]+)+\\}";
    public static String sqlAndOrPattern = "\\s+(?i)([`\\.'a-zA-Z\\d_]+){1}\\s*(&|\\|){1}\\s*(\\{\\s*([a-zA-Z\\d_]+)\\s*\\}){1}\\s*=\\s*(\\{\\s*([a-zA-Z\\d_]+)\\s*\\}){1}";
    public static String sqlTablePattern = "\\s+(?i)(from|join|into|update)\\s+([`\\.a-zA-Z\\d_]+){1}";

    public static String isCountPattern = "^\\s*(?i)select\\s+count\\s*\\(.+?\\)\\s+from.+";
    public static String isSelectPattern = "^\\s*(?i)select\\s+.+";
    public static String isUpdatePattern = "^\\s*(?i)update\\s+.+";
    public static String isDeletePattern = "^\\s*(?i)delete\\s+.+";
    public static String isInsertPattern = "^\\s*(?i)insert\\s+.+";

    public static void setDsIdenty(int _dsIdenty) {
        dsIdenty = _dsIdenty;
    }


    /**
     * 格式化sqlconfig的sql语句
     *
     * @param sqlConfig
     */
    public static void parseSql(SqlConfig sqlConfig) {


        String sql = sqlConfig.getSql();
//        sql = sql.replaceAll("\\n", " ");
//        sql = sql.replaceAll("\\(", " \\(  ");
//        sql = sql.replaceAll("\\)", " \\)  ");
//        sql = sql.replaceAll(",", " , ");
//        sql = sql.replaceAll("\\s+", " ");
        sql = " " + sql.trim();
        sqlConfig.setSql(sql);
        if (sql.matches(isInsertPattern))
            parseInsertSql(sqlConfig);
        else parseSelectSql(sqlConfig);
        parseTableName(sqlConfig);
        String sqlType;
        if (sql.matches(SqlConfigManager.isCountPattern)) {
            sqlType = SqlConfig.COUNT;
        } else if (sql.matches(SqlConfigManager.isSelectPattern)) {
            sqlType = SqlConfig.SELECT;
        } else if (sql.matches(SqlConfigManager.isUpdatePattern) || sql.matches(SqlConfigManager.isDeletePattern)) {
            sqlType = SqlConfig.UPDATE;
        } else if (sql.matches(SqlConfigManager.isInsertPattern)) {
            sqlType = SqlConfig.INSERT;
        } else {
            sqlType = SqlConfig.SELECT;
        }
        sqlConfig.setSqlType(sqlType);
        if (Strings.isNullOrEmpty(sqlConfig.getDbName())) {
            String dsni = "";
            if (!Strings.isNullOrEmpty(sqlConfig.getParentDbName()))
                dsni = sqlConfig.getParentDbName();
            else if (!Strings.isNullOrEmpty(sqlConfig.getId()))
                dsni = Splitter.on(".").splitToList(sqlConfig.getId()).get(dsIdenty - 1);
            if (sqlType.equals(SqlConfig.SELECT) || sqlType.equals(SqlConfig.COUNT))
                sqlConfig.setDbName(dsni + SqlSession.Slave);
            else sqlConfig.setDbName(dsni + SqlSession.Master);
        }
        if (sqlConfig.getRowCallbacks().size() > 0)
            sqlConfig.setSqlType(SqlConfig.CURSOR);
    }

    /**
     * 格式化select形式的sql语句
     *
     * @param sqlConfig
     */
    private static void parseSelectSql(SqlConfig sqlConfig) {
        Matcher om = Pattern.compile(sqlSelectPattern).matcher(sqlConfig.getSql());
        //0:whole  1:field  2:operator  3:prefix  4:paramwho 5:param 6:suffix
        List<SqlAnalysisNode> list = new ArrayList<SqlAnalysisNode>();
        while (om.find()) {
            Matcher m = getRealMatcher(om);
            SqlAnalysisNode analysisNode = new SqlAnalysisNode();
            analysisNode.setWhole(m.group(0));
            analysisNode.setWholePrefix(m.group(1));
            analysisNode.setField(m.group(2));
            analysisNode.setOperator(m.group(3));
            analysisNode.setPrefix(m.group(4));
            analysisNode.setParamWhole(m.group(5));
            analysisNode.setParam(m.group(6));
            analysisNode.setSuffix(m.group(7));
//            if (analysisNode.getWhole().endsWith(")") && analysisNode.getWhole().indexOf("(") == -1) {
            if (analysisNode.getWhole().endsWith(")") && (Strings.isNullOrEmpty(analysisNode.getPrefix()) || !analysisNode.getPrefix().startsWith("("))) {
                analysisNode.setWhole(analysisNode.getWhole().replaceAll("\\)$", ""));
                analysisNode.setSuffix(analysisNode.getSuffix().replaceAll("\\)$", ""));
            }
            list.add(analysisNode);
        }
        sqlConfig.setGroup(list);
        parseAndOrSql(sqlConfig);
    }

    private static void parseAndOrSql(SqlConfig sqlConfig) {
        Matcher m = Pattern.compile(sqlAndOrPattern).matcher(sqlConfig.getSql());
        //0:whole  1:field  2:operator  3:prefix  4:paramwho 5:param 6:suffix
        List<SqlAndOrNode> list = new ArrayList<SqlAndOrNode>();
        while (m.find()) {
            SqlAndOrNode node = new SqlAndOrNode();
            node.setWhole(m.group(0));
            node.setField(m.group(1));
            node.setOperator(m.group(2));
            node.setParamWhole1(m.group(3));
            node.setParam1(m.group(4));
            node.setParamWhole2(m.group(5));
            node.setParam2(m.group(6));
            list.add(node);
        }
        sqlConfig.setAndOrNodes(list);
    }

    /**
     * 格式化insert格式的sql语句
     *
     * @param sqlConfig
     */
    private static void parseInsertSql(SqlConfig sqlConfig) {
//        Matcher m = Pattern.compile(sqlInsertPattern).matcher(sqlConfig.getSql());
//        List<SqlAnalysisNode> list = new ArrayList<SqlAnalysisNode>();
//        while (m.find()) {
//            SqlAnalysisNode analysisNode = new SqlAnalysisNode();
//            analysisNode.setWhole(m.group(0));
//            analysisNode.setParamWhole(m.group(0));
//            analysisNode.setParam(m.group(1));
//            list.add(analysisNode);
//        }
        String sql = sqlConfig.getSql();
        int start = sql.indexOf("(");
        int end = sql.indexOf(")");
        String[] ffs = sql.substring(start + 1, end).split(",");
        String[] vvs = sql.substring(sql.indexOf("(", start + 1) + 1, sql.indexOf(")", end + 1)).split(",");
        List<SqlAnalysisNode> list = new ArrayList<SqlAnalysisNode>();
        List<String> ffList = new ArrayList<String>();
        for (int i = 0; i < vvs.length; i++) {
            start = vvs[i].indexOf("{");
            end = vvs[i].indexOf("}");
            if (start > -1 && end > -1) {
                SqlAnalysisNode node = new SqlAnalysisNode();
                node.setWhole(vvs[i].substring(start, end + 1).trim());
                node.setParamWhole(node.getWhole());
                node.setParam(vvs[i].substring(start + 1, end));
                list.add(node);
                ffList.add(ffs[i].trim());
            }
        }

        sqlConfig.setGroup(list);
        sqlConfig.setFfList(ffList);
    }

    private static void parseTableName(SqlConfig sqlConfig) {
        Matcher om = Pattern.compile(sqlTablePattern).matcher(sqlConfig.getSql());
        while (om.find()) {
            String name = om.group(2);
            name = name.replaceAll("`", "");
            if (name.indexOf(".") > 0)
                name = name.substring(name.indexOf(".") + 1);
            sqlConfig.setTableNameList(name);
        }
    }

    /**
     * 根据id获取sqlconfig
     *
     * @param sqlId
     * @return SqlConfig
     */
    public static SqlConfig getSqlConfig(String sqlId) {
        if (sqlConfigMap.containsKey(sqlId))
            return sqlConfigMap.get(sqlId);
        logger.warn("找不到" + sqlId + "的sqlConfig");
//        logger.error("找不到" + sqlId + "的sqlConfig", new SqlConfigNotExistException("找不到" + sqlId + "的sqlConfig"));
        return null;
    }

    /**
     * 添加sqlconfig到缓存
     *
     * @param config
     */
    public static void addSqlConfig(SqlConfig config) {
        logger.info(config.getId() + "配置如下");
        logger.info(JsonUtils.toJson(config));
        if (sqlConfigMap.containsKey(config.getId()))
            logger.warn("sqlConfig " + config.getId() + " 被覆盖了");
        sqlConfigMap.put(config.getId(), config);
    }

    /**
     * 扫描配置文件，生成sqlconfig
     *
     * @param mapperLocations
     * @throws Exception
     */
    public static void scan(String... mapperLocations) throws Exception {
        for (String mapperLocation : mapperLocations) {
            logger.trace("开始解析" + mapperLocation);
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources(mapperLocation);
            if (resources != null) {
                for (Resource resource : resources) {
                    if (!Hotload.canReload(resource)) continue;
                    logger.info(resource.getURL().toString());
                    Element root = XmlUtils.getRootElement(resource.getInputStream());
                    Map<String, String> parentAttrs = getAttributes(root);
                    if (!parentAttrs.containsKey("nameSpace"))
                        throw new Exception("配置文件的nameSpace不能为空");
                    if (parentAttrs.containsKey("dbName")) {
                        parentAttrs.put("parentDbName", parentAttrs.get("dbName"));
                        parentAttrs.remove("dbName");
                    }
                    for (Node element : XmlUtils.getChildNodes(root)) {
                        addSqlConfig(createSqlConfig(element, parentAttrs));
                    }
                }
            } else {
                throw new Exception("SqlConfigManager 初始化错误：" + mapperLocation);
            }
            logger.trace(mapperLocation + "解析完毕");
        }
    }

    /**
     * 根据xml的节点，生成sqlconfig
     *
     * @param element
     * @param parentAttrs
     * @return
     * @throws Exception
     */
    private static SqlConfig createSqlConfig(Node element, Map<String, String> parentAttrs) throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.putAll(parentAttrs);
        map.putAll(getAttributes(element));
        if (!map.containsKey("id"))
            throw new Exception("sqlConfig 的 id 不能为空");
        map.put("id", map.get("nameSpace") + "." + map.get("id"));
        Node sql = XmlUtils.getChildNode((Element) element, "sql");
        if (sql != null)
            map.put("sql", XmlUtils.getValue(sql));
        String returnType = XmlUtils.getAttributeValue(XmlUtils.getChildNode((Element) element, "returnType"), "class");
        if (returnType != null)
            map.put("returnType", returnType);
        SqlConfig sqlConfig = JsonUtils.convert(map, SqlConfig.class);

        if (map.containsKey("ref")) {
            SqlConfig ref = getSqlConfig(map.get("nameSpace") + "." + map.get("ref"));
            if (ref == null)
                throw new RuntimeException(map.get("nameSpace") + "." + map.get("ref") + ":不存在");
            Map refMap = JsonUtils.convert(ref, Map.class);
            refMap.putAll(JsonUtils.convert(sqlConfig, Map.class));
            sqlConfig = JsonUtils.convert(refMap, SqlConfig.class);
        }
        //如果ReturnType 不为空，则用TableStruct处理结果集
        if (sqlConfig.getSql().matches(isSelectPattern) && sqlConfig.getReturnType() != null) {
            sqlConfig.setFunIntercepts("all", TableStruct.instance);
        }
        /**
         * 处理funIntercepts
         */
        Node subNode = XmlUtils.getChildNode((Element) element, "paramIntercepts");
        if (subNode != null) {
            Map<String, String> _map;
            for (Node node : XmlUtils.getChildNodes(subNode)) {
                _map = XmlUtils.getAttributes(node);
                try {
                    sqlConfig.setParamIntercepts(_map.get("key"), (IFunIntercept) Class.forName(_map.get("class")).newInstance());
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        /**
         * 处理funIntercepts
         */
        subNode = XmlUtils.getChildNode((Element) element, "funIntercepts");
        if (subNode != null) {
            Map<String, String> _map;
            for (Node node : XmlUtils.getChildNodes(subNode)) {
                _map = XmlUtils.getAttributes(node);
                try {
                    sqlConfig.setFunIntercepts(_map.get("key"), (IFunIntercept) Class.forName(_map.get("class")).newInstance());
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        /**
         * 处理sqlIntercepts
         */
        subNode = XmlUtils.getChildNode((Element) element, "sqlIntercepts");
        if (subNode != null) {
            int index = 0;
            for (Node node : XmlUtils.getChildNodes(subNode)) {
                if (XmlUtils.getAttributeValue(node, "id") == null) {
                    Element _el = (Element) node;
                    _el.setAttribute("id", sqlConfig.getId() + "_" + index);
                    index++;
                }
                SqlConfig config = createSqlConfig(node, parentAttrs);
                config.setParentId(sqlConfig.getId());
                sqlConfig.setSqlIntercepts(config);
            }
        }
        /**
         * 处理 rowCallbacks
         */
        subNode = XmlUtils.getChildNode((Element) element, "rowCallbacks");
        if (subNode != null) {
            Map<String, String> _map;
            for (Node node : XmlUtils.getChildNodes(subNode)) {
                _map = XmlUtils.getAttributes(node);
                try {
                    sqlConfig.setRowCallbacks((IRowCallback) Class.forName(_map.get("class")).newInstance());
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        parseSql(sqlConfig);

        return sqlConfig;
    }

    public static Matcher getRealMatcher(Matcher om) {
        String field = om.group(0);
        if (Strings.isNullOrEmpty(field)) return om;
        String up = field.toUpperCase();
        String[] keys = {" WHERE ", " AND ", " OR "};
        boolean ismod = false;
        for (int i = 0; i < keys.length; i++) {
            int pos = up.lastIndexOf(keys[i]);
            if (pos > -1) {
                up = up.substring(pos + keys[i].length() - 1);
                field = field.substring(pos + keys[i].length() - 1);
                ismod = true;
            }
        }
        if (ismod) {
            Matcher m = Pattern.compile(sqlSelectPattern).matcher(field);
            if (m.find())
                return m;
        }

        return om;
    }
}

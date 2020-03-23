package qeorm;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import qeorm.intercept.IFunIntercept;
import qeorm.intercept.IRowCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Created by ashen on 2017-2-3.
 */
public class SqlConfig {
    public final static String RESULTASINT = "resultAsInt";
    public final static String INSERT = "insert";
    public final static String SELECT = "select";
    public final static String COUNT = "count";
    public final static String UPDATE = "update";
    public final static String DELETE = "delete";
    public final static String CURSOR = "cursor";
    private String extend;
    protected String relationKey;
    protected String fillKey;

    private String primaryKey;
    private String parentId;
    private String id;
    private String dbName;
    private String paramNode;
    private String proxy;
    private boolean isCache = false;
    private boolean isTran = false;
    private String sql;
    private String returnType;
    private List<Pair<String, IFunIntercept>> funIntercepts;
    private List<Pair<String, IFunIntercept>> paramIntercepts;
    private List<SqlConfig> sqlIntercepts;
    private List<IRowCallback> rowCallbacks;

    private String refId;
    private boolean autoPage;
    private String tableName;
    private List<String> tableNameList;
    private String canEmptyParams = "";
    private boolean isSelect;

    private Matcher matcher;
    private List<SqlAnalysisNode> group;
    private List<SqlAndOrNode> andOrNodes;
    private List<String> ffList;
    private Class klass;
    private boolean isPrimitive = false;
    String sqlType;
    String parentDbName;

    public List<SqlAndOrNode> getAndOrNodes() {
        if (andOrNodes == null) return new ArrayList<SqlAndOrNode>();
        return andOrNodes;
    }

    public void setAndOrNodes(List<SqlAndOrNode> andOrNodes) {
        this.andOrNodes = andOrNodes;
    }

    public List<String> getFfList() {
        return ffList;
    }

    public void setFfList(List<String> ffList) {
        this.ffList = ffList;
    }

    public String getParentDbName() {
        return parentDbName;
    }

    public void setParentDbName(String parentDbName) {
        this.parentDbName = parentDbName;
    }

    public Class getKlass() {
        return klass;
    }

    public String getSqlType() {
        return sqlType;
    }

    public void setSqlType(String sqlType) {
        this.sqlType = sqlType;
    }

    public List<SqlAnalysisNode> getGroup() {
        if (group == null)
            group = new ArrayList<SqlAnalysisNode>();
        return group;
    }

    public void setGroup(List<SqlAnalysisNode> group) {
        this.group = group;
    }

    public Matcher getMatcher() {
        return matcher;
    }

    public void setMatcher(Matcher matcher) {
        this.matcher = matcher;
    }

    public void setCanEmptyParams(String canEmptyParams) {
        if (canEmptyParams == null) return;
        this.canEmptyParams = "," + canEmptyParams + ",";
    }

    public boolean canEmpty(String param) {
        if (Strings.isNullOrEmpty(param)) return false;
        return canEmptyParams.contains("," + param + ",");
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setIsSelect(boolean isSelect) {
        this.isSelect = isSelect;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        if (tableName == null) return;
        this.tableName = tableName.replaceAll("`", "").replaceAll("'", "");
        if (!Strings.isNullOrEmpty(this.tableName))
            this.setTableNameList(this.tableName);
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }

    public boolean isAutoPage() {
        return autoPage;
    }

    public void setAutoPage(boolean autoPage) {
        this.autoPage = autoPage;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public String getRelationKey() {
        return relationKey;
    }

    public void setRelationKey(String relationKey) {
        this.relationKey = relationKey;
    }

    public String getFillKey() {
        return fillKey;
    }

    public void setFillKey(String fillKey) {
        this.fillKey = fillKey;
    }

    public List<SqlConfig> getSqlIntercepts() {
        if (sqlIntercepts == null)
            sqlIntercepts = new ArrayList<SqlConfig>();
        return sqlIntercepts;
    }

    public void setSqlIntercepts(SqlConfig sqlConfig) {
        if (sqlConfig == null) return;
        getSqlIntercepts().add(sqlConfig);
    }


    public List<IRowCallback> getRowCallbacks() {
        if (rowCallbacks == null)
            rowCallbacks = Lists.newArrayList();
        return rowCallbacks;
    }

    public void setRowCallbacks(IRowCallback rowCallback) {
        if (rowCallback == null) return;
        getRowCallbacks().add(rowCallback);
    }

    public SqlConfig() {
        canEmptyParams = "";
    }

    public SqlConfig(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDbName() {
        return CacheManager.instance.getRealDbName(dbName, getTableNameList());
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getParamNode() {
        return paramNode;
    }

    public void setParamNode(String paramNode) {
        this.paramNode = paramNode;
    }

    public boolean isCache() {
        return isCache;
    }

    public void setIsCache(boolean isCache) {
        this.isCache = isCache;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }

    public boolean isTran() {
        return isTran;
    }

    public void setIsTran(boolean isTran) {
        this.isTran = isTran;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        if (returnType == null) return;
        this.klass = RealClass.getRealClass(returnType);
        this.returnType = this.klass.getName();
        this.isPrimitive = RealClass.isPrimitive(this.klass);
    }

    public boolean isPrimitive() {
        return this.isPrimitive;
    }

    public List<Pair<String, IFunIntercept>> getFunIntercepts() {
        if (funIntercepts == null)
            funIntercepts = Lists.newArrayList();
        return funIntercepts;
    }

    public void setFunIntercepts(String key, IFunIntercept clz) {
        if (key == null || clz == null) return;
        getFunIntercepts().add(new MutablePair<>(key, clz));
    }

    public List<Pair<String, IFunIntercept>> getParamIntercepts() {
        if (paramIntercepts == null)
            paramIntercepts = Lists.newArrayList();
        return paramIntercepts;
    }

    public void setParamIntercepts(String key, IFunIntercept clz) {
        if (key == null || clz == null) return;
        getParamIntercepts().add(new MutablePair<>(key, clz));
    }

    public void setTableNameList(String tableName) {
        if (tableNameList == null) tableNameList = new ArrayList<>();
        tableNameList.add(tableName);
    }

    public List<String> getTableNameList() {
        if (tableNameList == null) tableNameList = new ArrayList<>();
        return tableNameList;
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }
}

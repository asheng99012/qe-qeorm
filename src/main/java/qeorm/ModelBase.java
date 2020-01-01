package qeorm;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import com.google.common.base.Splitter;
import org.springframework.beans.BeanUtils;
import org.springframework.cglib.beans.BeanMap;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import qeorm.annotation.Transient;
import qeorm.intercept.IFunIntercept;
import qeorm.utils.ExtendUtils;
import qeorm.utils.JsonUtils;

/**
 * Created by asheng on 2015/7/20 0020.
 */
public class ModelBase implements IFunIntercept, Serializable, Cloneable {

    @Transient
    private static final long serialVersionUID = 1L;

    // @Transient
    // private Logger logger = LoggerFactory.getLogger(getClass());
    @Transient
    public final static String DBNULL = "DBNULL";

    @Transient
    private static Map<String, Field[]> fieldMap = new HashMap<String, Field[]>();

    @Transient
    private Integer pn;

    @Transient
    private Integer ps;

    @Transient
    private Boolean needCount;

    @Transient
    private Boolean withRelation;

    @Transient
    private Boolean _ignoreNull = true;

    @Transient
    private Set<String> nullSet;

    public void ignoreNull() {
        _ignoreNull = true;
    }

    public void notIgnoreNull() {
        _ignoreNull = false;
    }

    public Boolean isWithRelation() {
        return withRelation;
    }

    public ModelBase() {
        nullSet = new HashSet<>();
    }

    public void addKeyNullSet(String key) {
        nullSet.add(key);
    }

    public void removeKeyNullSet(String key) {
        if (nullSet.contains(key)) nullSet.remove(key);
    }

    public Integer getPn() {
        return pn;
    }

    public void setPn(Integer pn) {
        this.pn = pn;
    }

    public Integer getPs() {
        if (ps == null)
            return 50;
        return ps;
    }

    public void setPs(Integer ps) {
        this.ps = ps;
    }

    public Boolean getNeedCount() {
        return needCount;
    }

    public void setNeedCount(Boolean needCount) {
        this.needCount = needCount;
    }


    public int insert() {
        return SqlExecutor.insert(this);
    }

    public int update() {
        return SqlExecutor.update(this);
    }

    public int save() {
        return SqlExecutor.save(this);
    }

    public int insert2() {
        Object o = exec(SqlConfig.INSERT);
        if (o == null)
            return 0;
        return Integer.parseInt(o.toString());
    }

    public int update2() {
        Object o = exec(SqlConfig.UPDATE);
        if (o == null)
            return 0;
        return Integer.parseInt(o.toString());
    }

    public int save2() {
        TableStruct table = TableStruct.getTableStruct(this.getClass().getName());
        String key = table.getPrimaryField();
        Map map = JsonUtils.convert(this, Map.class);
        if (map.containsKey(key))
            return update2();
        return insert2();
    }

    public long insertLong() {
        Object o = exec(SqlConfig.INSERT);
        if (o == null)
            return 0L;
        return Long.parseLong(o.toString());
    }

    public <T> List<T> selectWithrelation() {
        this.withRelation = true;
        Object o = exec(SqlConfig.SELECT);
        this.withRelation = null;
        if (o == null)
            return new ArrayList<T>();
        return (List<T>) o;
    }

    public <T> T selectOneWithrelation() {
        List<T> list = selectWithrelation();
        if (null == list || list.size() == 0)
            return null;
        return list.get(0);
    }

    public <T> T selectOneNotWithrelation() {
        List<T> list = select();
        if (null == list || list.size() == 0)
            return null;
        return list.get(0);
    }

    public <T> List<T> select() {
        this.withRelation = false;
        Object o = exec(SqlConfig.SELECT);
        this.withRelation = null;
        if (o == null)
            return new ArrayList<T>();
        return (List<T>) o;
    }

    public int count() {
        Object o = exec(SqlConfig.COUNT);
        if (o == null)
            return 0;
        return Integer.parseInt(o.toString());
    }

    public <T> T selectOne() {
        this.pn = 1;
        this.ps = 1;
        List<T> list = select();
        if (null == list || list.size() == 0)
            return null;
        return list.get(0);
    }

    public int delete() {
        Object o = exec(SqlConfig.DELETE);
        if (o == null)
            return 0;
        return Integer.parseInt(o.toString());
    }

    public <T> T exec(String sqlIndex) {
        createSqlConfig(sqlIndex);
        // if (sqlIndex.equals(SqlConfig.INSERT))
        // this.interceptInsert();
        // if (sqlIndex.equals(SqlConfig.UPDATE))
        // this.interceptUpdate();
        // if (sqlIndex.equals(SqlConfig.SELECT) ||
        // sqlIndex.equals(SqlConfig.COUNT))
        // this.interceptSelect();
        Map<String, Object> params;
        if (_ignoreNull) params = JsonUtils.convert(this, Map.class);
        else params = JsonUtils.convert(JsonUtils.toJsonWriteNull(this), Map.class);
        for (String str : nullSet) {
            params.put(str, null);
        }
        SqlResult sqlResult = SqlExecutor.exec(sqlIndexId(sqlIndex), params);
        if (sqlResult.isOk())
            return (T) sqlResult.getResult();
        return null;
    }


    public Map fetchRealVal() {
        Set json = new HashSet();
        json.add("pn");
        json.add("ps");
        json.add("_ignoreNull");
        json.add("withRelation");
        json.add("nullSet");
        json.add("needCount");


        Map<String, Object> params = BeanMap.create(this);
        TableStruct table = TableStruct.getTableStruct(this.getClass().getName());
        Map data = new HashMap();
        if (table != null) {
            for (TableColumn tc : table.getTableColumnList()) {
                String key = tc.getFiledName();
                if (!json.contains(key) && (params.get(key) != null || nullSet.contains(key))) {
                    data.put(tc.getClumnName(), params.get(key));
                }
            }
        }
        return data;
    }

    public void interceptInsert(SqlConfig sqlConfig) {

    }

    public void interceptSelect(SqlConfig sqlConfig) {

    }

    public void interceptUpdate(SqlConfig sqlConfig) {

    }

    protected String sqlIndexId(String action) {
        return getClass().getName() + "." + action;
    }

    protected boolean primaryKeyIntoDb() {
        return false;
    }

    protected SqlConfig createSqlConfig(String type) {
        String sqlId = sqlIndexId(type);
        if (SqlConfigManager.getSqlConfig(sqlId) == null) {
            TableStruct table = TableStruct.getTableStruct(getClass().getName());
            SqlConfig sqlConfig = new SqlConfig();
            sqlConfig.setId(sqlId);
            sqlConfig.setTableName(table.getTableName());
            if (SqlConfig.INSERT.equals(type)) {
                List<String> ff = Lists.newArrayList();
                List<String> vv = Lists.newArrayList();
                for (TableColumn tc : table.getTableColumnList()) {
                    if (primaryKeyIntoDb() && tc.getClumnName().equals(table.getPrimaryKey()))
                        continue;
                    ff.add("`" + tc.getClumnName() + "`");
                    vv.add("{" + tc.getFiledName() + "}");
                }
                String sql = StringFormat.format("insert into `{0}` ({1}) values({2})", table.getTableName(),
                        Joiner.on(",").join(ff), Joiner.on(",").join(vv));
                interceptInsert(sqlConfig);
                sqlConfig.setSql(sql);
                sqlConfig.setDbName(table.getMasterDbName());
                if (!Strings.isNullOrEmpty(table.getPrimaryKey()))
                    sqlConfig.setPrimaryKey(table.getPrimaryKey());
            }

            if (SqlConfig.SELECT.equals(type)) {
                sqlConfig.setSql(
                        StringFormat.format("select * from `{0}` where {1}", table.getTableName(), table.getWhere()));
                sqlConfig.setDbName(table.getSlaveDbName());
                sqlConfig.setReturnType(getClass().getName());
                sqlConfig.setFunIntercepts("all", TableStruct.instance);
                sqlConfig.setFunIntercepts("all", BeanUtils.instantiate(getClass()));
                interceptSelect(sqlConfig);
                if (!table.getRelationStructList().isEmpty()) {
                    for (RelationStruct rs : table.getRelationStructList()) {
                        TableStruct _table = TableStruct.getTableStruct(rs.getClazz().getName());
                        SqlConfig sc = new SqlConfig();
                        sc.setTableName(_table.getTableName());
                        sc.setDbName(_table.getMasterDbName());
                        sc.setReturnType(rs.getClazz().getName());
                        sc.setId(sqlConfig.getId() + "." + rs.getFillKey());
                        sc.setFunIntercepts(null, TableStruct.instance);
                        sc.setFunIntercepts(null, (IFunIntercept) BeanUtils.instantiate(rs.getClazz()));
                        sc.setSql(StringFormat.format("select * from `{0}` where {1}", _table.getTableName(),
                                rs.getWhere()));
                        sc.setRelationKey(rs.getRelationKey());
                        sc.setFillKey(rs.getFillKey());
                        sc.setExtend(rs.getExtend());
                        SqlConfigManager.parseSql(sc);
                        sqlConfig.setSqlIntercepts(sc);
                    }
                }
            }

            if (SqlConfig.COUNT.equals(type)) {
                interceptSelect(sqlConfig);
                sqlConfig.setSql(StringFormat.format("select count(*) from `{0}` where {1}", table.getTableName(),
                        table.getWhere()));
                sqlConfig.setDbName(table.getSlaveDbName());
            }

            if (SqlConfig.DELETE.equals(type)) {
                interceptSelect(sqlConfig);
                sqlConfig.setSql(
                        StringFormat.format("delete from `{0}` where {1}", table.getTableName(), table.getWhere()));
                sqlConfig.setDbName(table.getMasterDbName());
            }

            if (SqlConfig.UPDATE.equals(type)) {
                interceptUpdate(sqlConfig);
                List<String> up = new ArrayList<String>();
                for (TableColumn tc : table.getTableColumnList()) {
                    if (tc.getClumnName().equals(table.getPrimaryKey()))
                        continue;
                    up.add("`" + tc.getClumnName() + "`={" + tc.getFiledName() + "}");
                }
                sqlConfig.setSql(StringFormat.format("update `{0}` set {1} where {2}={{3}}", table.getTableName(),
                        Joiner.on(",").join(up), table.getPrimaryKey(), table.getPrimaryField()));
                sqlConfig.setDbName(table.getMasterDbName());
            }

            SqlConfigManager.parseSql(sqlConfig);
            SqlConfigManager.addSqlConfig(sqlConfig);
            // logger.info(sqlId + "======" + sqlConfig.getSql());
        }
        return SqlConfigManager.getSqlConfig(sqlId);
    }

    @Override
    public void intercept(String key, Map<String, Object> rowData, SqlResult sqlResult) {

    }

    public <T> T enhance() {
        return (T) enhance(false);
    }

    public <T> T enhance(boolean deep) {
        return (T) new EnhanceModel().createProxy(this, deep);
    }

    public static class EnhanceModel<T extends ModelBase> implements MethodInterceptor {
        private static Map<String, Field> fieldMap = new HashMap<>();
        //要代理的原始对象
        private T target;
        private boolean deep;

        public T createProxy(T target, boolean deep) {
            this.target = target;
            this.deep = deep;
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(this.target.getClass());
            enhancer.setCallback(this);
            enhancer.setClassLoader(target.getClass().getClassLoader());
            return (T) enhancer.create();
        }

        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            if (target == null) return null;
            Object result = methodProxy.invoke(target, objects);
            if (method.getName().startsWith("set")) {
                String filedName = method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4);
                Object val = objects[0];
                if (val == null) target.addKeyNullSet(filedName);
                else target.removeKeyNullSet(filedName);
            }
            if (result == null && method.getName().startsWith("get")) {
                String filedName = method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4);
                String id = target.sqlIndexId(SqlConfig.SELECT) + "." + filedName;
                SqlConfig sqlConfig = target.createSqlConfig(SqlConfig.SELECT).getSqlIntercepts()
                        .stream().filter(config -> config.getId().equals(id)).findAny().get();
                if (sqlConfig != null) {
                    String key = Splitter.on("|").splitToList(sqlConfig.getRelationKey()).get(0);
                    String byFiled = TableStruct.getRealMappBy(target, key);
                    Map data = JsonUtils.convert(target, Map.class);
                    if (data.containsKey(byFiled)) {
                        Map<String, Object> params = new HashMap<>();
                        params.put(key, data.get(byFiled));
                        SqlResult sqlResult = SqlExecutor.exec(sqlConfig, params);
                        if (sqlResult.isOk()) {
                            List list = (List) sqlResult.getResult();
                            if (list.size() > 0) {
                                List _list = new ArrayList();
                                for (int i = 0; i < list.size(); i++) {
                                    _list.add(enhance(list.get(i)));
                                }
                                if (sqlConfig.getExtend().equals(ExtendUtils.ONE2ONE)) {
                                    result = _list.get(0);
                                } else {
                                    result = _list;
                                }
                                getTargetField(filedName).set(target, result);
                            }

                        }
                    }
                }
            }


            return result;
        }

        private Object enhance(Object obj) {
            if (deep && obj instanceof ModelBase)
                return ((ModelBase) obj).enhance();
            return obj;
        }

        private Field getTargetField(String filedName) throws NoSuchFieldException {
            String key = target.getClass() + "." + filedName;
            if (!fieldMap.containsKey(key)) {
                Field field = target.getClass().getDeclaredField(filedName);
                field.setAccessible(true);
                fieldMap.put(key, field);
            }
            return fieldMap.get(key);
        }
    }
}

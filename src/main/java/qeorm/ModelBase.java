package qeorm;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.cglib.beans.BeanMap;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import qeorm.annotation.Transient;
import qeorm.intercept.IFunIntercept;

/**
 * Created by asheng on 2015/7/20 0020.
 */
public class ModelBase implements IFunIntercept {
    @Transient
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Transient
    public final static String DBNULL = "DBNULL";

    @Transient
    private static Map<String, Field[]> fieldMap = new HashMap<String, Field[]>();

    @Transient
    private Integer pn;
    @Transient
    private Integer ps;


    @Transient
    private Boolean withRelation ;
    public Boolean isWithRelation() {
        return withRelation;
    }

    public ModelBase() {
    }

    public Integer getPn() {
        return pn;
    }

    public void setPn(Integer pn) {
        this.pn = pn;
    }

    public Integer getPs() {
        if (ps == null ) return 50;
        return ps;
    }

    public void setPs(Integer ps) {
        this.ps = ps;
    }

    public int save() {
        TableStruct table = TableStruct.getTableStruct(this.getClass().getName());
        String key = table.getPrimaryField();
        BeanMap map = BeanMap.create(this);
        if (map.containsKey(key)) return update();
        return insert();
    }

    public int insert() {
        Object o = exec(SqlConfig.INSERT);
        if (o == null) return 0;
        return Integer.parseInt(o.toString());
    }

    public <T> List<T> selectWithrelation() {
        this.withRelation = true;
        Object o = exec(SqlConfig.SELECT);
        this.withRelation=null;
        if (o == null) return new ArrayList<T>();
        return (List<T>) o;
    }

    public <T> T selectOneWithrelation() {
        List<T> list = selectWithrelation();
        if (null == list || list.size() == 0) return null;
        return list.get(0);
    }
    
    public <T> T selectOneNotWithrelation() {
        List<T> list = select();
        if (null == list || list.size() == 0) return null;
        return list.get(0);
    }

    public <T> List<T> select() {
        this.withRelation = false;
        Object o = exec(SqlConfig.SELECT);
        this.withRelation=null;
        if (o == null) return new ArrayList<T>();
        return (List<T>) o;
    }

    public int count() {
        Object o = exec(SqlConfig.COUNT);
        if (o == null) return 0;
        return Integer.parseInt(o.toString());
    }

    public <T> T selectOne() {
        List<T> list = select();
        if (null == list || list.size() == 0) return null;
        return list.get(0);
    }

    public int update() {
        Object o = exec(SqlConfig.UPDATE);
        if (o == null) return 0;
        return Integer.parseInt(o.toString());
    }

    public int delete() {
        Object o = exec(SqlConfig.DELETE);
        if (o == null) return 0;
        return Integer.parseInt(o.toString());
    }

    public <T> T exec(String sqlIndex) {
        createSqlConfig(sqlIndex);
//        if (sqlIndex.equals(SqlConfig.INSERT))
//            this.interceptInsert();
//        if (sqlIndex.equals(SqlConfig.UPDATE))
//            this.interceptUpdate();
//        if (sqlIndex.equals(SqlConfig.SELECT) || sqlIndex.equals(SqlConfig.COUNT))
//            this.interceptSelect();
        SqlResult sqlResult = SqlExecutor.exec(sqlIndexId(sqlIndex), this);
        if (sqlResult.isOk())
            return (T) sqlResult.getResult();
        return null;
    }


    public void interceptInsert(SqlConfig sqlConfig) {

    }

    public void interceptSelect(SqlConfig sqlConfig) {

    }

    public void interceptUpdate(SqlConfig sqlConfig) {

    }

    private String sqlIndexId(String action) {
        return getClass().getName() + "." + action;
    }


    private SqlConfig createSqlConfig(String type) {
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
                    if (tc.getClumnName().equals(table.getPrimaryKey())) continue;
                    ff.add("`" + tc.getClumnName() + "`");
                    vv.add("{" + tc.getFiledName() + "}");
                }
                String sql = StringFormat.format("insert into `{0}` ({1}) values({2})",
                        table.getTableName(),
                        Joiner.on(",").join(ff),
                        Joiner.on(",").join(vv)
                );
                interceptInsert(sqlConfig);
                sqlConfig.setSql(sql);
                sqlConfig.setDbName(table.getMasterDbName());
                if (!Strings.isNullOrEmpty(table.getPrimaryKey())) sqlConfig.setPrimaryKey(table.getPrimaryKey());
            }

            if (SqlConfig.SELECT.equals(type)) {
                sqlConfig.setSql(StringFormat.format("select * from `{0}` where {1}", table.getTableName(), table.getWhere()));
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
                        sc.setId(rs.getClazz().getName() + "." + getClass().getSimpleName() + "." + rs.getRelationKey() + ".select");
                        sc.setFunIntercepts(null, TableStruct.instance);
                        sc.setFunIntercepts(null, (IFunIntercept) BeanUtils.instantiate(rs.getClazz()));
                        sc.setSql(StringFormat.format("select * from `{0}` where {1}", _table.getTableName(), rs.getWhere()));
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
                sqlConfig.setSql(StringFormat.format("select count(1) from `{0}` where {1}", table.getTableName(), table.getWhere()));
                sqlConfig.setDbName(table.getSlaveDbName());
            }

            if (SqlConfig.DELETE.equals(type)) {
                interceptSelect(sqlConfig);
                sqlConfig.setSql(StringFormat.format("delete from `{0}` where {1}", table.getTableName(), table.getWhere()));
                sqlConfig.setDbName(table.getMasterDbName());
            }

            if (SqlConfig.UPDATE.equals(type)) {
                interceptUpdate(sqlConfig);
                List<String> up = new ArrayList<String>();
                for (TableColumn tc : table.getTableColumnList()) {
                    if (tc.getClumnName().equals(table.getPrimaryKey())) continue;
                    up.add("`" + tc.getClumnName() + "`={" + tc.getFiledName() + "}");
                }
                sqlConfig.setSql(StringFormat.format("update `{0}` set {1} where {2}={{3}}",
                        table.getTableName(), Joiner.on(",").join(up), table.getPrimaryKey(), table.getPrimaryField()));
                sqlConfig.setDbName(table.getMasterDbName());
            }

            SqlConfigManager.parseSql(sqlConfig);
            SqlConfigManager.addSqlConfig(sqlConfig);
            logger.info(sqlId + "======" + sqlConfig.getSql());
        }
        return SqlConfigManager.getSqlConfig(sqlId);
    }

    public void intercept(String key, Map<String, Object> rowData, SqlResult sqlResult) {

    }
}

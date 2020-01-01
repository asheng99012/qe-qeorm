package qeorm;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import qeorm.annotation.*;
import qeorm.intercept.IFunIntercept;
import qeorm.utils.ExtendUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ashen on 2017-2-3.
 */
public class TableStruct implements IFunIntercept {
    public static TableStruct instance = new TableStruct();
    private String masterDbName;
    private String slaveDbName;
    private String primaryKey;
    private String primaryField;
    private String tableName;
    private String where;
    private List<TableColumn> tableColumnList;
    private List<RelationStruct> relationStructList;
    private boolean isMapped = false;
    static Map<String, TableStruct> TableStructMap = new HashMap<String, TableStruct>();
    private Map<String, String> fcMap = new HashMap<String, String>();
    private String className;
    private boolean first = true;

    private static TableStruct getTableStruct(String key, boolean first) {
        if (!TableStructMap.containsKey(key)) {
            try {
                TableStruct table = new TableStruct(Class.forName(key), first);
                TableStructMap.put(key, table);
                return table;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return TableStructMap.get(key);
    }

    public static TableStruct getTableStruct(String key) {
        return getTableStruct(key, true);
    }


    public TableStruct() {
    }

    private TableStruct(Class klazz) {
        this(klazz, true);
    }

    private TableStruct(Class klazz, boolean first) {
        this.first = first;
        className = klazz.getName();
        Table table = (Table) klazz.getAnnotation(Table.class);
        if (table == null) {
            table = (Table) klazz.getSuperclass().getAnnotation(Table.class);
        }
        if (table != null) {
            primaryKey = StringFormat.format(table.primaryKey());
            tableName = StringFormat.format(table.tableName());
            where = table.where();
            slaveDbName = StringFormat.format(table.slaveDbName());
            masterDbName = StringFormat.format(table.masterDbName());
        }

        isMapped = false;
        if (Strings.isNullOrEmpty(tableName)) tableName = klazz.getSimpleName();
        tableColumnList = new ArrayList<TableColumn>();
        relationStructList = new ArrayList<RelationStruct>();
        Field[] field;
//        field = klazz.getDeclaredFields();
//        field=klazz.getFields();
        field = getFields(klazz);
        Map<Field, Object> relation = new HashMap<Field, Object>();
        for (int i = 0; i < field.length; i++) {
            Transient _t = field[i].getAnnotation(Transient.class);
            if (_t == null) {
                OneToOne ones = field[i].getAnnotation(OneToOne.class);
                if (ones != null) {
                    relation.put(field[i], ones);
                    continue;
                }
                OneToMany oneToMany = field[i].getAnnotation(OneToMany.class);
                if (oneToMany != null) {
                    relation.put(field[i], oneToMany);
                    continue;
                }
                Column column = field[i].getAnnotation(Column.class);
                String columnName = (column != null && !Strings.isNullOrEmpty(column.value())) ? column.value() : field[i].getName();
                if (!Strings.isNullOrEmpty(primaryKey) && primaryKey.equals(columnName))
                    primaryField = field[i].getName();
                fcMap.put(field[i].getName(), columnName);
                TableColumn tableColumn = new TableColumn();
                tableColumn.setClumnName(columnName);
                tableColumn.setFiledName(field[i].getName());
                tableColumn.setType(field[i].getType());
                tableColumnList.add(tableColumn);
                if (!tableColumn.getClumnName().equals(tableColumn.getFiledName())) isMapped = true;
            }
        }
        TableStructMap.put(className, this);
        dealRelation(relation);
    }

    private Field[] getFields(Class klass) {
        Field[] fs = klass.getDeclaredFields();
        Field[] _fs = klass.getFields();
        Map<String, Boolean> map = Maps.newHashMap();
        List<Field> list = Lists.newArrayList();
        int i = 0;
        for (i = 0; i < fs.length; i++) {
            map.put(fs[i].getName(), true);
            list.add(fs[i]);
        }
        for (i = 0; i < _fs.length; i++) {
            if (map.containsKey(_fs[i].getName())) continue;
            map.put(_fs[i].getName(), true);
            list.add(_fs[i]);
        }
        return Iterables.toArray(list, Field.class);
    }

    private void dealRelation(Map<Field, Object> relation) {
        Object[] fields = relation.keySet().toArray();
        for (Object _field : fields) {
            Field field = (Field) _field;
            Object obj = relation.get(_field);


            if (obj instanceof OneToOne) {
                boolean isSame = field.getType().getName().equals(className);
                if (!first && isSame) continue;
                OneToOne ones = (OneToOne) obj;
                TableStruct tableStruct = TableStruct.getTableStruct(field.getType().getName(), !isSame);
                RelationStruct relationStruct = new RelationStruct();
                relationStruct.setRelationKey(fcMap.get(ones.self()) + "|" + tableStruct.getFcMap().get(ones.mappedBy()));
                relationStruct.setFillKey(field.getName());
                relationStruct.setExtend(ExtendUtils.ONE2ONE);
                relationStruct.setClazz(field.getType());
                relationStruct.setWhere(" `" + tableStruct.getFcMap().get(ones.mappedBy()) + "` in ({" + fcMap.get(ones.self()) + "}) " + ones.suffix());
                relationStructList.add(relationStruct);
            } else {
                OneToMany oneToMany = (OneToMany) obj;
                ParameterizedType pt = (ParameterizedType) field.getGenericType();
                RelationStruct relationStruct = new RelationStruct();
                relationStruct.setClazz((Class) pt.getActualTypeArguments()[0]);
                boolean isSame = relationStruct.getClazz().getName().equals(className);
                if (!first && isSame) continue;
                TableStruct tableStruct = TableStruct.getTableStruct(relationStruct.getClazz().getName(), !isSame);
                relationStruct.setRelationKey(fcMap.get(oneToMany.self()) + "|" + tableStruct.getFcMap().get(oneToMany.mappedBy()));
                relationStruct.setFillKey(field.getName());
                relationStruct.setExtend(ExtendUtils.ONE2MANY);
                relationStruct.setWhere(" `" + tableStruct.getFcMap().get(oneToMany.mappedBy()) + "` in ({" + fcMap.get(oneToMany.self()) + "}) " + oneToMany.suffix());
                relationStructList.add(relationStruct);
            }
        }
    }


    public List<RelationStruct> getRelationStructList() {
        return relationStructList;
    }

    public void setRelationStructList(List<RelationStruct> relationStructList) {
        this.relationStructList = relationStructList;
    }

    public boolean isMapped() {
        return isMapped;
    }

    public void setIsMapped(boolean isMapped) {
        this.isMapped = isMapped;
    }


    public String getPrimaryField() {
        return primaryField;
    }

    public void setPrimaryField(String primaryField) {
        this.primaryField = primaryField;
    }

    public String getMasterDbName() {
        return masterDbName;
    }

    public void setMasterDbName(String masterDbName) {
        this.masterDbName = masterDbName;
    }

    public String getSlaveDbName() {
        return slaveDbName;
    }

    public void setSlaveDbName(String slaveDbName) {
        this.slaveDbName = slaveDbName;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getWhere() {
        if (where == null || where.trim().equals(""))
            where = " " + getPrimaryKey() + "={" + getPrimaryField() + "} ";
        return where;
    }

    public void setWhere(String where) {
        this.where = where;
    }


    public Map<String, String> getFcMap() {
        return fcMap;
    }

    public List<TableColumn> getTableColumnList() {
        return tableColumnList;
    }

    public void setTableColumnList(List<TableColumn> tableColumnList) {
        this.tableColumnList = tableColumnList;
    }

    public static String getRealMappBy(Object model, String mappby) {
        if (model instanceof ModelBase) {
            Map<String, String> fcmap = TableStruct.getTableStruct(model.getClass().getName()).getFcMap();
            for (Map.Entry<String, String> entry : fcmap.entrySet()) {
                if (mappby.equals(entry.getValue()))
                    return entry.getKey();
            }
        }
        return mappby;
    }

    public void intercept(String key, Map<String, Object> rowData, SqlResult sqlResult) {
        TableStruct table = getTableStruct(sqlResult.getSqlConfig().getReturnType());
        if (table != null && table.isMapped()) {
            for (TableColumn tc : table.getTableColumnList()) {
                rowData.put(tc.getFiledName(), rowData.get(tc.getClumnName()));
            }
        }
    }
}

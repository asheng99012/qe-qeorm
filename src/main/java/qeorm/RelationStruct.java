package qeorm;

/**
 * Created by asheng on 2015/8/5 0005.
 */
public class RelationStruct {
    private String relationKey;
    private String fillKey;
    private String extend;
    private Class clazz;
    private String where;

    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        this.where = where;
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

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }
}

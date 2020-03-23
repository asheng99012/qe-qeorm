package qeorm;

/**
 * Created by asheng on 2015/8/5 0005.
 */
public class TableColumn {
    String clumnName;
    String filedName;
    Class type;

    public String getClumnName() {
        return clumnName;
    }

    public void setClumnName(String clumnName) {
        this.clumnName = clumnName;
    }

    public String getFiledName() {
        return filedName;
    }

    public void setFiledName(String filedName) {
        this.filedName = filedName;
    }

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }
}

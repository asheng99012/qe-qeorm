package qeorm;

/**
 * Created by ashen on 2017-2-3.
 */
public class SqlAndOrNode {
    String whole;
    String field;
    String operator="";
    String paramWhole1;
    String param1;
    String paramWhole2;
    String param2;

    public String getWhole() {
        return whole;
    }

    public void setWhole(String whole) {
        this.whole = whole;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getParamWhole1() {
        return paramWhole1;
    }

    public void setParamWhole1(String paramWhole1) {
        this.paramWhole1 = paramWhole1;
    }

    public String getParam1() {
        return param1;
    }

    public void setParam1(String param1) {
        this.param1 = param1;
    }

    public String getParamWhole2() {
        return paramWhole2;
    }

    public void setParamWhole2(String paramWhole2) {
        this.paramWhole2 = paramWhole2;
    }

    public String getParam2() {
        return param2;
    }

    public void setParam2(String param2) {
        this.param2 = param2;
    }
}

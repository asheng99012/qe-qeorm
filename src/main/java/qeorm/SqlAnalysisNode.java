package qeorm;

/**
 * Created by ashen on 2017-2-3.
 */
public class SqlAnalysisNode {
    String whole;
    String field;
    String operator="";
    String prefix;
    String paramWhole;
    String param;
    String suffix;

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
        this.operator = operator.toLowerCase().trim();
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getParamWhole() {
        return paramWhole;
    }

    public void setParamWhole(String paramWhole) {
        this.paramWhole = paramWhole;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public boolean isLike() {
        return operator.equals("like");
    }

    public boolean isIn() {
        return operator.equals("in");
    }

    public boolean isNotIn() {
        return operator.equals("not in");
    }

    public boolean isBy() {
        return operator.equals("by");
    }

}

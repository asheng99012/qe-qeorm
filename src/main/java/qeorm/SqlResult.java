package qeorm;

import com.google.common.collect.Lists;

import java.util.*;

/**
 * Created by asheng on 2015/5/17 0017.
 */
public class SqlResult {
    //解析处理之后的sql
    String sql;
    //执行sql所需的参数
    Map<String, Object> params;
    SqlConfig sqlConfig;
    boolean isOk = true;
    String errMsg;
    Object result;
    List<SqlResult> childs = Lists.newArrayList();

    public List<SqlResult> getChilds() {
        return childs;
    }

    public void setChilds(SqlResult result) {
        this.childs.add(result);
    }

    public boolean isOk() {
        return this.isOk;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.isOk = false;
        this.errMsg = errMsg;
    }


    public <T> T getResult() {
        if (result != null)
            return (T) result;
        return null;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public SqlResult() {

    }

    public SqlConfig getSqlConfig() {
        return sqlConfig;
    }

    public void setSqlConfig(SqlConfig sqlConfig) {
        this.sqlConfig = sqlConfig;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}

package qeorm.intercept;

import qeorm.SqlResult;

import java.util.Map;

/**
 * Created by ashen on 2017-2-5.
 */
public interface IFunIntercept {
    void intercept(String key, Map<String,Object> rowData, SqlResult sqlResult);
}

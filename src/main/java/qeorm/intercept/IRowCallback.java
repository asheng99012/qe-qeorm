package qeorm.intercept;

import qeorm.SqlResult;

import java.util.Map;

/**
 * Created by ashen on 2017-5-23.
 */
public interface IRowCallback {
    void processRow(Map<String, Object> rowData, int rowNum, SqlResult sqlResult);
}

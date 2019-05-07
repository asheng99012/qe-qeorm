package qeorm;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.ResultSetExtractor;
import qeorm.intercept.IFunIntercept;
import qeorm.intercept.IRowCallback;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by ashen on 2017-5-23.
 */
class RowCallbackHandlerResultSetExtractor implements ResultSetExtractor<Integer> {
    private static ColumnMapRowMapper rowMapper = new ColumnMapRowMapper();
    private List<IRowCallback> rowCallbacks;
    private SqlResult sqlResult;

    public RowCallbackHandlerResultSetExtractor(List<IRowCallback> rowCallbacks, SqlResult sqlResult) {
        this.rowCallbacks = rowCallbacks;
        this.sqlResult = sqlResult;
    }

    @Override
    public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {
        int rowNum = 0;
        while (rs.next()) {
            rowNum++;
            for (IRowCallback rowCallback : this.rowCallbacks) {
                rowCallback.processRow(rowMapper.mapRow(rs, rowNum), rowNum, this.sqlResult);
            }
        }
        return rowNum;
    }
}

package qeorm.jdbc;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.lang.Nullable;

import javax.sql.DataSource;
import java.util.List;

public class QeNamedParameterJdbcTemplate extends NamedParameterJdbcTemplate {
    public QeNamedParameterJdbcTemplate(DataSource dataSource) {
        super(dataSource);
    }

    public QeNamedParameterJdbcTemplate(JdbcOperations classicJdbcTemplate) {
        super(classicJdbcTemplate);
    }

    @Override
    @Nullable
    public <T> T queryForObject(String sql, SqlParameterSource paramSource, RowMapper<T> rowMapper)
            throws DataAccessException {

        List<T> results = getJdbcOperations().query(getPreparedStatementCreator(sql, paramSource), rowMapper);
        if (results == null || results.size() == 0)
            return null;
        return DataAccessUtils.nullableSingleResult(results);
    }
}

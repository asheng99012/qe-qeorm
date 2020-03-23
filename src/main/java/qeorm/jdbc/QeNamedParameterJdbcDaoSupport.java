package qeorm.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.lang.Nullable;

public class QeNamedParameterJdbcDaoSupport extends NamedParameterJdbcDaoSupport {
    @Nullable
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    protected void initTemplateConfig() {
        JdbcTemplate jdbcTemplate = getJdbcTemplate();
        if (jdbcTemplate != null) {
            this.namedParameterJdbcTemplate = new QeNamedParameterJdbcTemplate(jdbcTemplate);
        }
    }

    /**
     * Return a NamedParameterJdbcTemplate wrapping the configured JdbcTemplate.
     */
    @Nullable
    public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        return this.namedParameterJdbcTemplate;
    }

}

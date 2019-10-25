package qeorm.test;

import org.junit.Test;
import qeorm.SqlConfig;
import qeorm.SqlConfigManager;
import qeorm.SqlSession;
import qeorm.utils.JsonUtils;

public class SqlTest {

    @Test
    public void testCreateSql() {
        SqlConfig config = new SqlConfig();
        config.setSql("select * from user where name like '{name}%'");
        SqlConfigManager.parseSql(config);
        System.out.println(JsonUtils.toJson(config));
    }

    @Test
    public void testSession() {
        new SqlSession();
    }

    @Test
    public void testSqlCount() {
        String sql = " select count(id)\n" +
                "       from `expenses_detail`\n" +
                "       where yn = 1\n" +
                "       and partner_id = {partnerId}\n" +
                "       and partner_name = {partnerName}\n" +
                "       and bill_id = {billId}\n" +
                "       and city = {city}\n" +
                "       and own_month = {ownMonth}\n" +
                "       and project_type = {projectType}\n" +
                "       and project_code = {projectCode}\n" +
                "       and room_number = {roomNumber}\n" +
                "       and suite_id = {suiteId}\n" +
                "       and own_month like '{yearLike}%'";

        Object ret = sql.matches(SqlConfigManager.isCountPattern);
        System.out.println("==");
    }

    @Test
    public void testKK() {
        String sql = " select id, rule_id, rule_name, regist_start_date, regist_end_date, effective_start_date, effective_end_date, create_at, update_at, status, create_user, update_user, remark, create_user_name, update_user_name, own_month from `rule_conf`\n" +
                "     where\n" +
                "     (regist_start_date <= {registStartDate} and regist_end_date >= {registStartDate})\n" +
                "      or (regist_start_date <= {registEndDate} and regist_end_date >= {registEndDate})";
        SqlConfig sqlConfig = new SqlConfig();
        sqlConfig.setSql(sql);
        SqlConfigManager.parseSql(sqlConfig);


        sql = "update rule_conf set a={a},b={b} where c={c}";
        sqlConfig = new SqlConfig();
        sqlConfig.setSql(sql);
        SqlConfigManager.parseSql(sqlConfig);
        System.out.println("==");
    }
}

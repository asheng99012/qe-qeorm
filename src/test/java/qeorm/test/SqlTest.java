package qeorm.test;

import org.junit.jupiter.api.Test;
import qeorm.SqlConfig;
import qeorm.SqlConfigManager;
import qeorm.SqlExecutor;
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
    public void testUpdateNow(){
        String sql="update webank set create=now() where id=4";
        SqlExecutor.execSqlForObject(sql,Integer.class);
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
        String sql = " select id, rule_id, rule_name, regist_start_date, regist_end_date, effective_start_date, effective_end_date, create_at, update_at, status, create_user, update_user, remark, create_user_name, update_user_name, own_month from {tableName}\n" +
                "     where\n" +
                "     (\nregist_start_date <= {registStartDate} and a=dd(23,34) and regist_end_date >= {registStartDate} )\n" +
                "      or (regist_start_date <= {registEndDate} and regist_end_date >= {registEndDate})";
        SqlConfig sqlConfig = new SqlConfig();
        sqlConfig.setSql(sql);
        SqlConfigManager.parseSql(sqlConfig);


        sql = "update {tableName} set a={a},b={b} where c={c}";
        sqlConfig = new SqlConfig();
        sqlConfig.setSql(sql);
        SqlConfigManager.parseSql(sqlConfig);

        sql = "select * from {tableName} u where u.id={id}";
        sqlConfig = new SqlConfig();
        sqlConfig.setSql(sql);
        SqlConfigManager.parseSql(sqlConfig);
        System.out.println("==");
    }

    @Test
    public void testUpdate(){
        String sql="update `sell_staff` set `staff_no`={staffNo},`staff_name`={staffName},`job_status`={jobStatus},`city_code`={cityCode},`city_name`={cityName},`position`={position},`department`={department},`secondary_department`={secondaryDepartment},`tertiary_department`={tertiaryDepartment},`four_department`={fourDepartment},`join_date`={joinDate},`leave_date`={leaveDate},`is_seller`={isSeller},`job_level`={jobLevel},`guider`={guider},`mentor`={mentor},`supervisor`={supervisor},`illegal_commission`={illegalCommission},`other_commission`={otherCommission},`is_protected`={isProtected},`create_at`={createAt},`update_at`={updateAt},`create_user`={createUser},`create_user_name`={createUserName},`update_user`={updateUser},`update_user_name`={updateUserName},`yn`={yn},`version`={version},`performance_month_id`={performanceMonthId},`performance_month`={performanceMonth},`is_part_time`={isPartTime},`transfer_out_time`={transferOutTime},`transfer_out_position_after`={transferOutPositionAfter},`transfer_in_time`={transferInTime},`transfer_in_position_before`={transferInPositionBefore} where id={id} ";

        SqlConfig sqlConfig = new SqlConfig();
        sqlConfig.setSql(sql);
        SqlConfigManager.parseSql(sqlConfig);
        System.out.println("==");
    }


}

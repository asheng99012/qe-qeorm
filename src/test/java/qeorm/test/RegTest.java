package qeorm.test;

import org.junit.Test;
import qeorm.test.model.Tag;
import qeorm.utils.JsonUtils;

import java.util.Date;
import java.util.Map;

public class RegTest {
    @Test
    public void testUpdate() {
        String sql = "update tt set 1=1,b=12,1=1,1=1,a=23,1=1,1=1 where 1=1 and a=2 and 1=1 and 1=1 and (1=1 or b=3 or 1=1 or 1=1 or c=3 or 1=1) and ((1=1 and 1=1) or (1=1 and a=2 and 1=1 and 1=1 and v=2 and 1=1) or (1=1 and 1=1 ) or (1=1 and 1=1))  and 1=1 ";

        sql = "SELECT count(1) FROM (SELECT cert1.human_id,cert1.`name`,cert1.idcard as idCard, cert1.dk_result as certificationResult,cert2.phone as phone, cert2.dk_result as phoneResult, case WHEN black.status=0 THEN '未命中' WHEN black.status=1 THEN '命中' else '移除' end as blackResult,cert1.created_at as authenticationResult FROM risk_human_extend_info cert1 LEFT JOIN risk_human_extend_info cert2 on cert2.human_id=cert1.human_id AND cert2.type='Cert03' LEFT JOIN risk_user_blacklist black on black.human_id=cert1.human_id WHERE cert1.type='Cert01' and cert1.human_id !='null' and cert1.human_id !='' and cert1.idcard= cert2.idcard and 1=1 and 1=1 and 1=1 and 1=1 ORDER BY cert1.id desc) t";
        sql = replaceWhere(sql);
        sql = sql.replaceAll("\\s+", " ");
        sql = sql.replaceAll("\\s+\\(\\s+\\)", "()");
        sql = sql.replaceAll("\\(\\s+", "(");
        sql = sql.replaceAll("\\s+\\)", ")");
//        sql = sql.replaceAll(",(\\s*1=1\\s*,)+", " , ");
//        sql = sql.replaceAll(",?\\s*1=1\\s*,?", " ");
        System.out.println(sql);
    }

    public String replaceWhere(String sql) {
        sql = sql.replaceAll(",\\s*1=1\\s*", " ");
        sql = sql.replaceAll("1=1\\s*,", " ");
        sql = sql.replaceAll("(?i)1=1\\s+or\\s+", " ");
        sql = sql.replaceAll("(?i)\\s+or\\s+1=1\\s*", " ");
        sql = sql.replaceAll("(?i)\\s+and\\s+1=1\\s*", " ");
        sql = sql.replaceAll("\\(+\\s*1=1\\s*\\)", " 1=1 ");
        if (sql.matches(".*,\\s*1=1\\s*.*")
                || sql.matches(".*1=1\\s*,.*")
                || sql.matches("(?i).*1=1\\s+or\\s+.*")
                || sql.matches("(?i).*\\s+or\\s+1=1\\s*.*")
                || sql.matches("(?i).*\\s+and\\s+1=1\\s*.*")
                || sql.matches(".*\\(+\\s*1=1\\s*\\).*"))
            sql = replaceWhere(sql);
        return sql;
    }


    @Test
    public void testJson() {
        Tag ret = JsonUtils.convert("{\"add_time\":\"2019-12-09T13:03:57\",\"ps\":50}", Tag.class);
        Map map = JsonUtils.convert(ret, Map.class);
        System.out.println(JsonUtils.toJson(ret));
    }
}

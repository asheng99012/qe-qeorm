package qeorm.test;

import org.junit.Test;
import qeorm.SqlAnalysisNode;
import qeorm.SqlConfig;
import qeorm.SqlConfigManager;
import qeorm.utils.JsonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CacheManagerTest {
    @Test
    public void testMatchTableName() {
        run("select * frOm\r\nuser as u left Join `laputa`.`human` as h on u.id=h.dd left join cont as c on c.id=h.hid  union (select * from bb left join cc \r\nleft join dd \n) order by");
        run("inSERT INTo human (a,b,c)\r\n values(1,1,1)");
        run(" update info set a=1");
    }


    @Test
    public void testsql(){
        String sql=" update  count(1)   FROM (\n" +
                "            SELECT\n" +
                "              cert1.human_id,cert1.`name`,cert1.idcard as idCard, cert1.dk_result as certificationResult,cert2.phone as phone, cert2.dk_result as phoneResult,\n" +
                "             case WHEN black.status=0 THEN '未命中'  WHEN black.status=1 THEN '命中' else '移除'  end as blackResult,cert1.created_at as authenticationResult\n" +
                "            FROM\n" +
                "            risk_human_extend_info cert1\n" +
                "            LEFT JOIN risk_human_extend_info cert2 on cert2.human_id=cert1.human_id AND cert2.type='Cert03'\n" +
                "             LEFT JOIN risk_user_blacklist black on black.human_id=cert1.human_id\n" +
                "             WHERE cert1.type='Cert01'  and cert1.human_id !='null'  and cert1.human_id !='' and cert1.idcard= cert2.idcard and   cert1.dk_result={certification} and cert2.dk_result={phoneCertification} and cert2.phone={phone} and cert2.dk_result!={phoneNoCertification} and cert1.dk_result!={noCertification} and cert1.created_at>{beginTime} and cert1.created_at<{endTime} and cert1.human_id={humanId} and cert1.name={name}    ORDER BY cert1.id desc\n" +
                "            ) t";
        System.out.println(sql.matches("^\\s*(?i)select\\s+count\\s*\\(.+?\\)\\s+from[\\s\\S]+"));
        System.out.println(sql.matches("^\\s*(?i)update\\s+[\\s\\S]+"));

    }

    public void run(String sql) {
        System.out.println("==============");

        System.out.println(sql);

        String sqlTablePattern = "\\s+(?i)(from|join|into|update)\\s+([`\\.a-zA-Z\\d_]+){1}";


        Matcher om = Pattern.compile(sqlTablePattern).matcher(sql);
        List<String> list = new ArrayList<String>();
        while (om.find()) {
            String name = om.group(2);
            name = name.replaceAll("`", "");
            if (name.indexOf(".") > 0)
                name = name.substring(name.indexOf(".") + 1);
            list.add(name);
            int c = om.groupCount();
            for (int i = 0; i <= c; i++) {
                System.out.println(i + "===" + om.group(i));
            }
            System.out.println("@@@@@@@@@@@@@@");

        }
        System.out.println(JsonUtils.toJson(list));
    }
}

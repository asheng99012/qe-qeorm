package qeorm.test;

import org.junit.Test;
import qeorm.SqlAnalysisNode;
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

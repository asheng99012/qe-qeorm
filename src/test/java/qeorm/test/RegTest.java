package qeorm.test;

import org.junit.Test;

public class RegTest {
    @Test
    public void testUpdate() {
        String sql = "update tt set 1=1,b=12,1=1,1=1,a=23,1=1,1=1 where 1=1 and a=2 and 1=1 and 1=1 and (1=1 or b=3 or 1=1 or 1=1 or c=3 or 1=1) and ((1=1 and 1=1) or (1=1 and a=2 and 1=1 and 1=1 and v=2 and 1=1) or (1=1 and 1=1 ) or (1=1 and 1=1))  and 1=1 ";
        sql=replaceWhere(sql);
        sql = sql.replaceAll(",\\s*1=1\\s*", " ");
        sql = sql.replaceAll("1=1\\s*,", " ");
        sql = sql.replaceAll("(?i)1=1\\s+or\\s+", " ");
        sql = sql.replaceAll("(?i)or\\s+1=1\\s*", " ");
        sql = sql.replaceAll("(?i)and\\s*1=1\\s*", " ");
        sql = sql.replaceAll("\\(+\\s*1=1\\s*\\)", " 1=1 ");
//        sql = sql.replaceAll(",(\\s*1=1\\s*,)+", " , ");
//        sql = sql.replaceAll(",?\\s*1=1\\s*,?", " ");
        System.out.println("ok");
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
}

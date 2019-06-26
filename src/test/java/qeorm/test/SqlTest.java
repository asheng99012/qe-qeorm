package qeorm.test;

import org.junit.Test;
import qeorm.SqlConfig;
import qeorm.SqlConfigManager;
import qeorm.SqlSession;
import qeorm.utils.JsonUtils;

public class SqlTest {

    @Test
    public void testCreateSql(){
        SqlConfig config=new SqlConfig();
        config.setSql("select * from user where name like '{name}%'");
        SqlConfigManager.parseSql(config);
        System.out.println(JsonUtils.toJson(config));
    }

    @Test
    public void testSession(){
        new SqlSession();
    }
}

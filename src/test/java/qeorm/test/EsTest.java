package qeorm.test;

import com.alibaba.druid.pool.DruidDataSource;
//import com.alibaba.druid.pool.ElasticSearchDruidDataSourceFactory;
import org.junit.Before;
import org.junit.Test;
import qeorm.SqlExecutor;
import qeorm.SqlSession;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;

public class EsTest {
    SqlSession session;

//    @Before
    public void setup() throws Exception {
        session = new SqlSession();
        Properties properties = new Properties();
        properties.put("url", "jdbc:elasticsearch://172.16.31.55:9300/dkbs_new");
//        properties.put("url", "jdbc:elasticsearch://172.21.10.10:9300/dk-risk-2018.12.30");
        DruidDataSource dds =null;
//        dds=(DruidDataSource) ElasticSearchDruidDataSourceFactory.createDataSource(properties);
        Map<String, DataSource> dataSources = new HashMap<>();
        dataSources.put("esMaster", dds);
        session.setDataSources(dataSources);
    }

//    @Test
    public void select() {
        Object list;
//        list = SqlExecutor.execSql("SELECT * FROM dkbs_new/houseSource limit 1,4", new HashMap<>(), Map.class, "esMaster");
//        list = SqlExecutor.execSql("SELECT * FROM dkbs_new/houseSource limit 4,5", new HashMap<>(), Map.class, "esMaster");
//        list = SqlExecutor.execSqlForObject("select count(*) from dkbs_new/houseSource ", new HashMap<>(), Map.class, "esMaster");
//        list = queryList("  select blockId,count(*) as num,max(price) as max,avg(price) as avg from dkbs_new/houseSource " +
//                "group by (blockId),(cityId)");
//        Map data = new HashMap() {{
//            put("rentType", "整租");
//            put("price", 4500);
////            put("availableDate",new Date());
//        }};
//        list = SqlExecutor.execSql("select  rentType,price from dkbs_new/houseSource  where rentType ={rentType} and price>{price} and availableDate>{availableDate}", data, Map.class, "esMaster");

//        list=queryList("SELECT searchText FROM dkbs_new where searchText like '%金隅万科%'");
        list = queryList("SELECT searchText,searchText.keyword FROM dkbs_new where searchText =matchQuery('金隅万科')");
        list = queryList("SELECT searchText,searchText.keyword FROM dkbs_new where searchText =matchphrase('金隅万科')");
        System.out.println("ok");
    }

    public Object queryList(String sql) {
        return SqlExecutor.execSql(sql, new HashMap<>(), Map.class, "esMaster");
    }

    public Object queryObject(String sql) {
        return SqlExecutor.execSqlForObject(sql, new HashMap<>(), Map.class, "esMaster");
    }
}

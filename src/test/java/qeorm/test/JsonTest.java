package qeorm.test;

import org.junit.jupiter.api.Test;
import qeorm.SqlExecutor;
import qeorm.utils.JsonUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JsonTest {
    @Test
    public void json(){
        String json="{\"payKind\":1,\"payDetailPayloadList\":[{\"payDetailAmount\":19300,\"payNo\":\"20191218143230251723699383151\",\"createdAt\":1576650750251,\"payType\":1,\"discountAmount\":0,\"paySuccAt\":1576650789081,\"payDetailId\":6104299451,\"detailPayStatus\":2}],\"payId\":6087184651,\"paySuccAt\":1576650789081,\"bizOrderType\":79,\"payStatus\":2,\"userId\":1596151,\"bizOrderInfo\":\"[{\\\"bizOrderNo\\\":203182413010274351}]\",\"bizOrderNo\":203182413010274351}";
        Map map= JsonUtils.convert(json,Map.class);
        System.out.println("ok");
    }

    @Test
    public void testJingdu(){
        String sql = "insert into fmis_log (`traceid`,`key`,`exchange`,`routingkey`,`ip`,`message`,`exeCount`,`create_at`,`type`,`status`,`result`,`StartDeliverTime`,`appNamme`) values({traceid},{key},{exchange},{routingkey},{ip},{message},{exeCount},{create_at},{type},{status},{result},{StartDeliverTime},{appNamme})";
        String json="{\"payKind\":1,\"payDetailPayloadList\":[{\"payDetailAmount\":19300,\"payNo\":\"20191218143230251723699383151\",\"createdAt\":1576650750251,\"payType\":1,\"discountAmount\":0,\"paySuccAt\":1576650789081,\"payDetailId\":6104299451,\"detailPayStatus\":2}],\"payId\":6087184651,\"paySuccAt\":1576650789081,\"bizOrderType\":79,\"payStatus\":2,\"userId\":1596151,\"bizOrderInfo\":\"[{\\\"bizOrderNo\\\":203182413010274351}]\",\"bizOrderNo\":203182413010274351}";
        Map map= JsonUtils.convert(json,Map.class);
        Map param = new HashMap() {{
            put("traceid", "111");
            put("key", "wer");
            put("exchange", "exchange");
            put("routingkey", "routingkey");
            put("ip", "ip");
            put("message", map);
            put("ext", "ext");
            put("exeCount", 0);
            put("create_at", new Date());
            put("type", "type");
            put("status", 1);
            put("result", "result");
            put("StartDeliverTime", new Date());
            put("appNamme", "appNamme");
        }};
        SqlExecutor.execSql(sql, param, Integer.class, "dbName");
    }
}

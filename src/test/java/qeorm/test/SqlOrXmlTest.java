package qeorm.test;

import org.junit.jupiter.api.Test;
import qeorm.SqlOrXmlExecutor;

/**
 * @author asheng
 * @version : SqlOrXmlTest.java, v 0.1 2020年06月01日 11:29 asheng Exp $
 */
public class SqlOrXmlTest {
    @Test
    public void testSqlOrXml(){
        String sql="<sqlConfig >\n"
                + "        <sql  dbName=\"gdfsSlave\">\n"
                + "            <![CDATA[ \n"
                + "\n"
                + "select from_unixtime(eu.reg_time ) as 注册时间,ifnull(eu.name ,eu.nickname ) as 姓名\n"
                + ",concat(substring(eu.user_name ,1,3),'****', substring(eu.user_name ,length(eu.user_name )-3 ,4))  as 电话\n"
                + ",eu.user_id ,(case when eu.user_rank=9 then '注册' else '高级' end) as 等级,s.store_name \n"
                + ",eu.source_type as 注册来源,ba.name as 所属ba \n"
                + ",concat(substring(ba.login_name ,1,3),'****', substring(ba.login_name ,length(ba.login_name )-3 ,4))  as 所属ba电话\n"
                + "from ecs_users eu \n"
                + "left join ecs_store s on eu.store_id =s.store_id\n"
                + "left join ba_user ba on ba.id =eu.ba_user_id \n"
                + "where eu.reg_time >UNIX_TIMESTAMP({startTime}) and eu.reg_time <UNIX_TIMESTAMP({endTime})\n"
                + "and eu.user_name ={user_name} and s.store_name ={store_name}\n"
                + "and eu.source_type={source_type}\n"
                + "\t\t\t\n"
                + "            ]]>\n"
                + "        </sql>\n"
                + "        <sqlIntercepts>\n"
                + "            <sqlConfig extend=\"extend\" relationKey=\"user_id|user_id\" dbName=\"gdfsSlave\">\n"
                + "                 <sql><![CDATA[  \n"
                + "\t\t\t\t \n"
                + "\t\t\t\t select user_id,count(*) as 订单量,sum(goods_amount) as 商品总额,sum(pay_price) as 实际支付总额 \n"
                + "from (\n"
                + "\tselect eoi.user_id,eoi.goods_amount,sum(oc.yue_price+oc.order_price) as pay_price \n"
                + "\tfrom ecs_order_info eoi\n"
                + "\tleft join ecs_order_child oc on oc.order_id =eoi.order_id \n"
                + "\twhere eoi.pay_status=2 and eoi.user_id in ({user_id}) \n"
                + "\tgroup by eoi.order_id \n"
                + ") t group by user_id\n"
                + "\t\t\t\t \n"
                + "\t\t\t\t ]]></sql>\n"
                + "            </sqlConfig>\n"
                + "        </sqlIntercepts>\n"
                + "    </sqlConfig>";

        SqlOrXmlExecutor.isGroupBy(sql);
    }
}

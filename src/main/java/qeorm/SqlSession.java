package qeorm;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NamedThreadLocal;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Created by ashen on 2017-2-4.
 */

public class SqlSession {

    public final static String Master = "Master";
    public final static String Slave = "Slave";

    private final ThreadLocal<Map<Object, Object>> resources =
            new NamedThreadLocal<Map<Object, Object>>("SqlSession Transactional");
    private Logger logger = LoggerFactory.getLogger(SqlSession.class);

    private Map<Object, Object> getResources() {
        Map<Object, Object> map = resources.get();
        if (map == null) {
            map = new HashMap<Object, Object>();
            resources.set(map);
        }
        return map;
    }

    public void beginTransaction(){
        getResources().put("isTransaction", true);
        setTransactionCount(true);
    }
    @Deprecated
    public void setTransaction(boolean transaction) {
        getResources().put("isTransaction", transaction);
        setTransactionCount(transaction);
    }


    private boolean isTransaction() {
        Map<Object, Object> map = getResources();
        if (map.containsKey("isTransaction"))
            return (Boolean) map.get("isTransaction");
        return false;
    }

    private int getTransactionCount() {
        Map<Object, Object> map = getResources();
        if (map.containsKey("transactionCount"))
            return (Integer) map.get("transactionCount");
        return 0;
    }

    private int setTransactionCount(boolean plus) {
        int count = getTransactionCount();
        if (plus) count = count + 1;
        else count = count - 1;
        getResources().put("transactionCount", count);
        logger.info("当前事务深度为："+count);
        return count;
    }

    //----------------

    private static Map<String, NamedParameterJdbcDaoSupport> jdbcTemplate = new HashMap<String, NamedParameterJdbcDaoSupport>();
    private String defaultDataSource;

    public void setDataSources(final Map<String, DataSource> dataSources) {
        String[] keys = Iterators.toArray(dataSources.keySet().iterator(), String.class);
        for (String input : keys) {
            logger.info("设置数据源{}", input);
            NamedParameterJdbcDaoSupport support = new NamedParameterJdbcDaoSupport();
            support.setDataSource(dataSources.get(input));
            jdbcTemplate.put(input.trim(), support);
        }
        logger.info("数据源初始化完毕");
        SqlResultExecutor.setSqlSession(this);
    }

    public void setDefaultDataSource(String _defaultDataSource) {
        defaultDataSource = _defaultDataSource;
    }

    public NamedParameterJdbcOperations getJdbcTemplate() {
        return getJdbcTemplate(null);
    }

    public NamedParameterJdbcOperations getJdbcTemplate(String _dbName) {
        String dbName = _dbName;
        if (Strings.isNullOrEmpty(_dbName)) dbName = defaultDataSource + Master;
        if (!jdbcTemplate.containsKey(dbName)) {
            if (dbName.endsWith(Master))
                dbName = defaultDataSource + Master;
            else if (dbName.endsWith(Slave))
                dbName = defaultDataSource + Slave;
            logger.warn("数据源{}不存在，使用了默认数据源{}", _dbName, dbName);

        }
        //如果开启事务，则停用从库
        if (isTransaction() && dbName.endsWith(Slave))
            dbName = dbName.replace(Slave, Master);

        if (!jdbcTemplate.containsKey(dbName)){
            logger.error("数据源" + dbName + "不存在", new DataSourceNotExistException("数据源" + dbName + "不存在"));
            throw new DataSourceNotExistException("数据源" + dbName + "不存在");
        }
        logger.info("使用的数据源是{}", dbName);
        NamedParameterJdbcDaoSupport jdbc = jdbcTemplate.get(dbName);
        setTransaction(dbName, jdbc.getDataSource());
        return jdbc.getNamedParameterJdbcTemplate();
    }


    private Stack<Pair<DataSourceTransactionManager, TransactionStatus>> getStack() {
        Map<Object, Object> map = getResources();
        Stack<Pair<DataSourceTransactionManager, TransactionStatus>> stack;
        if (!map.containsKey("stack")) {
            stack = new Stack<Pair<DataSourceTransactionManager, TransactionStatus>>();
            map.put("stack", stack);
        } else {
            stack = (Stack<Pair<DataSourceTransactionManager, TransactionStatus>>) map.get("stack");
        }
        return stack;
    }

    private Map<String, Boolean> getStartedDb() {
        Map<Object, Object> map = getResources();
        Map<String, Boolean> startedDb;
        if (!map.containsKey("startedDb")) {
            startedDb = Maps.newHashMap();
            map.put("startedDb", startedDb);
        } else {
            startedDb = (Map<String, Boolean>) map.get("startedDb");
        }
        return startedDb;
    }

    private void setTransaction(String dbName, DataSource dataSource) {
        if (!isTransaction()) return;
        Map<Object, Object> map = getResources();
        Map<String, Boolean> startedDb = getStartedDb();
        if (startedDb.containsKey(dbName)) return;
        startedDb.put(dbName, true);
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(dataSource);
        TransactionStatus status = dataSourceTransactionManager.getTransaction(null);
        getStack().push(new MutablePair<>(dataSourceTransactionManager, status));
    }

    public void rollback() {
        if (!isTransaction()) return;
        if (setTransactionCount(false) > 0) return;
        Stack<Pair<DataSourceTransactionManager, TransactionStatus>> stack = getStack();
        Map<String, Boolean> startedDb = getStartedDb();
        while (!stack.isEmpty()) {
            Pair<DataSourceTransactionManager, TransactionStatus> pair = stack.pop();
            pair.getKey().rollback(pair.getValue());
        }
        resources.remove();
    }

    public void commit() {
        if (!isTransaction()) return;
        if (setTransactionCount(false) > 0) return;
        Stack<Pair<DataSourceTransactionManager, TransactionStatus>> stack = getStack();
        Map<String, Boolean> startedDb = getStartedDb();
        while (!stack.isEmpty()) {
            Pair<DataSourceTransactionManager, TransactionStatus> pair = stack.pop();
            pair.getKey().commit(pair.getValue());
        }
        resources.remove();
    }


}

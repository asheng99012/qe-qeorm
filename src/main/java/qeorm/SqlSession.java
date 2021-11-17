package qeorm;

import com.google.common.base.Strings;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.yaml.snakeyaml.Yaml;
import qeorm.jdbc.QeNamedParameterJdbcDaoSupport;
import qeorm.utils.JsonUtils;

import javax.sql.DataSource;
import java.sql.SQLException;
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

    public static SqlSession instance;
    public Map<String, String> defaultConfig;

    public SqlSession() {
        defaultConfig = new HashMap<>();
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources("classpath*:defultQeormConfig.yml");
            Map config = null;
            config = new Yaml().loadAs(resources[0].getInputStream(), Map.class);
            config = JsonUtils.convert(config.get("qeorm"), Map.class);
            config = JsonUtils.convert(config.get("datasource"), Map.class);
            config = JsonUtils.convert(config.get("defaultConfig"), Map.class);
            defaultConfig = config;
            instance = this;
            SqlResultExecutor.setSqlSession(this);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }

    }

    private Map<Object, Object> getResources() {
        Map<Object, Object> map = resources.get();
        if (map == null) {
            map = new HashMap<Object, Object>();
            resources.set(map);
        }
        return map;
    }

    public void beginTransaction() {
        setTransactionCount(true);
    }

    private boolean isTransaction() {
        return getTransactionCount() > 0;
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
        logger.info("当前事务深度为：" + count);
        return count;
    }

    //----------------

    private static Map<String, NamedParameterJdbcDaoSupport> jdbcTemplate =null;

    //    @Value("${qeorm.defaultDataSource}")
    private String defaultDataSource="default";

    public static Map<String, Map<String, String>> dataSourcesMap;

    public void setDataSources(final Map<String, DataSource> dataSources) {
        String[] keys = Iterators.toArray(dataSources.keySet().iterator(), String.class);
        jdbcTemplate=new HashMap<>();
        for (String input : keys) {
            logger.info("设置数据源{}", input);
            QeNamedParameterJdbcDaoSupport support = new QeNamedParameterJdbcDaoSupport();
            support.setDataSource(dataSources.get(input));
            jdbcTemplate.put(input.trim(), support);
        }
        logger.info("数据源初始化完毕");
        instance = this;
        SqlResultExecutor.setSqlSession(this);
    }

    public void initJdbcTemplate(){
        if(jdbcTemplate==null){
            jdbcTemplate=new HashMap<>();
            QeNamedParameterJdbcDaoSupport support = new QeNamedParameterJdbcDaoSupport();
            support.setDataSource(SpringUtils.getBean(DataSource.class));
            jdbcTemplate.put(defaultDataSource+Master, support);
            jdbcTemplate.put(defaultDataSource+Slave, support);

        }
    }

    public void setDefaultDataSource(String _defaultDataSource) {
        defaultDataSource = _defaultDataSource;
    }

    //    @Value("${qeorm.dataSourcesMap}")
    public void setDataSourcesMap(Map<String, Map<String, String>> _dataSourcesMap) throws ClassNotFoundException, IllegalAccessException, InstantiationException, SQLException {
        dataSourcesMap=_dataSourcesMap;
        if (dataSourcesMap == null) return;
        Map<String, String> _defaultConfig = dataSourcesMap.get("defaultConfig");
        if (_defaultConfig != null)
            defaultConfig.putAll(_defaultConfig);
        Map<String, DataSource> dataSources = new HashMap<>();
        BeanDefinitionRegistry beanFactory = (BeanDefinitionRegistry) ((ConfigurableApplicationContext) SpringUtils.getApplicationContext()).getBeanFactory();
        for (Map.Entry<String, Map<String, String>> entry : dataSourcesMap.entrySet()) {
            if (!entry.getKey().equals("defaultConfig")) {
                Map<String, String> config = new HashMap<>();
                config.putAll(defaultConfig);
                config.putAll(entry.getValue());

                String type=config.get("type");
                if(Strings.isNullOrEmpty(type)){
                    type=config.get("class");
                }
                config.remove("type");
                config.remove("class");
                //创建bean信息.
                BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(Class.forName(type));
                for (Map.Entry<String, String> kv : config.entrySet()) {
                    String field = kv.getKey();
                    beanDefinitionBuilder.addPropertyValue(kv.getKey(), kv.getValue());
                }
                beanFactory.registerBeanDefinition(entry.getKey(), beanDefinitionBuilder.getBeanDefinition());
                dataSources.put(entry.getKey(), (DataSource) SpringUtils.getBean(entry.getKey()));
            }
        }
        setDataSources(dataSources);
    }

    public NamedParameterJdbcOperations getJdbcTemplate() {
        return getJdbcTemplate(null);
    }

    public NamedParameterJdbcOperations getJdbcTemplate(String _dbName) {

        NamedParameterJdbcDaoSupport jdbc = getSupport(_dbName);

        return jdbc.getNamedParameterJdbcTemplate();
    }

    public NamedParameterJdbcDaoSupport getSupport(String _dbName) {
        initJdbcTemplate();
        String dbName = _dbName;
        if (Strings.isNullOrEmpty(_dbName)) dbName = defaultDataSource + Master;
        if (!jdbcTemplate.containsKey(dbName)) {
            if (dbName.endsWith(Master))
                dbName = defaultDataSource + Master;
            else if (dbName.endsWith(Slave))
                dbName = defaultDataSource + Slave;
            logger.trace("数据源{}不存在，使用了默认数据源{}", _dbName, dbName);

        }
        //如果开启事务，则停用从库
        if (isTransaction() && dbName.endsWith(Slave) && jdbcTemplate.containsKey(dbName.replace(Slave, Master)))
            dbName = dbName.replace(Slave, Master);

        if (!jdbcTemplate.containsKey(dbName)) {
            logger.error("数据源" + dbName + "不存在", new DataSourceNotExistException("数据源" + dbName + "不存在"));
            throw new DataSourceNotExistException("数据源" + dbName + "不存在");
        }
        logger.trace("使用的数据源是{}", dbName);
        NamedParameterJdbcDaoSupport jdbc = jdbcTemplate.get(dbName);
        setTransaction(dbName, jdbc.getDataSource());
        return jdbc;
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

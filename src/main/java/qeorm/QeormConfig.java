package qeorm;

import com.alibaba.druid.pool.DruidAbstractDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
public class QeormConfig {

    @Bean
    @ConditionalOnProperty(prefix = "qeorm.mapper", name = "basePackage")
    public MapperScanner qeormMapperScanner() {
        return new MapperScanner();
    }

    @Bean
    @ConditionalOnProperty(prefix = "qeorm.datasource", name = "defaultDataSource")
    public SqlSession qeormSqlSession() {
        return new SqlSession();
    }

    @Bean
    @ConditionalOnBean(SqlSession.class)
    public TransactionalManager transactionalManager() {
        return new TransactionalManager();
    }

    @Bean
    @ConditionalOnProperty(prefix = "qeorm.mapper", name = "basePackage")
    public QeormBeanDefinitionRegistryPostProcessor qeormBeanDefinitionRegistryPostProcessor(Environment env) {
        return new QeormBeanDefinitionRegistryPostProcessor(env);
    }


}

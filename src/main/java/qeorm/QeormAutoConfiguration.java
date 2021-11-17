package qeorm;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class QeormAutoConfiguration {

    @Bean
    public SpringUtils springUtils() {
        return new SpringUtils();
    }

    @Bean
    @ConditionalOnProperty(prefix = "qeorm.mapper", name = "basePackage")
    public MapperScanner qeormMapperScanner() {
        return new MapperScanner();
    }

    @Bean
//    @ConditionalOnProperty(prefix = "qeorm.datasource", name = "defaultDataSource")
    @ConfigurationProperties(prefix = "qeorm.datasource")
    public SqlSession qeormSqlSession() {
        return new SqlSession();
    }

    @Bean
    @ConditionalOnBean(SqlSession.class)
    public TransactionalManager transactionalManager() {
        return new TransactionalManager();
    }

    @Bean
    @ConditionalOnProperty(prefix = "qeorm.mapper", name = "mapperLocations")
    public QeormBeanDefinitionRegistryPostProcessor qeormBeanDefinitionRegistryPostProcessor(Environment env) {
        return new QeormBeanDefinitionRegistryPostProcessor(env);
    }


}

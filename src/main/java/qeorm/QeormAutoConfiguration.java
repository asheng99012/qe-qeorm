package qeorm;

import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

@Configuration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
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

package qeorm;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

public class QeormBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {
    Environment env;

    public QeormBeanDefinitionRegistryPostProcessor(Environment env) {
        this.env = env;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        ClassPathScanner scanner = new ClassPathScanner(registry);
        String basePackage = env.getProperty("qeorm.mapper.basePackage");
        String dsIdenty = env.getProperty("qeorm.mapper.dsIdenty");
        String mapperLocations = env.getProperty("qeorm.mapper.mapperLocations");
        scanner.registerFilters();
        scanner.scan(StringUtils.tokenizeToStringArray(basePackage, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
        try {
            SqlConfigManager.setDsIdenty(dsIdenty == null ? 3 : Integer.parseInt(dsIdenty));
            SqlConfigManager.scan(StringUtils.tokenizeToStringArray(mapperLocations, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }
}

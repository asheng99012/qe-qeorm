package qeorm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

/**
 * Created by ashen on 2017-2-3.
 */
public class MapperScanner implements BeanDefinitionRegistryPostProcessor {
    Logger logger = LoggerFactory.getLogger(getClass());
    //要扫描的包
    private String basePackage;
    //xml配置文件的路径
    public String mapperLocations;

    private int dsIdenty;

    //热部署，定时更新 dao 的 xml 文件
    private int interval;

    public void setDsIdenty(int dsIdenty) {
        this.dsIdenty = dsIdenty;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public void setMapperLocations(String mapperLocations) {
        this.mapperLocations = mapperLocations;
    }

    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        logger.info("SqlConfigScanner.postProcessBeanDefinitionRegistry");
        ClassPathScanner scanner = new ClassPathScanner(registry);
        scanner.registerFilters();
        scanner.scan(StringUtils.tokenizeToStringArray(this.basePackage, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
        try {
            SqlConfigManager.setDsIdenty(dsIdenty);
            SqlConfigManager.scan(StringUtils.tokenizeToStringArray(this.mapperLocations, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
            if (interval > 0) Hotload.init(this, interval);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }
}




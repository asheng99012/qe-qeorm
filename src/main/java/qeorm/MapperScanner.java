package qeorm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

/**
 * Created by ashen on 2017-2-3.
 */
@ConfigurationProperties(prefix = "qeorm.mapper")
public class MapperScanner {
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

    public String getBasePackage() {
        return basePackage;
    }

    public String getMapperLocations() {
        return mapperLocations;
    }

    public int getDsIdenty() {
        return dsIdenty;
    }

    public void init(){

    }
}




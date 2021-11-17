package qeorm;

import com.google.common.base.Strings;
import lombok.SneakyThrows;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.StringUtils;
import qeorm.annotation.QeMapper;

import java.io.IOException;

public class QeormBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor, ResourceLoaderAware {
    Environment env;
    private ResourceLoader resourceLoader;

    public QeormBeanDefinitionRegistryPostProcessor(Environment env) {
        this.env = env;
    }

    @SneakyThrows
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        String basePackage = env.getProperty("qeorm.mapper.basePackage");
        String dsIdenty = env.getProperty("qeorm.mapper.dsIdenty");
        String mapperLocations = env.getProperty("qeorm.mapper.mapperLocations");
        if (!Strings.isNullOrEmpty(basePackage)) {
            ClassPathScanner scanner = new ClassPathScanner(registry);
            scanner.registerFilters();
            scanner.scan(StringUtils.tokenizeToStringArray(basePackage, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
        }
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

    public void scanQeMapper(BeanDefinitionRegistry registry) throws IOException, ClassNotFoundException {
        ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
        MetadataReaderFactory metaReader = new CachingMetadataReaderFactory(resourceLoader);
        Resource[] resources = resolver.getResources("classpath*:com/tianya/**/*.class");

        for (Resource r : resources) {
            MetadataReader reader = metaReader.getMetadataReader(r);
            if (reader.getAnnotationMetadata().getAnnotationTypes().contains(QeMapper.class.getName())) {
                String className = reader.getClassMetadata().getClassName();
                Class<?> klass = Class.forName(className);
                BeanDefinition sbd = new RootBeanDefinition(SqlConfigProxy.class);
                sbd.getConstructorArgumentValues().addGenericArgumentValue(klass);
                BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(sbd, klass.getSimpleName());
                ScopedProxyUtils.createScopedProxy(definitionHolder, registry, true);
            }
        }
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}

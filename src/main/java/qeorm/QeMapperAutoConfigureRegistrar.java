package qeorm;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import qeorm.annotation.QeMapper;
import qeorm.annotation.QeMapperScan;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class QeMapperAutoConfigureRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    public static String[] packages={"com.tianya"};
    private ResourceLoader resourceLoader;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes mapperScanAttrs = AnnotationAttributes
                .fromMap(importingClassMetadata.getAnnotationAttributes(QeMapperScan.class.getName()));

        QeMapperAutoConfigureRegistrar.packages = (String[]) mapperScanAttrs.get("value");
        QeMapperBeanDefinitionScanner scanner = new QeMapperBeanDefinitionScanner(registry, false);
        scanner.setResourceLoader(resourceLoader);
        scanner.addIncludeFilter(new AnnotationTypeFilter(QeMapper.class));
        scanner.doScan(packages);
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public static class QeMapperBeanDefinitionScanner extends ClassPathBeanDefinitionScanner {
        TypeFilter tf = new AnnotationTypeFilter(QeMapper.class);
        static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";
        private String resourcePattern = DEFAULT_RESOURCE_PATTERN;
        private ResourcePatternResolver resourcePatternResolver;

        public QeMapperBeanDefinitionScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters) {
            super(registry, useDefaultFilters);
        }

        @Override
        public Set<BeanDefinition> findCandidateComponents(String basePackage) {
            return scanCandidateComponents(basePackage);
        }

        private ResourcePatternResolver getResourcePatternResolver() {
            if (this.resourcePatternResolver == null) {
                this.resourcePatternResolver = new PathMatchingResourcePatternResolver();
            }
            return this.resourcePatternResolver;
        }

        private Set<BeanDefinition> scanCandidateComponents(String basePackage) {
            Set<BeanDefinition> candidates = new LinkedHashSet<>();
            try {
                String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                        resolveBasePackage(basePackage) + '/' + this.resourcePattern;
                Resource[] resources = getResourcePatternResolver().getResources(packageSearchPath);
                boolean traceEnabled = logger.isTraceEnabled();
                boolean debugEnabled = logger.isDebugEnabled();
                for (Resource resource : resources) {
                    if (traceEnabled) {
                        logger.trace("Scanning " + resource);
                    }
                    if (resource.isReadable()) {
                        try {
                            MetadataReader metadataReader = getMetadataReaderFactory().getMetadataReader(resource);
                            if (isCandidateComponent(metadataReader)) {
                                ScannedGenericBeanDefinition sbd = new ScannedGenericBeanDefinition(metadataReader);
                                sbd.setSource(resource);
                                if (isCandidateComponent(sbd)) {
                                    candidates.add(sbd);
                                }
                            }
                        } catch (Throwable ex) {
                            throw new BeanDefinitionStoreException(
                                    "Failed to read candidate component class: " + resource, ex);
                        }
                    } else {
                        if (traceEnabled) {
                            logger.trace("Ignored because not readable: " + resource);
                        }
                    }
                }
            } catch (IOException ex) {
                throw new BeanDefinitionStoreException("I/O failure during classpath scanning", ex);
            }
            return candidates;
        }

        @Override
        protected boolean isCandidateComponent(MetadataReader metadataReader) throws IOException {
            return tf.match(metadataReader, getMetadataReaderFactory());
        }

        @Override
        protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
            AnnotationMetadata metadata = beanDefinition.getMetadata();
            return metadata.isInterface() && metadata.isIndependent();
        }

        @Override
        protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
            Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);

            if (beanDefinitions.isEmpty()) {
                logger.warn("No MyBatis mapper was found in '" + Arrays.toString(basePackages) + "' package. Please check your configuration.");
            } else {
                processBeanDefinitions(beanDefinitions);
            }

            return beanDefinitions;
        }

        private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
            GenericBeanDefinition definition;
            for (BeanDefinitionHolder holder : beanDefinitions) {
                definition = (GenericBeanDefinition) holder.getBeanDefinition();

                definition.getConstructorArgumentValues().addGenericArgumentValue(definition.getBeanClassName()); // issue #59
                definition.setBeanClass(SqlConfigProxy.class);
            }
        }
    }

    public static class QeMapperSanAutoConfigureRegistrar implements ImportBeanDefinitionRegistrar {

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
            AnnotationAttributes mapperScanAttrs = AnnotationAttributes
                    .fromMap(importingClassMetadata.getAnnotationAttributes(QeMapperScan.class.getName()));

            QeMapperAutoConfigureRegistrar.packages = (String[]) mapperScanAttrs.get("value");
        }
    }
}
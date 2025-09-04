package org.springframework.context.annotation;

import org.springframework.beans.factory.parsing.FailFastProblemReporter;
import org.springframework.beans.factory.parsing.PassThroughSourceExtractor;
import org.springframework.beans.factory.parsing.ProblemReporter;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import java.io.IOException;
import java.util.Set;

public abstract class ImplicitClassConfigurationUtils {

    static public void process(BeanDefinitionRegistry registry,
                               Environment environment,
                               ResourceLoader resourceLoader,
                               Class<?> implicitClass) {
        BeanNameGenerator componentScanBeanNameGenerator = AnnotationBeanNameGenerator.INSTANCE;
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory();
        ProblemReporter problemReporter = new FailFastProblemReporter();
        PassThroughSourceExtractor passThroughSourceExtractor = new PassThroughSourceExtractor();
        FullyQualifiedAnnotationBeanNameGenerator beanNameGenerator = FullyQualifiedAnnotationBeanNameGenerator.INSTANCE;
        ConfigurationClassParser ccp = new ConfigurationClassParser(
                metadataReaderFactory,
                problemReporter,
                environment,
                resourceLoader,
                componentScanBeanNameGenerator,
                registry
        );
        ConfigurationClass parsedConfigurationClass = null;
        try {
            parsedConfigurationClass = ccp
                    .parse(implicitClass.getName(), implicitClass.getSimpleName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ConfigurationClassBeanDefinitionReader ccbdr = new ConfigurationClassBeanDefinitionReader(registry,
                passThroughSourceExtractor,
                resourceLoader,
                environment,
                beanNameGenerator,
                ccp.getImportRegistry());
        ccbdr.loadBeanDefinitions(Set.of(parsedConfigurationClass));
    }
}

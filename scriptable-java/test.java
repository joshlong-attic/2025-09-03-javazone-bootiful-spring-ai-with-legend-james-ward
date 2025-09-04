import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImplicitClassConfigurationUtils;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

static AtomicReference<Class<?>> IMPLICIT_CLASS_NAME = new AtomicReference<>();

void main() {
    IMPLICIT_CLASS_NAME.set(getClass());

    var modifiers = getClass().getModifiers();
    if (Modifier.isFinal(modifiers)) {
        System.out.println("the outer class name is " + IMPLICIT_CLASS_NAME.get().getName() + ", and it's final!");
    }

    SpringApplication.run(MyConfig.class);

}

static class ImplicitClassBeanDefinitionReader
        implements BeanDefinitionRegistryPostProcessor, PriorityOrdered,
        ResourceLoaderAware, EnvironmentAware {


    private ResourceLoader resourceLoader;
    private Environment environment;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

        AbstractBeanDefinition bd = BeanDefinitionBuilder
                .genericBeanDefinition(IMPLICIT_CLASS_NAME.get())
                .getBeanDefinition();
        String scriptApplication = IMPLICIT_CLASS_NAME.get().getSimpleName();// "scriptApplication";
        if (!registry.containsBeanDefinition(scriptApplication)) {
            registry.registerBeanDefinition(scriptApplication, bd);
        }
        ImplicitClassConfigurationUtils
                .process(registry, environment,
                        resourceLoader, IMPLICIT_CLASS_NAME.get());


    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}

@Bean
ApplicationRunner mooRunner() {
    return _ -> System.out.println("moo");
}

@EnableAutoConfiguration
@Configuration(proxyBeanMethods = false)
static final class MyConfig {

    @Bean
    static ImplicitClassBeanDefinitionReader implicitClassBeanDefinitionReader() {
        return new ImplicitClassBeanDefinitionReader();
    }

    @Bean
    ApplicationRunner runner() {
        return args -> System.out.println("runner..");
    }
}
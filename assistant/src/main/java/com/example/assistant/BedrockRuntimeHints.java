package com.example.assistant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Registers the types for GraalVM native image applications with Bedrock.
 *
 * @author Josh Long
 */
class BedrockRuntimeHints
		implements BeanClassLoaderAware, EnvironmentAware, ResourceLoaderAware, RuntimeHintsRegistrar {

	private final Logger log = LoggerFactory.getLogger(BedrockRuntimeHints.class);

	private static final MemberCategory[] ALL_CATEGORIES = MemberCategory.values();

	private final AtomicReference<ClassLoader> classLoader = new AtomicReference<>();

	private final AtomicReference<ResourceLoader> resourceLoader = new AtomicReference<>();

	private final AtomicReference<Environment> environment = new AtomicReference<>();

	/**
	 * visible for testing
	 */
	protected List<TypeReference> find(String packageName) {
		var scanner = new ClassPathScanningCandidateComponentProvider(false) {
			@Override
			protected boolean isCandidateComponent(MetadataReader metadataReader) throws IOException {
				return true;
			}

			@Override
			protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
				return true;
			}

		};
		if (this.environment.get() != null) {
			scanner.setEnvironment(this.environment.get());
			this.log.debug("setting the environment ");
		}
		if (this.resourceLoader.get() != null) {
			scanner.setResourceLoader(this.resourceLoader.get());
			this.log.debug("setting the resource loader");
		}

		return scanner //
			.findCandidateComponents(packageName) //
			.stream()//
			.map(BeanDefinition::getBeanClassName) //
			.filter(Objects::nonNull) //
			.filter(x -> !x.contains("package-info"))
			.map(TypeReference::of) //
			.toList();

	}

	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
		registerBedrockRuntimeService(hints, classLoader);

		// Register serialization support
		registerSerializationClasses(hints, classLoader);

		// Register resources
		registerResources(hints);
	}

	private void registerBedrockRuntimeService(RuntimeHints hints, ClassLoader classLoader) {
		var pkg = "software.amazon.awssdk.services.bedrockruntime";

		// Client classes
		register(hints, classLoader, pkg + ".BedrockRuntimeClient");
		register(hints, classLoader, pkg + ".BedrockRuntimeAsyncClient");
		register(hints, classLoader, pkg + ".BedrockRuntimeClientBuilder");
		register(hints, classLoader, pkg + ".BedrockRuntimeAsyncClientBuilder");

		// Exception classes
		var modelPkg = pkg + ".model";
		register(hints, classLoader, modelPkg + ".AccessDeniedException");
		register(hints, classLoader, modelPkg + ".InternalServerException");
		register(hints, classLoader, modelPkg + ".ModelErrorException");
		register(hints, classLoader, modelPkg + ".ModelNotReadyException");
		register(hints, classLoader, modelPkg + ".ModelStreamErrorException");
		register(hints, classLoader, modelPkg + ".ModelTimeoutException");
		register(hints, classLoader, modelPkg + ".ResourceNotFoundException");
		register(hints, classLoader, modelPkg + ".ServiceQuotaExceededException");
		register(hints, classLoader, modelPkg + ".ServiceUnavailableException");
		register(hints, classLoader, modelPkg + ".ThrottlingException");
		register(hints, classLoader, modelPkg + ".ValidationException");

		// Core inference operations
		register(hints, classLoader, modelPkg + ".InvokeModelRequest");
		register(hints, classLoader, modelPkg + ".InvokeModelResponse");
		register(hints, classLoader, modelPkg + ".InvokeModelWithResponseStreamRequest");
		register(hints, classLoader, modelPkg + ".InvokeModelWithResponseStreamResponse");

		// Converse API
		register(hints, classLoader, modelPkg + ".ConverseRequest");
		register(hints, classLoader, modelPkg + ".ConverseResponse");
		register(hints, classLoader, modelPkg + ".ConverseStreamRequest");
		register(hints, classLoader, modelPkg + ".ConverseStreamResponse");

		// Token counting
		register(hints, classLoader, modelPkg + ".CountTokensRequest");
		register(hints, classLoader, modelPkg + ".CountTokensResponse");

		// Async invocations
		register(hints, classLoader, modelPkg + ".StartAsyncInvokeRequest");
		register(hints, classLoader, modelPkg + ".StartAsyncInvokeResponse");
		register(hints, classLoader, modelPkg + ".GetAsyncInvokeRequest");
		register(hints, classLoader, modelPkg + ".GetAsyncInvokeResponse");
		register(hints, classLoader, modelPkg + ".ListAsyncInvokesRequest");
		register(hints, classLoader, modelPkg + ".ListAsyncInvokesResponse");

		// Guardrails
		register(hints, classLoader, modelPkg + ".ApplyGuardrailRequest");
		register(hints, classLoader, modelPkg + ".ApplyGuardrailResponse");
		register(hints, classLoader, modelPkg + ".GuardrailConfiguration");
		register(hints, classLoader, modelPkg + ".GuardrailAssessment");
		register(hints, classLoader, modelPkg + ".GuardrailContentFilter");
		register(hints, classLoader, modelPkg + ".GuardrailTrace");
		register(hints, classLoader, modelPkg + ".GuardrailUsage");

		// Core message/content types
		register(hints, classLoader, modelPkg + ".Message");
		register(hints, classLoader, modelPkg + ".ContentBlock");
		register(hints, classLoader, modelPkg + ".ContentBlockDelta");
		register(hints, classLoader, modelPkg + ".ContentBlockStart");
		register(hints, classLoader, modelPkg + ".ConverseOutput");
		register(hints, classLoader, modelPkg + ".ConverseMetrics");

		// Media blocks
		register(hints, classLoader, modelPkg + ".TextBlock");
		register(hints, classLoader, modelPkg + ".ImageBlock");
		register(hints, classLoader, modelPkg + ".ImageSource");
		register(hints, classLoader, modelPkg + ".DocumentBlock");
		register(hints, classLoader, modelPkg + ".DocumentSource");
		register(hints, classLoader, modelPkg + ".AudioBlock");
		register(hints, classLoader, modelPkg + ".AudioSource");
		register(hints, classLoader, modelPkg + ".VideoBlock");
		register(hints, classLoader, modelPkg + ".VideoSource");

		// Tool use
		register(hints, classLoader, modelPkg + ".Tool");
		register(hints, classLoader, modelPkg + ".ToolChoice");
		register(hints, classLoader, modelPkg + ".ToolConfiguration");
		register(hints, classLoader, modelPkg + ".ToolUseBlock");
		register(hints, classLoader, modelPkg + ".ToolResultBlock");
		register(hints, classLoader, modelPkg + ".ToolSpecification");

		// Configuration
		register(hints, classLoader, modelPkg + ".InferenceConfiguration");
		register(hints, classLoader, modelPkg + ".SystemContentBlock");
		register(hints, classLoader, modelPkg + ".TokenUsage");
	}

	private String resolveSubpackage(String pkg) {
		return ROOT_PACKAGE + "." + pkg;
	}

	private void debug(String pkg) {
		IO.println("[======]");
		IO.println(pkg.toUpperCase(Locale.ROOT));
		this.find(pkg).forEach(x -> IO.println(x.getName()));
		IO.println("[======]");
	}

	static final String ROOT_PACKAGE = "software.amazon.awssdk";

	private final Collection<TypeReference> ALL = find(ROOT_PACKAGE);

	private void registerSerializationClasses(RuntimeHints hints, ClassLoader classLoader) {
		for (var c : ALL) {
			try {
				var clzz = ClassUtils.forName(c.getName(), getClass().getClassLoader());
				if (Serializable.class.isAssignableFrom(clzz)) {
					this.register(hints, classLoader, clzz.getName());
					hints.serialization().registerType(c);
				}
			} //
			catch (Throwable e) {
				//
			}
		}
	}

	/**
	 * Register resources needed at runtime
	 */
	private void registerResources(RuntimeHints hints) {
		// Service model resources
		hints.resources().registerPattern("software/amazon/awssdk/services/bedrock*/codegen-resources/service-2.json");
		hints.resources()
			.registerPattern("software/amazon/awssdk/services/bedrock*/codegen-resources/endpoint-rule-set.json");
		hints.resources()
			.registerPattern("software/amazon/awssdk/services/bedrock*/codegen-resources/paginators-1.json");
		hints.resources().registerPattern("software/amazon/awssdk/services/bedrock*/codegen-resources/waiters-2.json");

		// Partition data
		hints.resources().registerPattern("software/amazon/awssdk/global/handlers/execution.interceptors");
	}

	private void register(RuntimeHints hints, ClassLoader classLoader, String className) {
		for (var clzz : new String[] { className, className + "$Builder", className + "$$SerializableBuilder" }) {
			try {
				var clazz = Class.forName(clzz, false, classLoader);
				hints.reflection().registerType(TypeReference.of(clazz), ALL_CATEGORIES);
			}
			catch (Throwable e) {
				//
			}
		}

	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment.set(environment);
		log.info("environment set");
	}

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader.set(resourceLoader);
		log.info("resource loader set");
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.classLoader.set(classLoader);
		log.info("BeanClassLoader set");
	}

}

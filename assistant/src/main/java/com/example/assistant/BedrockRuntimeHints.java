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
	List<TypeReference> find(String packageName) {
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
		if (this.environment.get() != null)
			scanner.setEnvironment(this.environment.get());

		if (this.resourceLoader.get() != null)
			scanner.setResourceLoader(this.resourceLoader.get());

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
		// Register all services
		registerBedrockService(hints, classLoader);
		registerBedrockRuntimeService(hints, classLoader);
		registerBedrockAgentService(hints, classLoader);
		registerBedrockAgentRuntimeService(hints, classLoader);
		// registerBedrockAgentCoreService(hints, classLoader);
		// registerBedrockAgentCoreControlService(hints, classLoader);
		// registerBedrockDataAutomationService(hints, classLoader);
		// registerBedrockDataAutomationRuntimeService(hints, classLoader);

		// Register serialization support
		registerSerializationClasses(hints, classLoader);

		// Register resources
		registerResources(hints);
	}

	/**
	 * Register Bedrock control plane service classes
	 */
	private void registerBedrockService(RuntimeHints hints, ClassLoader classLoader) {
		var pkg = "software.amazon.awssdk.services.bedrock";

		// Client classes
		register(hints, classLoader, pkg + ".BedrockClient");
		register(hints, classLoader, pkg + ".BedrockAsyncClient");
		register(hints, classLoader, pkg + ".BedrockClientBuilder");
		register(hints, classLoader, pkg + ".BedrockAsyncClientBuilder");
		register(hints, classLoader, pkg + ".BedrockServiceClientConfiguration");

		// Exception classes
		var modelPkg = pkg + ".model";
		register(hints, classLoader, modelPkg + ".AccessDeniedException");
		register(hints, classLoader, modelPkg + ".ConflictException");
		register(hints, classLoader, modelPkg + ".InternalServerException");
		register(hints, classLoader, modelPkg + ".ResourceNotFoundException");
		register(hints, classLoader, modelPkg + ".ServiceQuotaExceededException");
		register(hints, classLoader, modelPkg + ".ThrottlingException");
		register(hints, classLoader, modelPkg + ".TooManyTagsException");
		register(hints, classLoader, modelPkg + ".ValidationException");

		// Key request/response classes - major operations
		register(hints, classLoader, modelPkg + ".CreateGuardrailRequest");
		register(hints, classLoader, modelPkg + ".CreateGuardrailResponse");
		register(hints, classLoader, modelPkg + ".GetGuardrailRequest");
		register(hints, classLoader, modelPkg + ".GetGuardrailResponse");
		register(hints, classLoader, modelPkg + ".UpdateGuardrailRequest");
		register(hints, classLoader, modelPkg + ".UpdateGuardrailResponse");
		register(hints, classLoader, modelPkg + ".DeleteGuardrailRequest");
		register(hints, classLoader, modelPkg + ".DeleteGuardrailResponse");
		register(hints, classLoader, modelPkg + ".ListGuardrailsRequest");
		register(hints, classLoader, modelPkg + ".ListGuardrailsResponse");

		register(hints, classLoader, modelPkg + ".CreateCustomModelRequest");
		register(hints, classLoader, modelPkg + ".CreateCustomModelResponse");
		register(hints, classLoader, modelPkg + ".GetCustomModelRequest");
		register(hints, classLoader, modelPkg + ".GetCustomModelResponse");
		register(hints, classLoader, modelPkg + ".ListCustomModelsRequest");
		register(hints, classLoader, modelPkg + ".ListCustomModelsResponse");

		register(hints, classLoader, modelPkg + ".GetFoundationModelRequest");
		register(hints, classLoader, modelPkg + ".GetFoundationModelResponse");
		register(hints, classLoader, modelPkg + ".ListFoundationModelsRequest");
		register(hints, classLoader, modelPkg + ".ListFoundationModelsResponse");

		register(hints, classLoader, modelPkg + ".CreateProvisionedModelThroughputRequest");
		register(hints, classLoader, modelPkg + ".CreateProvisionedModelThroughputResponse");
		register(hints, classLoader, modelPkg + ".GetProvisionedModelThroughputRequest");
		register(hints, classLoader, modelPkg + ".GetProvisionedModelThroughputResponse");
		register(hints, classLoader, modelPkg + ".UpdateProvisionedModelThroughputRequest");
		register(hints, classLoader, modelPkg + ".UpdateProvisionedModelThroughputResponse");

		register(hints, classLoader, modelPkg + ".TagResourceRequest");
		register(hints, classLoader, modelPkg + ".TagResourceResponse");
		register(hints, classLoader, modelPkg + ".UntagResourceRequest");
		register(hints, classLoader, modelPkg + ".UntagResourceResponse");

		// Core model classes
		register(hints, classLoader, modelPkg + ".FoundationModelSummary");
		register(hints, classLoader, modelPkg + ".GuardrailSummary");
		register(hints, classLoader, modelPkg + ".CustomModelSummary");
		register(hints, classLoader, modelPkg + ".ProvisionedModelSummary");
		register(hints, classLoader, modelPkg + ".Tag");
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

	/**
	 * Register Bedrock Agent service classes
	 */
	private void registerBedrockAgentService(RuntimeHints hints, ClassLoader classLoader) {
		String pkg = "software.amazon.awssdk.services.bedrockagent";

		// Client classes
		register(hints, classLoader, pkg + ".BedrockAgentClient");
		register(hints, classLoader, pkg + ".BedrockAgentAsyncClient");

		// Exception classes
		String modelPkg = pkg + ".model";
		register(hints, classLoader, modelPkg + ".AccessDeniedException");
		register(hints, classLoader, modelPkg + ".ConflictException");
		register(hints, classLoader, modelPkg + ".ResourceNotFoundException");
		register(hints, classLoader, modelPkg + ".ServiceQuotaExceededException");
		register(hints, classLoader, modelPkg + ".ThrottlingException");
		register(hints, classLoader, modelPkg + ".ValidationException");

		// Key operations
		register(hints, classLoader, modelPkg + ".CreateAgentRequest");
		register(hints, classLoader, modelPkg + ".CreateAgentResponse");
		register(hints, classLoader, modelPkg + ".GetAgentRequest");
		register(hints, classLoader, modelPkg + ".GetAgentResponse");
		register(hints, classLoader, modelPkg + ".UpdateAgentRequest");
		register(hints, classLoader, modelPkg + ".UpdateAgentResponse");
		register(hints, classLoader, modelPkg + ".DeleteAgentRequest");
		register(hints, classLoader, modelPkg + ".DeleteAgentResponse");
		register(hints, classLoader, modelPkg + ".ListAgentsRequest");
		register(hints, classLoader, modelPkg + ".ListAgentsResponse");

		register(hints, classLoader, modelPkg + ".CreateKnowledgeBaseRequest");
		register(hints, classLoader, modelPkg + ".CreateKnowledgeBaseResponse");
		register(hints, classLoader, modelPkg + ".GetKnowledgeBaseRequest");
		register(hints, classLoader, modelPkg + ".GetKnowledgeBaseResponse");
		register(hints, classLoader, modelPkg + ".UpdateKnowledgeBaseRequest");
		register(hints, classLoader, modelPkg + ".UpdateKnowledgeBaseResponse");

		register(hints, classLoader, modelPkg + ".CreateDataSourceRequest");
		register(hints, classLoader, modelPkg + ".CreateDataSourceResponse");
		register(hints, classLoader, modelPkg + ".StartIngestionJobRequest");
		register(hints, classLoader, modelPkg + ".StartIngestionJobResponse");

		// Core model classes
		register(hints, classLoader, modelPkg + ".Agent");
		register(hints, classLoader, modelPkg + ".AgentSummary");
		register(hints, classLoader, modelPkg + ".KnowledgeBase");
		register(hints, classLoader, modelPkg + ".KnowledgeBaseConfiguration");
		register(hints, classLoader, modelPkg + ".DataSource");
		register(hints, classLoader, modelPkg + ".DataSourceConfiguration");
	}

	private String resolveSubpackage(String pkg) {
		return ROOT_PACKAGE + "." + pkg;
	}

	/**
	 * Register Bedrock Agent Runtime service classes
	 */
	private void registerBedrockAgentRuntimeService(RuntimeHints hints, ClassLoader classLoader) {
		var pkg = "software.amazon.awssdk.services.bedrockagentruntime";

		// Client classes
		register(hints, classLoader, pkg + ".BedrockAgentRuntimeClient");
		register(hints, classLoader, pkg + ".BedrockAgentRuntimeAsyncClient");

		// Exception classes
		String modelPkg = pkg + ".model";
		register(hints, classLoader, modelPkg + ".AccessDeniedException");
		register(hints, classLoader, modelPkg + ".BadGatewayException");
		register(hints, classLoader, modelPkg + ".ConflictException");
		register(hints, classLoader, modelPkg + ".DependencyFailedException");
		register(hints, classLoader, modelPkg + ".InternalServerException");
		register(hints, classLoader, modelPkg + ".ResourceNotFoundException");
		register(hints, classLoader, modelPkg + ".ThrottlingException");
		register(hints, classLoader, modelPkg + ".ValidationException");

		// Core operations
		register(hints, classLoader, modelPkg + ".InvokeAgentRequest");
		register(hints, classLoader, modelPkg + ".InvokeAgentResponse");
		register(hints, classLoader, modelPkg + ".InvokeFlowRequest");
		register(hints, classLoader, modelPkg + ".InvokeFlowResponse");
		register(hints, classLoader, modelPkg + ".RetrieveRequest");
		register(hints, classLoader, modelPkg + ".RetrieveResponse");
		register(hints, classLoader, modelPkg + ".RetrieveAndGenerateRequest");
		register(hints, classLoader, modelPkg + ".RetrieveAndGenerateResponse");
		register(hints, classLoader, modelPkg + ".RerankRequest");
		register(hints, classLoader, modelPkg + ".RerankResponse");

		// Model classes
		register(hints, classLoader, modelPkg + ".KnowledgeBaseRetrievalResult");
		register(hints, classLoader, modelPkg + ".RetrievalResult");
		register(hints, classLoader, modelPkg + ".Citation");
		register(hints, classLoader, modelPkg + ".Trace");
		register(hints, classLoader, modelPkg + ".OrchestrationTrace");
	}

	// /**
	// * Register Bedrock Agent Core service classes
	// */
	// private void registerBedrockAgentCoreService(RuntimeHints hints, ClassLoader
	// classLoader) {
	// var pkg = "software.amazon.awssdk.services.bedrockagentcore";
	// this.find(pkg).forEach(k -> log.info("found agentcore {} in {}", k.getName(),
	// pkg));
	// register(hints, classLoader, pkg + ".BedrockAgentCoreClient");
	// register(hints, classLoader, pkg + ".BedrockAgentCoreAsyncClient");
	//
	// var modelPkg = pkg + ".model";
	// register(hints, classLoader, modelPkg + ".GetMemoryRecordInput");
	// register(hints, classLoader, modelPkg + ".GetMemoryRecordOutput");
	// register(hints, classLoader, modelPkg + ".ListMemoryRecordsInput");
	// register(hints, classLoader, modelPkg + ".ListMemoryRecordsOutput");
	// }

	/**
	 * Register Bedrock Agent Core Control service classes
	 */
	// private void registerBedrockAgentCoreControlService(RuntimeHints hints, ClassLoader
	// classLoader) {
	// String pkg = "software.amazon.awssdk.services.bedrockagentcorecontrol";
	// register(hints, classLoader, pkg + ".BedrockAgentCoreControlClient");
	// register(hints, classLoader, pkg + ".BedrockAgentCoreControlAsyncClient");
	//
	// String modelPkg = pkg + ".model";
	// register(hints, classLoader, modelPkg + ".CreateMemoryInput");
	// register(hints, classLoader, modelPkg + ".CreateMemoryOutput");
	// register(hints, classLoader, modelPkg + ".GetMemoryInput");
	// register(hints, classLoader, modelPkg + ".GetMemoryOutput");
	// }

	/**
	 * Register Bedrock Data Automation service classes
	 *//*
		 * private void registerBedrockDataAutomationService(RuntimeHints hints,
		 * ClassLoader classLoader) { String pkg =
		 * "software.amazon.awssdk.services.bedrockdataautomation"; register(hints,
		 * classLoader, pkg + ".BedrockDataAutomationClient"); register(hints,
		 * classLoader, pkg + ".BedrockDataAutomationAsyncClient");
		 *
		 * String modelPkg = pkg + ".model"; register(hints, classLoader, modelPkg +
		 * ".CreateDataAutomationProjectRequest"); register(hints, classLoader, modelPkg +
		 * ".CreateDataAutomationProjectResponse"); register(hints, classLoader, modelPkg
		 * + ".GetDataAutomationProjectRequest"); register(hints, classLoader, modelPkg +
		 * ".GetDataAutomationProjectResponse"); }
		 */
	/*
	 */
	/**
	 * Register Bedrock Data Automation Runtime service classes
	 *//*
		 * private void registerBedrockDataAutomationRuntimeService(RuntimeHints hints,
		 * ClassLoader classLoader) { var clzz = new TypeReference[]{
		 *
		 * BedrockDataAutomationRuntimeClient.class };
		 *
		 * var pkg = "software.amazon.awssdk.services.bedrockdataautomationruntime";
		 * this.register(hints, classLoader, pkg + ".BedrockDataAutomationRuntimeClient");
		 * this.register(hints, classLoader, pkg +
		 * ".BedrockDataAutomationRuntimeAsyncClient");
		 *
		 * var modelPkg = pkg + ".model"; this.register(hints, classLoader, modelPkg +
		 * ".InvokeDataAutomationRequest"); this.register(hints, classLoader, modelPkg +
		 * ".InvokeDataAutomationResponse"); }
		 */

	static final String ROOT_PACKAGE = "software.amazon.awssdk";

	private final Collection<TypeReference> ALL = find(ROOT_PACKAGE);

	private void registerSerializationClasses(RuntimeHints hints, ClassLoader classLoader) {
		for (var c : ALL) {
			try {
				var clzz = ClassUtils.forName(c.getName(), getClass().getClassLoader());
				if (Serializable.class.isAssignableFrom(clzz)) {
					this.log.info("Registering serializable class {}", clzz);
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

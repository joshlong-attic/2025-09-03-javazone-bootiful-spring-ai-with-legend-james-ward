package com.example.assistant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * Registers the types for GraalVM native image applications with Bedrock.
 *
 * @author Josh Long
 */
class BedrockRuntimeHints implements RuntimeHintsRegistrar {

	private final String rootPackage = "software.amazon.awssdk";

	private final Logger log = LoggerFactory.getLogger(BedrockRuntimeHints.class);

	private final MemberCategory[] memberCategories = MemberCategory.values();

	private final Collection<TypeReference> allClasses;

	BedrockRuntimeHints() {
		this.allClasses = this.find(rootPackage);
	}

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
		registerSerializationClasses(hints, classLoader);
		registerResources(hints);
	}

	private void registerBedrockRuntimeService(RuntimeHints hints, ClassLoader classLoader) {
		var pkg = rootPackage + ".services.bedrockruntime";
		var all = new HashSet<TypeReference>();
		for (var clzz : this.allClasses) {
			if (clzz.getName().contains("Bedrock") && clzz.getName().contains("Client"))
				all.add(clzz);
		}
		var modelPkg = pkg + ".model";
		all.addAll(this.find(modelPkg));
		all.forEach(tr -> hints.reflection().registerType(tr, this.memberCategories));
	}

	private void registerSerializationClasses(RuntimeHints hints, ClassLoader classLoader) {
		for (var c : this.allClasses) {
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
		for (var clzz : new String[] { className, className + "$Builder", className + "$BuilderImpl",
				className + "$SerializableBuilder" }) {
			try {
				var clazz = Class.forName(clzz, false, classLoader);
				hints.reflection().registerType(TypeReference.of(clazz), memberCategories);
			} //
			catch (Throwable e) {
				this.log.trace("Failed to register Bedrock class {}", className, e);
			}
		}
	}

}

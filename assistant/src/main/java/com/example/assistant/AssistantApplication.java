package com.example.assistant;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.sql.DataSource;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@ImportRuntimeHints(BedrockRuntimeHints.class)
@SpringBootApplication
public class AssistantApplication {

	public static void main(String[] args) {
		SpringApplication.run(AssistantApplication.class, args);
	}

	@Bean
	QuestionAnswerAdvisor questionAnswerAdvisor(VectorStore dataSource) {
		return QuestionAnswerAdvisor.builder(dataSource).build();
	}

	@Bean
	PromptChatMemoryAdvisor promptChatMemoryAdvisor(DataSource dataSource) {
		var jdbc = JdbcChatMemoryRepository.builder().dataSource(dataSource).build();
		var mwa = MessageWindowChatMemory.builder().chatMemoryRepository(jdbc).build();
		return PromptChatMemoryAdvisor.builder(mwa).build();
	}

	@Bean
	JdbcClient jdbcClient(DataSource dataSource) {
		return JdbcClient.create(dataSource);
	}

}

interface DogRepository extends ListCrudRepository<Dog, Integer> {

}

record Dog(@Id int id, String description, String owner, String name) {
}

@Controller
@ResponseBody
@ImportRuntimeHints(AssistantController.Hints.class)
class AssistantController {

	static class Hints implements RuntimeHintsRegistrar {

		@Override
		public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
			hints.reflection().registerType(DogAdoptionSuggestion.class, MemberCategory.values());
		}

	}

	private final ChatClient ai;

	AssistantController(ChatClient.Builder ai, JdbcClient db, DogAdoptionScheduler scheduler,
			QuestionAnswerAdvisor questionAnswerAdvisor, VectorStore vectorStore, DogRepository repository,
			PromptChatMemoryAdvisor promptChatMemoryAdvisor) {

		if (db.sql("select count(id) as c from vector_store ")//
			.query((rs, rowNum) -> rs.getLong("c")) //
			.single() //
			.intValue() == 0) {
			repository.findAll().forEach(dog -> {
				var dogument = new Document(
						"id: %s, name: %s, description: %s".formatted(dog.id(), dog.name(), dog.description()));
				vectorStore.add(List.of(dogument));
			});
		}
		var prompt = """
				You are an AI powered assistant to help people adopt a dog from the adoption\s
				agency named Pooch Palace with locations in Oslo, Seoul, Denver, Tokyo, Singapore, Paris,\s
				Mumbai, New Delhi, Barcelona, San Francisco, and London. Information about the dogs available\s
				will be presented below. If there is no information, then return a polite response suggesting we\s
				don't have any dogs available.
				""";
		this.ai = ai.defaultSystem(prompt)
			.defaultTools(scheduler)
			// .defaultToolCallbacks(SyncMcpToolCallbackProvider.syncToolCallbacks(schedulerMcpClient))
			.defaultAdvisors(promptChatMemoryAdvisor, questionAnswerAdvisor)
			.build();
	}

	@GetMapping("/askso")
	DogAdoptionSuggestion questionStructuredOutput(@RequestParam String question) {
		return this.ai.prompt(question).call().entity(DogAdoptionSuggestion.class);
	}

	@GetMapping("/ask")
	Map<String, String> question(@RequestParam String question) {
		return Map.of("reply", Objects.requireNonNull(this.ai.prompt(question).call().content()));
	}

}

record DogAdoptionSuggestion(int id, String name, String description) {
}

@Component
class DogAdoptionScheduler {

	@Tool(description = "schedule an appointment to pick up or adopt a dog from a Pooch Palace location")
	String schedule(@ToolParam(description = "the id of the dog") int dogId,
			@ToolParam(description = "the name of the dog") String dogName) {
		var i = Instant.now().plus(3, ChronoUnit.DAYS).toString();
		IO.println("scheduling " + dogId + '/' + dogName + " for " + i);
		return i;
	}

}

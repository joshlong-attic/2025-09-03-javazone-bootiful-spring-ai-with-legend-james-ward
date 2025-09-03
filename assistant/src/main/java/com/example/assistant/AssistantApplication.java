package com.example.assistant;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.sql.DataSource;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class AssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(AssistantApplication.class, args);
    }

    @Bean
    PromptChatMemoryAdvisor promptChatMemoryAdvisor(DataSource dataSource) {
        var jdbc = JdbcChatMemoryRepository
                .builder()
                .dataSource(dataSource)
                .build();
        var mwa = MessageWindowChatMemory
                .builder()
                .chatMemoryRepository(jdbc)
                .build();
        return PromptChatMemoryAdvisor
                .builder(mwa)
                .build();
    }

    @Bean
    QuestionAnswerAdvisor questionAnswerAdvisor(VectorStore vectorStore) {
        return new QuestionAnswerAdvisor(vectorStore);
    }

    // todo ignore this
//    @Bean
    ApplicationRunner vectorAllTheThings(DogRepository repository, VectorStore vectorStore) {
        return _ -> repository
                .findAll()
                .forEach(dog -> {
                    var dogument = new Document("id: %s, name: %s, description: %s".formatted(
                            dog.id(), dog.name(), dog.description()
                    ));
                    vectorStore.add(List.of(dogument));
                });
    }

    @Bean
    McpSyncClient schedulerMcpSyncClient() {
        var mcp = McpClient
                .sync(HttpClientSseClientTransport.builder("http://localhost:8084").build())
                .build();
        mcp.initialize();
        return mcp;
    }
}

interface DogRepository extends ListCrudRepository<Dog, Integer> {
}

record Dog(@Id int id, String name, String owner, String description) {
}

@Controller
@ResponseBody
class AssistantController {

    private final ChatClient ai;

    AssistantController(
            McpSyncClient schedulerMcpSyncClient,
            PromptChatMemoryAdvisor memory,
            QuestionAnswerAdvisor qa,
            ChatClient.Builder ai) {

        this.ai = ai
                .defaultSystem("""
                        You are an AI powered assistant to help people adopt a dog from the adoption\s
                        agency named Pooch Palace with locations in Antwerp, Seoul, Tokyo, Singapore, Paris,\s
                        Mumbai, New Delhi, Barcelona, San Francisco, and London. Information about the dogs available\s
                        will be presented below. If there is no information, then return a polite response suggesting we\s
                        don't have any dogs available.
                        """)
                .defaultAdvisors(memory, qa)
                .defaultToolCallbacks(new SyncMcpToolCallbackProvider(schedulerMcpSyncClient))
                .build();
    }

    @GetMapping("/ask")
    Map<String, String> question(
            Principal principal,
            @RequestParam String question
    ) {
        return Map.of("reply", this.ai
                .prompt(question)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, principal.getName()))
                .call()
                .content());
    }

}
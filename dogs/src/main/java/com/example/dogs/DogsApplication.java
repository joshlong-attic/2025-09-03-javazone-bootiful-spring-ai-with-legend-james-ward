package com.example.dogs;

import org.springframework.beans.factory.BeanRegistrar;
import org.springframework.beans.factory.BeanRegistry;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Import(DogsApplication.MyBeanRegistrar.class)
@SpringBootApplication
public class DogsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DogsApplication.class, args);
    }

    static class MyRunner implements ApplicationRunner {

        @Override
        public void run(ApplicationArguments args) throws Exception {
            System.out.println("hello");
        }
    }

//    @Bean
//    ApplicationRunner runner() {
//        return new MyRunner();
//    }
//
    static class MyBeanRegistrar implements BeanRegistrar {

        @Override
        public void register(BeanRegistry registry, Environment env) {
            registry.registerBean(MyRunner.class , spec -> spec.description("the runner"));
        }
    }
}


@Controller
@ResponseBody
class MeController {

    @GetMapping("/me")
    Map<String, String> me(Principal principal) {
        return Map.of("name", principal.getName());
    }
}

@Controller
@ResponseBody
class DogController {

    private final DogRepository repository;

    DogController(DogRepository repository) {
        this.repository = repository;
    }

    @GetMapping(value = "/dogs", version = "1.1")
    Collection<Dog> dogs(Principal principal) {
        return this.repository.findByOwner(principal.getName());
    }

    @GetMapping(value = "/dogs", version = "1.0")
    List<Map<String, String>> dogsLegacy() {
        return repository.findAll()
                .stream()
                .map(dog -> Map.of("fullName", dog.name(), "description", dog.description()))
                .toList();
    }
}

interface DogRepository extends ListCrudRepository<Dog, Integer> {

    Collection <Dog> findByOwner(String owner);
}

record Dog(@Id int id, String name, String owner, String description) {
}

package com.example.dogs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.Collection;
import java.util.Map;

@SpringBootApplication
public class DogsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DogsApplication.class, args);
    }
}

@Controller
@ResponseBody
class MeController {

    @GetMapping ("/me")
    Map <String, Object> me(Principal principal) {
        return Map.of("name", principal.getName());
    }
}

@Controller
@ResponseBody
class DogsController {

    private final DogRepository repository;

    DogsController(DogRepository repository) {
        this.repository = repository;
    }

    // http :8080/dogs X-Dogs-Version:1.0
    @GetMapping(value = "/dogs", version = "1.0")
    Collection<Map<String, Object>> dogsv1() {
        var all = this.repository
                .findAll()
                .stream()
                .map(dog -> Map.of("id", (Object) dog.id(), "fullName", (Object) dog.name()))
                .toList();
        System.out.println("1.0 " + all);
        return all;
    }

    @GetMapping(value = "/dogs", version = "1.1")
    Collection<Dog> dogs(Principal principal) {
        return this.repository.findByOwner(principal.getName());
    }
}

interface DogRepository extends ListCrudRepository<Dog, Integer> {
    Collection <Dog> findByOwner(String owner);
}

record Dog(@Id int id, String owner, String name, String description) {
}

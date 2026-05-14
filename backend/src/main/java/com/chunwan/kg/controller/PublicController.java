package com.chunwan.kg.controller;

import com.chunwan.kg.entity.*;
import com.chunwan.kg.repository.*;
import com.chunwan.kg.service.GraphService;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PublicController {

    private final PersonRepository personRepository;
    private final ProgramRepository programRepository;
    private final YearRepository yearRepository;
    private final RoleRepository roleRepository;
    private final CategoryRepository categoryRepository;
    private final GraphService graphService;

    public PublicController(PersonRepository personRepository,
                            ProgramRepository programRepository,
                            YearRepository yearRepository,
                            RoleRepository roleRepository,
                            CategoryRepository categoryRepository,
                            GraphService graphService) {
        this.personRepository = personRepository;
        this.programRepository = programRepository;
        this.yearRepository = yearRepository;
        this.roleRepository = roleRepository;
        this.categoryRepository = categoryRepository;
        this.graphService = graphService;
    }

    @GetMapping("/persons")
    public List<Person> persons() {
        return personRepository.findAll();
    }

    @GetMapping("/programs")
    public List<Program> programs() {
        return programRepository.findAll();
    }

    @GetMapping("/years")
    public List<YearNode> years() {
        return yearRepository.findAll().stream().sorted(Comparator.comparingInt(YearNode::getYear)).toList();
    }

    @GetMapping("/roles")
    public List<RoleNode> roles() {
        return roleRepository.findAll();
    }

    @GetMapping("/categories")
    public List<CategoryNode> categories() {
        return categoryRepository.findAll();
    }

    @GetMapping("/graph")
    public Map<String, Object> graph() {
        return graphService.graphData();
    }

    @GetMapping("/suggest/persons")
    public List<Person> suggestPersons(@RequestParam String q) {
        return personRepository.findByNameContainingIgnoreCase(q);
    }

    @GetMapping("/suggest/programs")
    public List<Program> suggestPrograms(@RequestParam String q) {
        return programRepository.findByNameContainingIgnoreCase(q);
    }
}

package com.chunwan.kg.controller;

import com.chunwan.kg.dto.RelationRequest;
import com.chunwan.kg.entity.*;
import com.chunwan.kg.repository.*;
import com.chunwan.kg.service.BaikeCrawlerService;
import com.chunwan.kg.service.GraphService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final PersonRepository personRepository;
    private final ProgramRepository programRepository;
    private final YearRepository yearRepository;
    private final RoleRepository roleRepository;
    private final CategoryRepository categoryRepository;
    private final GraphService graphService;
    private final BaikeCrawlerService baikeCrawlerService;

    public AdminController(PersonRepository personRepository,
                           ProgramRepository programRepository,
                           YearRepository yearRepository,
                           RoleRepository roleRepository,
                           CategoryRepository categoryRepository,
                           GraphService graphService,
                           BaikeCrawlerService baikeCrawlerService) {
        this.personRepository = personRepository;
        this.programRepository = programRepository;
        this.yearRepository = yearRepository;
        this.roleRepository = roleRepository;
        this.categoryRepository = categoryRepository;
        this.graphService = graphService;
        this.baikeCrawlerService = baikeCrawlerService;
    }

    @PostMapping("/persons")
    public ResponseEntity<Person> createPerson(@RequestBody Person request) {
        if (personRepository.findByName(request.getName()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        request.setId(UUID.randomUUID().toString());
        return ResponseEntity.ok(personRepository.save(request));
    }

    @PutMapping("/persons/{id}")
    public ResponseEntity<Person> updatePerson(@PathVariable String id, @RequestBody Person request) {
        return personRepository.findById(id)
                .map(existing -> {
                    existing.setName(request.getName());
                    existing.setGender(request.getGender());
                    existing.setEthnicity(request.getEthnicity());
                    existing.setRegion(request.getRegion());
                    existing.setType(request.getType());
                    existing.setOverseas(request.getOverseas());
                    existing.setBaikeUrl(request.getBaikeUrl());
                    existing.setBio(request.getBio());
                    return ResponseEntity.ok(personRepository.save(existing));
                }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/persons/{id}")
    public ResponseEntity<Void> deletePerson(@PathVariable String id) {
        if (!personRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        personRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/programs")
    public ResponseEntity<Program> createProgram(@RequestBody Program request) {
        if (programRepository.findByName(request.getName()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        request.setId(UUID.randomUUID().toString());
        return ResponseEntity.ok(programRepository.save(request));
    }

    @PutMapping("/programs/{id}")
    public ResponseEntity<Program> updateProgram(@PathVariable String id, @RequestBody Program request) {
        return programRepository.findById(id)
                .map(existing -> {
                    existing.setName(request.getName());
                    existing.setType(request.getType());
                    existing.setContent(request.getContent());
                    return ResponseEntity.ok(programRepository.save(existing));
                }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/programs/{id}")
    public ResponseEntity<Void> deleteProgram(@PathVariable String id) {
        if (!programRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        programRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/years/{year}")
    public ResponseEntity<YearNode> createYear(@PathVariable Integer year) {
        if (yearRepository.findByYear(year).isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        YearNode node = new YearNode();
        node.setId(String.valueOf(year));
        node.setYear(year);
        return ResponseEntity.ok(yearRepository.save(node));
    }

    @DeleteMapping("/years/{id}")
    public ResponseEntity<Void> deleteYear(@PathVariable String id) {
        if (!yearRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        yearRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/programs/{programId}/year/{yearId}")
    public void bindProgramYear(@PathVariable String programId, @PathVariable String yearId) {
        graphService.linkProgramYear(programId, yearId);
    }

    @PostMapping("/roles")
    public ResponseEntity<RoleNode> createRole(@RequestBody RoleNode request) {
        if (roleRepository.findByName(request.getName()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        request.setId(UUID.randomUUID().toString());
        return ResponseEntity.ok(roleRepository.save(request));
    }

    @DeleteMapping("/roles/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable String id) {
        if (!roleRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        roleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryNode> createCategory(@RequestBody CategoryNode request) {
        if (categoryRepository.findByName(request.getName()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        request.setId(UUID.randomUUID().toString());
        return ResponseEntity.ok(categoryRepository.save(request));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable String id) {
        if (!categoryRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        categoryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/relations")
    public void createRelation(@Valid @RequestBody RelationRequest request) {
        graphService.upsertRelation(request);
    }

    @DeleteMapping("/relations")
    public void deleteRelation(@Valid @RequestBody RelationRequest request) {
        graphService.deleteRelation(request);
    }

    @PostMapping("/persons/crawl")
    public Person crawlByName(@RequestParam String name) {
        return baikeCrawlerService.crawlAndSave(name);
    }
}

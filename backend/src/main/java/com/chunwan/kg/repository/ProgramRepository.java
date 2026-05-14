package com.chunwan.kg.repository;

import com.chunwan.kg.entity.Program;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.List;
import java.util.Optional;

public interface ProgramRepository extends Neo4jRepository<Program, String> {
    Optional<Program> findByName(String name);
    List<Program> findByNameContainingIgnoreCase(String name);
}

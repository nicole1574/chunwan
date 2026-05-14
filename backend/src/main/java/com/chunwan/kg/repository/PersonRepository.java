package com.chunwan.kg.repository;

import com.chunwan.kg.entity.Person;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.List;
import java.util.Optional;

public interface PersonRepository extends Neo4jRepository<Person, String> {
    Optional<Person> findByName(String name);
    List<Person> findByNameContainingIgnoreCase(String name);
}

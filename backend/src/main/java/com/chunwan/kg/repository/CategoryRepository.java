package com.chunwan.kg.repository;

import com.chunwan.kg.entity.CategoryNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.Optional;

public interface CategoryRepository extends Neo4jRepository<CategoryNode, String> {
    Optional<CategoryNode> findByName(String name);
}

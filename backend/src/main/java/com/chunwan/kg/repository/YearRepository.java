package com.chunwan.kg.repository;

import com.chunwan.kg.entity.YearNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.Optional;

public interface YearRepository extends Neo4jRepository<YearNode, String> {
    Optional<YearNode> findByYear(Integer year);
}

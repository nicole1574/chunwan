package com.chunwan.kg.repository;

import com.chunwan.kg.entity.RoleNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.Optional;

public interface RoleRepository extends Neo4jRepository<RoleNode, String> {
    Optional<RoleNode> findByName(String name);
}

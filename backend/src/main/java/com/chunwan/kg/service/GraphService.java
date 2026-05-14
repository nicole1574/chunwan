package com.chunwan.kg.service;

import com.chunwan.kg.dto.RelationRequest;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GraphService {

    private static final Set<String> ALLOWED_RELATIONS = Set.of("参演", "导演", "主持");

    private final Neo4jClient neo4jClient;

    public GraphService(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    public void upsertRelation(RelationRequest request) {
        String relationType = sanitizeRelationType(request.relationType());

        String cypher = "MATCH (p:Person {id: $personId}), (pr:Program {id: $programId}) " +
                "MERGE (p)-[r:" + relationType + "]->(pr)";
        neo4jClient.query(cypher)
                .bindAll(Map.of("personId", request.personId(), "programId", request.programId()))
                .run();

        if (request.roleId() != null && !request.roleId().isBlank()) {
            neo4jClient.query("MATCH (p:Person {id: $personId}), (r:Role {id: $roleId}) MERGE (p)-[:拥有身份]->(r)")
                    .bindAll(Map.of("personId", request.personId(), "roleId", request.roleId()))
                    .run();
        }
        if (request.categoryId() != null && !request.categoryId().isBlank()) {
            neo4jClient.query("MATCH (p:Person {id: $personId}), (c:Category {id: $categoryId}) MERGE (p)-[:关系分类]->(c)")
                    .bindAll(Map.of("personId", request.personId(), "categoryId", request.categoryId()))
                    .run();
        }
        if (request.yearId() != null && !request.yearId().isBlank()) {
            linkProgramYear(request.programId(), request.yearId());
        }
    }

    public void linkProgramYear(String programId, String yearId) {
        neo4jClient.query("MATCH (:Program {id: $programId})-[r:属于年份]->(:Year) DELETE r")
                .bind(programId).to("programId")
                .run();
        neo4jClient.query("MATCH (pr:Program {id: $programId}), (y:Year {id: $yearId}) MERGE (pr)-[:属于年份]->(y)")
                .bindAll(Map.of("programId", programId, "yearId", yearId))
                .run();
    }

    public void deleteRelation(RelationRequest request) {
        String relationType = sanitizeRelationType(request.relationType());
        String cypher = "MATCH (p:Person {id: $personId})-[r:" + relationType + "]->(pr:Program {id: $programId}) DELETE r";
        neo4jClient.query(cypher)
                .bindAll(Map.of("personId", request.personId(), "programId", request.programId()))
                .run();
    }

    private String sanitizeRelationType(String relationType) {
        if (!ALLOWED_RELATIONS.contains(relationType)) {
            throw new IllegalArgumentException("relationType 仅支持: 参演/导演/主持");
        }
        return relationType;
    }

    public Map<String, Object> graphData() {
        Collection<Map<String, Object>> nodes = neo4jClient.query("MATCH (n) RETURN id(n) AS vid, labels(n) AS labels, properties(n) AS props")
                .fetch().all();
        Collection<Map<String, Object>> links = neo4jClient.query("MATCH (a)-[r]->(b) RETURN id(a) AS source, id(b) AS target, type(r) AS type, properties(r) AS props")
                .fetch().all();
        Map<String, Object> result = new HashMap<>();
        result.put("nodes", nodes);
        result.put("links", links);
        return result;
    }
}

package com.chunwan.kg.entity;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("Year")
public class YearNode {
    @Id
    private String id;
    private Integer year;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
}

package com.chunwan.kg.entity;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("Person")
public class Person {
    @Id
    private String id;
    private String name;
    private String gender;
    private String ethnicity;
    private String region;
    private String type;
    private Boolean overseas;
    private String baikeUrl;
    private String bio;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getEthnicity() { return ethnicity; }
    public void setEthnicity(String ethnicity) { this.ethnicity = ethnicity; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Boolean getOverseas() { return overseas; }
    public void setOverseas(Boolean overseas) { this.overseas = overseas; }
    public String getBaikeUrl() { return baikeUrl; }
    public void setBaikeUrl(String baikeUrl) { this.baikeUrl = baikeUrl; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}

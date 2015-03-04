/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.entities;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Property;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"algorithm", "name", "description"})
@Entity(value = "staging_algorithms", noClassnameStored = true)
public class StagingAlgorithm {

    @Id
    private ObjectId _id;
    @Indexed
    @Property("algorithm")
    private String _algorithm;
    @Property("name")
    private String _name;
    @Property("description")
    private String _description;

    /**
     * Morphia requires a default constructor
     */
    public StagingAlgorithm() {
    }

    public StagingAlgorithm(String algorithm) {
        setAlgorithm(algorithm);
    }

    @JsonIgnore
    public ObjectId getId() {
        return _id;
    }

    public void setId(ObjectId id) {
        _id = id;
    }

    @JsonProperty("algorithm")
    public String getAlgorithm() {
        return _algorithm;
    }

    public void setAlgorithm(String algorithm) {
        _algorithm = algorithm;
    }

    @JsonProperty("name")
    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    @JsonProperty("description")
    public String getDescription() {
        return _description;
    }

    public void setDescription(String description) {
        _description = description;
    }

}

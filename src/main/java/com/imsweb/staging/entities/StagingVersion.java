/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.entities;

import java.util.Date;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Property;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"algorithm", "version", "type", "year_from", "year_to", "last_modified"})
@Entity(value = "staging_versions", noClassnameStored = true)
public class StagingVersion {

    @Id
    private ObjectId _id;
    @Indexed
    @Property("algorithm")
    private String _algorithm;
    @Indexed
    @Property("version")
    private String _version;
    @Property("type")
    private Type _type;
    @Property("year_from")
    private Integer _yearFrom;
    @Property("year_to")
    private Integer _yearTo;
    @Property("modified")
    private Date _lastModified;

    /**
     * Default constructor is required by Morphia
     */
    public StagingVersion() {
    }

    public StagingVersion(String algorithm, String version) {
        setAlgorithm(algorithm);
        setVersion(version);
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

    @JsonProperty("version")
    public String getVersion() {
        return _version;
    }

    public void setVersion(String version) {
        _version = version;
    }

    @JsonProperty("type")
    public Type getType() {
        return _type;
    }

    public void setType(Type type) {
        _type = type;
    }

    @JsonProperty("year_from")
    public Integer getYearFrom() {
        return _yearFrom;
    }

    public void setYearFrom(Integer yearFrom) {
        _yearFrom = yearFrom;
    }

    @JsonProperty("year_to")
    public Integer getYearTo() {
        return _yearTo;
    }

    public void setYearTo(Integer yearTo) {
        _yearTo = yearTo;
    }

    @JsonProperty("last_modified")
    public Date getLastModified() {
        return _lastModified;
    }

    public void setLastModified(Date lastModified) {
        _lastModified = lastModified;
    }

    @JsonIgnore
    public boolean isProduction() {
        return Type.PRODUCTION.equals(getType());
    }

    @JsonIgnore
    public boolean isBeta() {
        return Type.BETA.equals(getType());
    }

    @JsonIgnore
    public boolean isDevelopment() {
        return Type.DEVELOPMENT.equals(getType());
    }

    public enum Type {
        PRODUCTION,
        BETA,
        DEVELOPMENT
    }

}

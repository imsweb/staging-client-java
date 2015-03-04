/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.entities;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Objects;

import com.imsweb.decisionengine.TablePath;

@JsonPropertyOrder({"id", "input_mapping", "output_mapping", "inputs", "outputs"})
@Embedded
public class StagingTablePath implements TablePath {

    @Property("id")
    private String _id;
    @Embedded("input_mapping")
    private List<StagingKeyMapping> _inputMapping;
    @Embedded("output_mapping")
    private List<StagingKeyMapping> _outputMapping;
    @Embedded("inputs")
    private Set<String> _inputs;
    @Embedded("outputs")
    private Set<String> _outputs;

    /**
     * Morphia requires a default constructor
     */
    public StagingTablePath() {
    }

    public StagingTablePath(String id) {
        setId(id);
    }

    @Override
    @JsonProperty("id")
    public String getId() {
        return _id;
    }

    public void setId(String id) {
        _id = id;
    }

    @Override
    @JsonProperty("input_mapping")
    public List<StagingKeyMapping> getInputMapping() {
        return _inputMapping;
    }

    public void setInputMapping(List<StagingKeyMapping> input) {
        _inputMapping = input;
    }

    @Override
    @JsonProperty("output_mapping")
    public List<StagingKeyMapping> getOutputMapping() {
        return _outputMapping;
    }

    public void setOutputMapping(List<StagingKeyMapping> output) {
        _outputMapping = output;
    }

    @JsonProperty("inputs")
    public Set<String> getInputs() {
        return _inputs;
    }

    @JsonDeserialize(as = LinkedHashSet.class)
    public void setInputs(Set<String> inputs) {
        _inputs = inputs;
    }

    @JsonProperty("outputs")
    public Set<String> getOutputs() {
        return _outputs;
    }

    @JsonDeserialize(as = LinkedHashSet.class)
    public void setOutputs(Set<String> outputs) {
        _outputs = outputs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        StagingTablePath that = (StagingTablePath)o;

        return Objects.equal(_id, that._id) &&
                Objects.equal(_inputMapping, that._inputMapping) &&
                Objects.equal(_inputs, that._inputs) &&
                Objects.equal(_outputMapping, that._outputMapping) &&
                Objects.equal(_outputs, that._outputs);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(_id, _inputMapping, _inputs, _outputMapping, _outputs);
    }
}

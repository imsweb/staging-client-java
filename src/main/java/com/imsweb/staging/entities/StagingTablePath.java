/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.entities;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Property;

import com.imsweb.decisionengine.TablePath;

@JsonPropertyOrder({"id", "input_mapping", "output_mapping", "inputs", "outputs"})
@Embedded
public class StagingTablePath implements TablePath {

    @Property("id")
    private String _id;
    @Embedded("input_mapping")
    private Set<StagingKeyMapping> _inputMapping;
    @Embedded("output_mapping")
    private Set<StagingKeyMapping> _outputMapping;
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
    public Set<StagingKeyMapping> getInputMapping() {
        return _inputMapping;
    }

    @JsonDeserialize(as = LinkedHashSet.class)
    public void setInputMapping(Set<StagingKeyMapping> input) {
        _inputMapping = input;
    }

    @Override
    @JsonProperty("output_mapping")
    public Set<StagingKeyMapping> getOutputMapping() {
        return _outputMapping;
    }

    @JsonDeserialize(as = LinkedHashSet.class)
    public void setOutputMapping(Set<StagingKeyMapping> output) {
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

        return Objects.equals(_id, that._id) &&
                Objects.equals(_inputMapping, that._inputMapping) &&
                Objects.equals(_inputs, that._inputs) &&
                Objects.equals(_outputMapping, that._outputMapping) &&
                Objects.equals(_outputs, that._outputs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_id, _inputMapping, _inputs, _outputMapping, _outputs);
    }
}

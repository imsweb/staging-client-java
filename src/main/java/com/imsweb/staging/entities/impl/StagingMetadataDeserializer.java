/*
 * Copyright (C) 2021 Information Management Services, Inc.
 */
package com.imsweb.staging.entities.impl;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class StagingMetadataDeserializer extends StdDeserializer<StagingMetadata> {

    public StagingMetadataDeserializer() {
        this(null);
    }

    protected StagingMetadataDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public StagingMetadata deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);

        if (node.isNull())
            return null;

        if (node.isObject()) {
            StagingMetadata meta = new StagingMetadata();

            if (node.get("name") != null)
                meta.setName(node.get("name").asText());
            if (node.get("start") != null)
                meta.setStart(node.get("start").asInt());
            if (node.get("end") != null)
                meta.setEnd(node.get("end").asInt());

            return meta;
        }

        return new StagingMetadata(node.asText());
    }
}

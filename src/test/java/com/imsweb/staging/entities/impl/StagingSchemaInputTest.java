package com.imsweb.staging.entities.impl;

import java.util.Collections;
import java.util.HashSet;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class StagingSchemaInputTest {

    @Test
    public void testEquals() {
        StagingSchemaInput input1 = new StagingSchemaInput();
        StagingSchemaInput input2 = new StagingSchemaInput();

        assertEquals(input1, input2);

        input1.setKey("key");
        input1.setName("name");
        input1.setTable("table");
        input1.setMetadata(new HashSet<>(Collections.singletonList(new StagingMetadata("META1"))));

        input2 = new StagingSchemaInput("key", "name", "table");
        input2.setMetadata(new HashSet<>(Collections.singletonList(new StagingMetadata("META1"))));

        assertEquals(input1, input2);

        input2.setMetadata(new HashSet<>(Collections.singletonList(new StagingMetadata("META2"))));

        assertNotEquals(input1, input2);

        // test copy constructor
        StagingSchemaInput input3 = new StagingSchemaInput(input1);
        assertEquals(input1, input3);
    }
}
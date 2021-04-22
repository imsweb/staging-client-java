/*
 * Copyright (C) 2021 Information Management Services, Inc.
 */
package com.imsweb.staging.entities;

import java.util.Collections;
import java.util.HashSet;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class StagingSchemaOutputTest {

    @Test
    public void testEquals() {
        StagingSchemaOutput output1 = new StagingSchemaOutput();
        StagingSchemaOutput output2 = new StagingSchemaOutput();

        assertEquals(output1, output2);

        output1.setKey("key");
        output1.setName("name");
        output1.setTable("table");
        output1.setNaaccrXmlId("test");
        output1.setMetadata(new HashSet<>(Collections.singletonList("META1")));

        output2 = new StagingSchemaOutput("key", "name", "table");
        output2.setNaaccrXmlId("test");
        output2.setMetadata(new HashSet<>(Collections.singletonList("META1")));

        assertEquals(output1, output2);

        output2.setMetadata(new HashSet<>(Collections.singletonList("META2")));

        assertNotEquals(output1, output2);

        // test copy constructor
        StagingSchemaOutput output3 = new StagingSchemaOutput(output1);
        assertEquals(output1, output3);
    }

}

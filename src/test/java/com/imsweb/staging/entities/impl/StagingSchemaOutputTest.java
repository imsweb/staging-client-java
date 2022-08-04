/*
 * Copyright (C) 2021 Information Management Services, Inc.
 */
package com.imsweb.staging.entities.impl;

import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StagingSchemaOutputTest {

    @Test
    void testEquals() {
        StagingSchemaOutput output1 = new StagingSchemaOutput();
        StagingSchemaOutput output2 = new StagingSchemaOutput();

        Assertions.assertEquals(output1, output2);

        output1.setKey("key");
        output1.setName("name");
        output1.setTable("table");
        output1.setNaaccrXmlId("test");
        output1.setMetadata(Collections.singletonList(new StagingMetadata("META1")));

        output2 = new StagingSchemaOutput("key", "name", "table");
        output2.setNaaccrXmlId("test");
        output2.setMetadata(Collections.singletonList(new StagingMetadata("META1")));

        Assertions.assertEquals(output1, output2);

        output2.setMetadata(Collections.singletonList(new StagingMetadata("META2")));

        Assertions.assertNotEquals(output1, output2);

        // test copy constructor
        StagingSchemaOutput output3 = new StagingSchemaOutput(output1);
        Assertions.assertEquals(output1, output3);
    }

}

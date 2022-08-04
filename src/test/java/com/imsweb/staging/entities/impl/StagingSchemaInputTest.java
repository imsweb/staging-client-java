package com.imsweb.staging.entities.impl;

import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StagingSchemaInputTest {

    @Test
    void testEquals() {
        StagingSchemaInput input1 = new StagingSchemaInput();
        StagingSchemaInput input2 = new StagingSchemaInput();

        Assertions.assertEquals(input1, input2);

        input1.setKey("key");
        input1.setName("name");
        input1.setTable("table");
        input1.setMetadata(Collections.singletonList(new StagingMetadata("META1")));

        input2 = new StagingSchemaInput("key", "name", "table");
        input2.setMetadata(Collections.singletonList(new StagingMetadata("META1")));

        Assertions.assertEquals(input1, input2);

        input2.setMetadata(Collections.singletonList(new StagingMetadata("META2")));

        Assertions.assertNotEquals(input1, input2);

        // test copy constructor
        StagingSchemaInput input3 = new StagingSchemaInput(input1);
        Assertions.assertEquals(input1, input3);
    }
}
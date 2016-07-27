package com.imsweb.staging.entities;

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
        input2.setKey("key");
        input1.setName("name");
        input2.setName("name");
        input1.setMetadata(new HashSet<>(Collections.singletonList("META1")));
        input2.setMetadata(new HashSet<>(Collections.singletonList("META1")));

        assertEquals(input1, input2);

        input2.setMetadata(new HashSet<>(Collections.singletonList("META2")));

        assertNotEquals(input1, input2);
    }
}
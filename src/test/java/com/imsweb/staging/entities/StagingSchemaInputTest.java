package com.imsweb.staging.entities;

import java.util.Collections;
import java.util.HashSet;

import org.junit.Assert;
import org.junit.Test;

public class StagingSchemaInputTest {

    @Test
    public void testEquals() {
        StagingSchemaInput input1 = new StagingSchemaInput();
        StagingSchemaInput input2 = new StagingSchemaInput();

        Assert.assertEquals(input1, input2);

        input1.setKey("key");
        input2.setKey("key");
        input1.setName("name");
        input2.setName("name");
        input1.setMetadata(new HashSet<>(Collections.singletonList("META1")));
        input2.setMetadata(new HashSet<>(Collections.singletonList("META1")));

        Assert.assertEquals(input1, input2);

        input2.setMetadata(new HashSet<>(Collections.singletonList("META2")));

        Assert.assertNotEquals(input1, input2);
    }
}
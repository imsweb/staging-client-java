package com.imsweb.staging.entities;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Sets;

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
        input1.setMetadata(Sets.newHashSet("META1"));
        input2.setMetadata(Sets.newHashSet("META1"));

        Assert.assertEquals(input1, input2);

        input2.setMetadata(Sets.newHashSet("META2"));

        Assert.assertNotEquals(input1, input2);

        //        input1.setMetadata(null);
        //        input2.setMetadata(Collections.<String>emptySet());
        //
        //        Assert.assertEquals(input1, input2);
    }
}
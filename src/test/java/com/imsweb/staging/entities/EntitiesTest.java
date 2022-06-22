/*
 * Copyright (C) 2022 Information Management Services, Inc.
 */
package com.imsweb.staging.entities;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

import com.imsweb.staging.entities.impl.StagingColumnDefinition;
import com.imsweb.staging.entities.impl.StagingEndpoint;
import com.imsweb.staging.entities.impl.StagingKeyMapping;
import com.imsweb.staging.entities.impl.StagingKeyValue;
import com.imsweb.staging.entities.impl.StagingMapping;
import com.imsweb.staging.entities.impl.StagingMetadata;
import com.imsweb.staging.entities.impl.StagingSchema;
import com.imsweb.staging.entities.impl.StagingSchemaInput;
import com.imsweb.staging.entities.impl.StagingSchemaOutput;
import com.imsweb.staging.entities.impl.StagingTable;
import com.imsweb.staging.entities.impl.StagingTablePath;
import com.imsweb.staging.entities.impl.StagingTableRow;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEqualsExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCodeExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSettersExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSettersFor;

public class EntitiesTest {

    @Test
    public void testEntities() {
        MatcherAssert.assertThat(Error.class, CoreMatchers.allOf(hasValidBeanConstructor(), hasValidGettersAndSetters()));

        MatcherAssert.assertThat(GlossaryDefinition.class, CoreMatchers.allOf(hasValidBeanConstructor(), hasValidGettersAndSetters(), hasValidBeanEquals(), hasValidBeanHashCode()));

        MatcherAssert.assertThat(StagingColumnDefinition.class, CoreMatchers.allOf(hasValidBeanConstructor(), hasValidGettersAndSetters(), hasValidBeanEquals(), hasValidBeanHashCode()));
        MatcherAssert.assertThat(StagingEndpoint.class, CoreMatchers.allOf(hasValidBeanConstructor(), hasValidGettersAndSetters()));
        MatcherAssert.assertThat(StagingKeyMapping.class, CoreMatchers.allOf(hasValidBeanConstructor(), hasValidGettersAndSetters(), hasValidBeanEquals(), hasValidBeanHashCode()));
        MatcherAssert.assertThat(StagingKeyValue.class, CoreMatchers.allOf(hasValidBeanConstructor(), hasValidGettersAndSetters(), hasValidBeanEquals(), hasValidBeanHashCode()));
        MatcherAssert.assertThat(StagingMapping.class, CoreMatchers.allOf(hasValidBeanConstructor(), hasValidGettersAndSetters(), hasValidBeanEquals(), hasValidBeanHashCode()));
        MatcherAssert.assertThat(StagingMetadata.class, CoreMatchers.allOf(hasValidBeanConstructor(), hasValidGettersAndSetters(), hasValidBeanEquals(), hasValidBeanHashCode()));
        MatcherAssert.assertThat(StagingSchema.class, CoreMatchers.allOf(hasValidBeanConstructor(),
                hasValidGettersAndSettersExcluding("inputMap", "outputMap"),
                hasValidBeanEqualsExcluding("inputMap", "outputMap", "lastModified"),
                hasValidBeanHashCodeExcluding("inputMap", "outputMap", "lastModified")));
        MatcherAssert.assertThat(StagingSchemaInput.class, CoreMatchers.allOf(hasValidBeanConstructor(), hasValidGettersAndSetters(), hasValidBeanEquals(), hasValidBeanHashCode()));
        MatcherAssert.assertThat(StagingSchemaOutput.class, CoreMatchers.allOf(hasValidBeanConstructor(), hasValidGettersAndSetters(), hasValidBeanEquals(), hasValidBeanHashCode()));
        MatcherAssert.assertThat(StagingTable.class, CoreMatchers.allOf(hasValidBeanConstructor(),
                hasValidGettersAndSetters(),
                hasValidBeanEqualsExcluding("tableRows", "lastModified"),
                hasValidBeanHashCodeExcluding("tableRows", "lastModified")));
        MatcherAssert.assertThat(StagingTablePath.class, CoreMatchers.allOf(hasValidBeanConstructor(), hasValidGettersAndSetters(), hasValidBeanEquals(), hasValidBeanHashCode()));
        MatcherAssert.assertThat(StagingTableRow.class, CoreMatchers.allOf(hasValidBeanConstructor(), hasValidGettersAndSettersFor("inputs", "endpoints")));
    }

}

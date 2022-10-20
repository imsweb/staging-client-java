/*
 * Copyright (C) 2022 Information Management Services, Inc.
 */
package com.imsweb.staging.entities;

import org.junit.jupiter.api.Test;

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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;

class EntitiesTest {

    @Test
    void testEntities() {
        assertThat(Error.class, allOf(hasValidBeanConstructor(), hasValidGettersAndSetters()));

        assertThat(GlossaryDefinition.class, allOf(hasValidBeanConstructor(), hasValidGettersAndSetters(), hasValidBeanEquals(), hasValidBeanHashCode()));

        assertThat(StagingColumnDefinition.class, allOf(hasValidBeanConstructor(), hasValidGettersAndSetters(), hasValidBeanEquals(), hasValidBeanHashCode()));
        assertThat(StagingEndpoint.class, allOf(hasValidBeanConstructor(), hasValidGettersAndSetters()));
        assertThat(StagingKeyMapping.class, allOf(hasValidBeanConstructor(), hasValidGettersAndSetters(), hasValidBeanEquals(), hasValidBeanHashCode()));
        assertThat(StagingKeyValue.class, allOf(hasValidBeanConstructor(), hasValidGettersAndSetters(), hasValidBeanEquals(), hasValidBeanHashCode()));
        assertThat(StagingMapping.class, allOf(hasValidBeanConstructor(), hasValidGettersAndSetters(), hasValidBeanEquals(), hasValidBeanHashCode()));
        assertThat(StagingMetadata.class, allOf(hasValidBeanConstructor(), hasValidGettersAndSetters(), hasValidBeanEquals(), hasValidBeanHashCode()));
        assertThat(StagingSchema.class, allOf(hasValidBeanConstructor(),
                hasValidGettersAndSettersExcluding("inputMap", "outputMap"),
                hasValidBeanEqualsExcluding("inputMap", "outputMap", "lastModified"),
                hasValidBeanHashCodeExcluding("inputMap", "outputMap", "lastModified")));
        assertThat(StagingSchemaInput.class, allOf(hasValidBeanConstructor(), hasValidGettersAndSetters(), hasValidBeanEquals(), hasValidBeanHashCode()));
        assertThat(StagingSchemaOutput.class, allOf(hasValidBeanConstructor(), hasValidGettersAndSetters(), hasValidBeanEquals(), hasValidBeanHashCode()));
        assertThat(StagingTable.class, allOf(hasValidBeanConstructor(),
                hasValidGettersAndSetters(),
                hasValidBeanEqualsExcluding("tableRows", "lastModified"),
                hasValidBeanHashCodeExcluding("tableRows", "lastModified")));
        assertThat(StagingTablePath.class, allOf(hasValidBeanConstructor(), hasValidGettersAndSetters(), hasValidBeanEquals(), hasValidBeanHashCode()));
        assertThat(StagingTableRow.class, allOf(hasValidBeanConstructor(), hasValidGettersAndSettersFor("inputs", "endpoints")));
    }

}

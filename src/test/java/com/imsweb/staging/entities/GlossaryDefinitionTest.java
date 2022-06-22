package com.imsweb.staging.entities;

import java.util.Collections;
import java.util.Date;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GlossaryDefinitionTest {

    @Test
    public void testConstruction() {
        Date now = new Date();

        GlossaryDefinition def = new GlossaryDefinition();
        def.setName("NAME");
        def.setDefinition("DEF");
        def.setAlternateNames(Collections.singletonList("ALT"));
        def.setLastModified(now);

        assertThat(def).isEqualTo(new GlossaryDefinition("NAME", "DEF", Collections.singletonList("ALT"), now));
    }

}
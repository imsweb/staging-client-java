/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.staging.pediatric;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.imsweb.staging.Staging;
import com.imsweb.staging.StagingDataProvider;
import com.imsweb.staging.StagingFileDataProvider;
import com.imsweb.staging.StagingTest;
import com.imsweb.staging.entities.GlossaryDefinition;
import com.imsweb.staging.entities.Input;
import com.imsweb.staging.entities.Metadata;
import com.imsweb.staging.entities.Output;
import com.imsweb.staging.entities.Schema;
import com.imsweb.staging.entities.SchemaLookup;
import com.imsweb.staging.entities.StagingData.Result;
import com.imsweb.staging.pediatric.PediatricDataProvider.PediatricVersion;
import com.imsweb.staging.pediatric.PediatricStagingData.PediatricInput;
import com.imsweb.staging.pediatric.PediatricStagingData.PediatricOutput;
import com.imsweb.staging.pediatric.PediatricStagingData.PediatricStagingInputBuilder;

import static org.assertj.core.api.Assertions.assertThat;

class PediatricStagingTest extends StagingTest {

    @BeforeAll
    public static void init() {
        _STAGING = Staging.getInstance(PediatricDataProvider.getInstance(PediatricVersion.V1_0));
    }

    @Override
    public String getAlgorithm() {
        return "pediatric";
    }

    @Override
    public String getVersion() {
        return PediatricVersion.V1_0.getVersion();
    }

    @Override
    public StagingFileDataProvider getProvider() {
        return PediatricDataProvider.getInstance(PediatricVersion.LATEST);
    }

    @Test
    void testBasicInitialization() {
        assertThat(_STAGING.getSchemaIds()).hasSize(33);
        assertThat(_STAGING.getTableIds()).isNotEmpty();

        assertThat(_STAGING.getSchema("ependymoma")).isNotNull();
        assertThat(_STAGING.getTable("n_myc_amplification_57417")).isNotNull();
    }

    @Test
    void testVersionInitializationTypes() {
        Staging staging10 = Staging.getInstance(PediatricDataProvider.getInstance(PediatricVersion.V1_0));
        assertThat(staging10.getVersion()).isEqualTo(PediatricVersion.LATEST.getVersion());

        Staging stagingLatest = Staging.getInstance(PediatricDataProvider.getInstance());
        assertThat(stagingLatest.getVersion()).isEqualTo(PediatricVersion.LATEST.getVersion());
    }

    @Test
    void testDescriminatorKeys() {
        assertThat(_STAGING.getSchema("acute_lymphoblastic_leukemia").getSchemaDiscriminators()).containsOnly("age_dx", "behavior");
        assertThat(_STAGING.getSchema("ovarian").getSchemaDiscriminators()).containsOnly("age_dx", "behavior");

        // check all schema discriminators
        Set<String> allDiscriminators = _STAGING.getSchemaIds().stream()
                .map(schemaId -> _STAGING.getSchema(schemaId).getSchemaDiscriminators())
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        assertThat(allDiscriminators).containsOnly("age_dx", "behavior");
    }

    @Override
    @Test
    public void testValidCode() {
        Map<String, String> context = new HashMap<>();
        context.put("hist", "7000");
        assertThat(_STAGING.isContextValid("ovarian", "hist", context)).isFalse();
        context.put("hist", "8000");
        assertThat(_STAGING.isContextValid("ovarian", "hist", context)).isTrue();
        context.put("hist", "8542");
        assertThat(_STAGING.isContextValid("ovarian", "hist", context)).isTrue();

        // make sure null is handled
        context.put("hist", null);
        assertThat(_STAGING.isContextValid("ovarian", "hist", context)).isFalse();

        // make sure blank is handled
        context.put("hist", "");
        assertThat(_STAGING.isContextValid("ovarian", "hist", context)).isFalse();
    }

    @Test
    void testInputAndOutput() {
        Set<String> inputs = new HashSet<>();
        Set<String> outputs = new HashSet<>();
        _STAGING.getSchemaIds().stream()
                .map(schemaId -> _STAGING.getSchema(schemaId))
                .forEach(schema -> {
                    inputs.addAll(_STAGING.getInputs(schema));
                    outputs.addAll(_STAGING.getOutputs(schema));
                });

        // note that while year_dx is not "used for staging" it is validated at the start of the process so it kind of is
        inputs.add("year_dx");

        // this verified that the inputs/outputs are an exact match to the Input and Output enums
        assertThat(inputs).containsExactlyInAnyOrderElementsOf(Stream.of(PediatricInput.values()).map(PediatricInput::toString).collect(Collectors.toSet()));
        assertThat(outputs).containsExactlyInAnyOrderElementsOf(Stream.of(PediatricOutput.values()).map(PediatricOutput::toString).collect(Collectors.toSet()));
    }

    @Test
    void testSchemaSelection() {
        // test bad values
        List<Schema> lookup = _STAGING.lookupSchema(new SchemaLookup());
        assertThat(lookup).isEmpty();

        lookup = _STAGING.lookupSchema(new PediatricSchemaLookup("XXX", "YYY"));
        assertThat(lookup).isEmpty();

        // test valid combinations that do not require a discriminator
        PediatricSchemaLookup schemaLookup = new PediatricSchemaLookup("C220", "8970", "10", "3");
        lookup = _STAGING.lookupSchema(schemaLookup);
        assertThat(lookup).hasSize(1);
        assertThat(lookup.get(0).getId()).isEqualTo("hepatoblastoma");

        // test valid combination that requires a discriminator but is not supplied one
        lookup = _STAGING.lookupSchema(new PediatricSchemaLookup("C723", "9384"));
        assertThat(lookup).hasSize(2);
        assertThat(lookup.stream().map(Schema::getId).collect(Collectors.toSet())).isEqualTo(
                new HashSet<>(Arrays.asList("astrocytoma", "adult_other_non_pediatric")));
        assertThat(lookup.stream().flatMap(d -> d.getSchemaDiscriminators().stream()).collect(Collectors.toSet())).isEqualTo(new HashSet<>(Arrays.asList("age_dx", "behavior")));

        // test valid combination that requires discriminator and a good discriminator is supplied
        schemaLookup = new PediatricSchemaLookup("C723", "9384");
        schemaLookup.setInput(PediatricInput.AGE_DX.toString(), "11");
        lookup = _STAGING.lookupSchema(schemaLookup);
        assertThat(lookup).hasSize(1);
        assertThat(lookup.stream().flatMap(d -> d.getSchemaDiscriminators().stream()).collect(Collectors.toSet())).isEqualTo(new HashSet<>(Collections.singletonList("age_dx")));
        assertThat(lookup.get(0).getId()).isEqualTo("astrocytoma");

        // test valid combination that requires a discriminator but is supplied a bad disciminator value
        schemaLookup = new PediatricSchemaLookup("C723", "9384");
        schemaLookup.setInput(PediatricInput.AGE_DX.toString(), "XX");
        lookup = _STAGING.lookupSchema(schemaLookup);
        assertThat(lookup).isEmpty();

        // test searching on only site
        lookup = _STAGING.lookupSchema(new PediatricSchemaLookup("C401", null));
        assertThat(lookup).hasSize(16);

        // test searching on only hist
        lookup = _STAGING.lookupSchema(new PediatricSchemaLookup(null, "9702"));
        assertThat(lookup).hasSize(2);
    }

    @Test
    void testDiscriminatorInputs() {
        Set<String> discriminators = new HashSet<>();
        _STAGING.getSchemaIds().stream()
                .map(schemaId -> _STAGING.getSchema(schemaId))
                .map(Schema::getSchemaDiscriminators)
                .filter(Objects::nonNull)
                .forEach(discriminators::addAll);

        assertThat(discriminators).containsOnly("age_dx", "behavior");
    }

    @Test
    void testLookupCache() {
        final String site = "C710";
        final String hist = "9392";
        final String schemaId = "ependymoma";

        // do the same lookup twice
        List<Schema> lookup = _STAGING.lookupSchema(new PediatricSchemaLookup(site, hist));
        assertThat(lookup).hasSize(1);
        assertThat(lookup.get(0).getId()).isEqualTo(schemaId);

        lookup = _STAGING.lookupSchema(new PediatricSchemaLookup(site, hist));
        assertThat(lookup).hasSize(1);
        assertThat(lookup.get(0).getId()).isEqualTo(schemaId);

        // now invalidate the cache
        PediatricDataProvider.getInstance(PediatricVersion.V1_0).invalidateCache();

        // try the lookup again
        lookup = _STAGING.lookupSchema(new PediatricSchemaLookup(site, hist));
        assertThat(lookup).hasSize(1);
        assertThat(lookup.get(0).getId()).isEqualTo(schemaId);
    }

    @Test
    void testFindTableRow() {
        final String tableId = "pediatric_mets_12638";
        final String colId = "ped_mets";

        assertThat(_STAGING.getTable(tableId)).isNotNull();

        assertThat(_STAGING.findMatchingTableRow(tableId, colId, "XX")).isNull();

        // null maps to blank
        assertThat(_STAGING.findMatchingTableRow(tableId, colId, "00")).isEqualTo(Integer.valueOf(0));
        assertThat(_STAGING.findMatchingTableRow(tableId, colId, "10")).isEqualTo(Integer.valueOf(1));
        assertThat(_STAGING.findMatchingTableRow(tableId, colId, "70")).isEqualTo(Integer.valueOf(2));
        assertThat(_STAGING.findMatchingTableRow(tableId, colId, "99")).isEqualTo(Integer.valueOf(3));
    }

    @Test
    void testStageOvarian() {
        PediatricStagingData data = new PediatricStagingInputBuilder()
                .withInput(PediatricInput.PRIMARY_SITE, "C569")
                .withInput(PediatricInput.HISTOLOGY, "9081")
                .withInput(PediatricInput.YEAR_DX, "2021")
                .withInput(PediatricInput.AGE_DX, "16")
                .withInput(PediatricInput.BEHAVIOR, "3")
                .withInput(PediatricInput.PED_PRIMARY_TUMOR, "200")
                .build();

        // perform the staging
        _STAGING.stage(data);

        assertThat(data.getResult()).isEqualTo(Result.STAGED);
        assertThat(data.getSchemaId()).isEqualTo("ovarian");
        assertThat(data.getErrors()).isEmpty();
        assertThat(data.getPath()).containsExactlyInAnyOrder("toronto_stage.pediatric_stage_78332");
        assertThat(data.getOutput()).hasSize(7);

        // check outputs
        assertThat(data.getOutput(PediatricOutput.DERIVED_VERSION)).isEqualTo(PediatricVersion.LATEST.getVersion());

        assertThat(data.getOutput())
                .hasSize(7)
                .hasFieldOrPropertyWithValue(PediatricOutput.DERIVED_VERSION.toString(), PediatricVersion.LATEST.getVersion())
                .hasFieldOrPropertyWithValue(PediatricOutput.TORONTO_VERSION_NUMBER.toString(), "2")
                .hasFieldOrPropertyWithValue(PediatricOutput.PEDIATRIC_ID.toString(), "10c2")
                .hasFieldOrPropertyWithValue(PediatricOutput.PEDIATRIC_GROUP.toString(), "2")
                .hasFieldOrPropertyWithValue(PediatricOutput.PEDIATRIC_T.toString(), "88")
                .hasFieldOrPropertyWithValue(PediatricOutput.PEDIATRIC_N.toString(), "88")
                .hasFieldOrPropertyWithValue(PediatricOutput.PEDIATRIC_M.toString(), "88");
    }

    @Test
    void testBadLookupInStage() {
        PediatricStagingData data = new PediatricStagingData();
        data.setInput(PediatricInput.AGE_DX, "15");

        // if site/hist are not supplied, no lookup
        _STAGING.stage(data);
        assertThat(data.getResult()).isEqualTo(Result.FAILED_MISSING_SITE_OR_HISTOLOGY);

        // add hist only and it should fail with same result
        data.setInput(PediatricInput.PRIMARY_SITE, "C489");
        _STAGING.stage(data);
        assertThat(data.getResult()).isEqualTo(Result.FAILED_MISSING_SITE_OR_HISTOLOGY);

        // put a site/hist combo that doesn't match a schema
        data.setInput(PediatricInput.HISTOLOGY, "9898");
        _STAGING.stage(data);
        assertThat(data.getResult()).isEqualTo(Result.FAILED_NO_MATCHING_SCHEMA);

        // now a site/hist that returns multiple results
        data.setInput(PediatricInput.PRIMARY_SITE, "C699");
        data.setInput(PediatricInput.HISTOLOGY, "9500");
        _STAGING.stage(data);
        assertThat(data.getResult()).isEqualTo(Result.FAILED_MULITPLE_MATCHING_SCHEMAS);

        // test other constructors
        _STAGING.stage(new PediatricStagingData("C699", "9500", "15"));
        assertThat(data.getResult()).isEqualTo(Result.FAILED_MULITPLE_MATCHING_SCHEMAS);

        data = new PediatricStagingData("C699", "9500");
        data.setInput(PediatricInput.AGE_DX, "15");
        _STAGING.stage(data);
        assertThat(data.getResult()).isEqualTo(Result.FAILED_MULITPLE_MATCHING_SCHEMAS);

        assertThat(data.getInput(PediatricInput.PRIMARY_SITE)).isEqualTo("C699");
        assertThat(data.getInput(PediatricInput.HISTOLOGY)).isEqualTo("9500");
        assertThat(data.getInput(PediatricInput.AGE_DX)).isEqualTo("15");
    }

    @Test
    void testInvolvedTables() {
        Set<String> tables = _STAGING.getInvolvedTables("testicular");

        assertThat(tables).containsOnly(
                "age_at_diagnosis_validation_3881",
                "behavior",
                "combined_s_category_15139",
                "histology",
                "in_situ_to_88s_95645",
                "nodes_pos_fpa",
                "pediatric_m_97287",
                "pediatric_mets_93634",
                "pediatric_n_84898",
                "pediatric_primary_tumor_73431",
                "pediatric_regional_nodes_42195",
                "pediatric_stage_57755",
                "pediatric_t_65327",
                "primary_site",
                "s_category_clinical_11368",
                "s_category_pathological_46197",
                "schema_selection_testicular",
                "year_dx_validation");
    }

    @Test
    void testInvolvedSchemas() {
        Set<String> schemas = _STAGING.getInvolvedSchemas("s_category_clinical_11368");

        assertThat(schemas).isEqualTo(new HashSet<>(Collections.singletonList("testicular")));
    }

    @Test
    void testGetInputs() {
        assertThat(_STAGING.getInputs(_STAGING.getSchema("nhl_nos"))).containsOnly("site", "hist", "age_dx", "behavior");
        assertThat(_STAGING.getInputs(_STAGING.getSchema("testicular"))).containsOnly("ped_mets", "site", "hist",
                "nodes_pos", "age_dx", "s_category_path", "ped_primary_tumor", "s_category_clin", "behavior", "ped_regional_nodes");
    }

    @Test
    void testIsCodeValid() {
        // test bad parameters for schema or field
        assertThat(_STAGING.isCodeValid("bad_schema_name", "site", "C509")).isFalse();
        assertThat(_STAGING.isCodeValid("testicular", "bad_field_name", "C509")).isFalse();

        final String schemaId = "astrocytoma";

        // test null values
        assertThat(_STAGING.isCodeValid(null, null, null)).isFalse();
        assertThat(_STAGING.isCodeValid(schemaId, null, null)).isFalse();
        assertThat(_STAGING.isCodeValid(schemaId, "site", null)).isFalse();

        // test fields that have a "value" specified
        assertThat(_STAGING.isCodeValid(schemaId, "year_dx", null)).isTrue();  // year_dx is now allowed to be null
        assertThat(_STAGING.isCodeValid(schemaId, "year_dx", "200")).isFalse();
        assertThat(_STAGING.isCodeValid(schemaId, "year_dx", "2003")).isFalse();
        assertThat(_STAGING.isCodeValid(schemaId, "year_dx", "2145")).isFalse();
        assertThat(_STAGING.isCodeValid(schemaId, "year_dx", "2018")).isTrue();

        // test valid and invalid fields
        assertThat(_STAGING.isCodeValid(schemaId, "braf_mutational_analysis", "2")).isTrue();
        assertThat(_STAGING.isCodeValid(schemaId, "braf_mutational_analysis", "5")).isFalse();
        assertThat(_STAGING.isCodeValid(schemaId, "ped_mets", "10")).isTrue();
        assertThat(_STAGING.isCodeValid(schemaId, "ped_mets", "20")).isFalse();
    }

    @Test
    void testGetSchema() {
        assertThat(_STAGING.getSchema("bad_schema_name")).isNull();
        assertThat(_STAGING.getSchema("hepatoblastoma")).isNotNull();
        assertThat(_STAGING.getSchema("hepatoblastoma").getName()).isEqualTo("Hepatoblastoma");
    }

    @Test
    void testLookupOutputs() {
        PediatricSchemaLookup lookup = new PediatricSchemaLookup("C569", "9091");
        List<Schema> lookups = _STAGING.lookupSchema(lookup);
        assertThat(lookups).hasSize(2);

        assertThat(lookups).extracting("id").containsExactlyInAnyOrder("ovarian", "adult_other_non_pediatric");

        Schema schema = _STAGING.getSchema("ovarian");

        // build list of output keys
        Set<String> definedOutputs = schema.getOutputs().stream().map(Output::getKey).collect(Collectors.toSet());

        // test without context
        assertThat(_STAGING.getOutputs(schema)).isEqualTo(definedOutputs);

        // test with context
        assertThat(_STAGING.getOutputs(schema, lookup.getInputs())).isEqualTo(definedOutputs);
    }

    @Test
    void testGlossary() {
        assertThat(_STAGING.getGlossaryTerms()).isNotEmpty();
        GlossaryDefinition entry = _STAGING.getGlossaryDefinition("Cortex");
        assertThat(entry).isNotNull();
        assertThat(entry.getName()).isEqualTo("Cortex");
        assertThat(entry.getDefinition()).startsWith("The external or outer surface layer of an organ");
        assertThat(entry.getAlternateNames()).contains("Cortical");
        assertThat(entry.getLastModified()).isNotNull();
    }

    @Test
    void testMetadata() {
        Schema schema = _STAGING.getSchema("testicular");
        assertThat(schema).isNotNull();

        Input input = schema.getInputMap().get("s_category_clin");
        assertThat(input).isNotNull();

        Set<String> metadata = input.getMetadata().stream().map(Metadata::getName).collect(Collectors.toSet());
        assertThat(metadata).containsExactlyInAnyOrder("SEER_REQUIRED", "SSDI");
    }

    @Test
    void testCachedSiteAndHistology() {
        StagingDataProvider provider = getProvider();
        assertThat(provider.getValidSites()).isNotEmpty();
        assertThat(provider.getValidHistologies()).isNotEmpty();

        // site tests
        List<String> validSites = Arrays.asList("C000", "C809");
        List<String> invalidSites = Arrays.asList("C727", "C810");
        for (String site : validSites)
            assertThat(provider.getValidSites()).contains(site);
        for (String site : invalidSites)
            assertThat(provider.getValidSites()).doesNotContain(site);

        // hist tests
        List<String> validHist = Arrays.asList("8000", "8002", "8005", "8290", "9992", "9993");
        List<String> invalidHist = Arrays.asList("8006", "9990");
        for (String hist : validHist)
            assertThat(provider.getValidHistologies())
                    .withFailMessage("The histology '" + hist + "' is not in the valid histology list")
                    .contains(hist);
        for (String hist : invalidHist)
            assertThat(provider.getValidHistologies())
                    .withFailMessage("The histology '" + hist + "' is not supposed to be in the valid histology list")
                    .doesNotContain(hist);
    }
}

/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.staging.toronto;

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

import org.junit.BeforeClass;
import org.junit.Test;

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
import com.imsweb.staging.toronto.TorontoDataProvider.TorontoVersion;
import com.imsweb.staging.toronto.TorontoStagingData.TorontoInput;
import com.imsweb.staging.toronto.TorontoStagingData.TorontoOutput;
import com.imsweb.staging.toronto.TorontoStagingData.TorontoStagingInputBuilder;

import static org.assertj.core.api.Assertions.assertThat;

public class TorontoStagingTest extends StagingTest {

    @BeforeClass
    public static void init() {
        _STAGING = Staging.getInstance(TorontoDataProvider.getInstance(TorontoVersion.V0_1));
    }

    @Override
    public String getAlgorithm() {
        return "toronto";
    }

    @Override
    public String getVersion() {
        return TorontoVersion.V0_1.getVersion();
    }

    @Override
    public StagingFileDataProvider getProvider() {
        return TorontoDataProvider.getInstance(TorontoVersion.LATEST);
    }

    @Test
    public void testBasicInitialization() {
        assertThat(_STAGING.getSchemaIds()).hasSize(35);
        assertThat(_STAGING.getTableIds().size() > 0).isTrue();

        assertThat(_STAGING.getSchema("ependymoma")).isNotNull();
        assertThat(_STAGING.getTable("st_jude_murphy_staging_system_35179")).isNotNull();
    }

    @Test
    public void testVersionInitializationTypes() {
        Staging staging10 = Staging.getInstance(TorontoDataProvider.getInstance(TorontoVersion.V0_1));
        assertThat(staging10.getVersion()).isEqualTo(TorontoVersion.LATEST.getVersion());

        Staging stagingLatest = Staging.getInstance(TorontoDataProvider.getInstance());
        assertThat(stagingLatest.getVersion()).isEqualTo(TorontoVersion.LATEST.getVersion());
    }

    @Test
    public void testDescriminatorKeys() {
        assertThat(_STAGING.getSchema("acute_myeloid_leukemia").getSchemaDiscriminators()).containsOnly("age_dx");
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
    public void testInputAndOutput() {
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
        assertThat(inputs).containsExactlyInAnyOrderElementsOf(Stream.of(TorontoInput.values()).map(TorontoInput::toString).collect(Collectors.toSet()));
        assertThat(outputs).containsExactlyInAnyOrderElementsOf(Stream.of(TorontoOutput.values()).map(TorontoOutput::toString).collect(Collectors.toSet()));
    }

    @Test
    public void testSchemaSelection() {
        // test bad values
        List<Schema> lookup = _STAGING.lookupSchema(new SchemaLookup());
        assertThat(lookup).isEmpty();

        lookup = _STAGING.lookupSchema(new TorontoSchemaLookup("XXX", "YYY"));
        assertThat(lookup).isEmpty();

        // test valid combinations that do not require a discriminator
        TorontoSchemaLookup schemaLookup = new TorontoSchemaLookup("C220", "8970");
        lookup = _STAGING.lookupSchema(schemaLookup);
        assertThat(lookup).hasSize(1);
        assertThat(lookup.get(0).getId()).isEqualTo("hepatoblastoma");

        // test valid combination that requires a discriminator but is not supplied one
        lookup = _STAGING.lookupSchema(new TorontoSchemaLookup("C723", "9384"));
        assertThat(lookup).hasSize(2);
        assertThat(lookup.stream().map(Schema::getId).collect(Collectors.toSet())).isEqualTo(
                new HashSet<>(Arrays.asList("astrocytoma", "adult_other_non_pediatric")));
        assertThat(lookup.stream().flatMap(d -> d.getSchemaDiscriminators().stream()).collect(Collectors.toSet())).isEqualTo(new HashSet<>(Arrays.asList("age_dx", "behavior")));

        // test valid combination that requires discriminator and a good discriminator is supplied
        schemaLookup = new TorontoSchemaLookup("C723", "9384");
        schemaLookup.setInput(TorontoInput.AGE_DX.toString(), "11");
        lookup = _STAGING.lookupSchema(schemaLookup);
        assertThat(lookup).hasSize(1);
        assertThat(lookup.stream().flatMap(d -> d.getSchemaDiscriminators().stream()).collect(Collectors.toSet())).isEqualTo(new HashSet<>(Collections.singletonList("age_dx")));
        assertThat(lookup.get(0).getId()).isEqualTo("astrocytoma");

        // test valid combination that requires a discriminator but is supplied a bad disciminator value
        schemaLookup = new TorontoSchemaLookup("C723", "9384");
        schemaLookup.setInput(TorontoInput.AGE_DX.toString(), "XX");
        lookup = _STAGING.lookupSchema(schemaLookup);
        assertThat(lookup).isEmpty();

        // test searching on only site
        lookup = _STAGING.lookupSchema(new TorontoSchemaLookup("C401", null));
        assertThat(lookup).hasSize(18);

        // test searching on only hist
        lookup = _STAGING.lookupSchema(new TorontoSchemaLookup(null, "9702"));
        assertThat(lookup).hasSize(2);
    }

    @Test
    public void testDiscriminatorInputs() {
        Set<String> discriminators = new HashSet<>();
        _STAGING.getSchemaIds().stream()
                .map(schemaId -> _STAGING.getSchema(schemaId))
                .map(Schema::getSchemaDiscriminators)
                .filter(Objects::nonNull)
                .forEach(discriminators::addAll);

        assertThat(discriminators).containsOnly("age_dx", "behavior");
    }

    @Test
    public void testLookupCache() {
        final String site = "C710";
        final String hist = "9392";
        final String schemaId = "ependymoma";

        // do the same lookup twice
        List<Schema> lookup = _STAGING.lookupSchema(new TorontoSchemaLookup(site, hist));
        assertThat(lookup).hasSize(1);
        assertThat(lookup.get(0).getId()).isEqualTo(schemaId);

        lookup = _STAGING.lookupSchema(new TorontoSchemaLookup(site, hist));
        assertThat(lookup).hasSize(1);
        assertThat(lookup.get(0).getId()).isEqualTo(schemaId);

        // now invalidate the cache
        TorontoDataProvider.getInstance(TorontoVersion.V0_1).invalidateCache();

        // try the lookup again
        lookup = _STAGING.lookupSchema(new TorontoSchemaLookup(site, hist));
        assertThat(lookup).hasSize(1);
        assertThat(lookup.get(0).getId()).isEqualTo(schemaId);
    }

    @Test
    public void testFindTableRow() {
        final String tableId = "toronto_m_65862";
        final String colId = "eod_mets";

        assertThat(_STAGING.getTable(tableId)).isNotNull();

        assertThat(_STAGING.findMatchingTableRow(tableId, colId, "XX")).isNull();

        // null maps to blank
        assertThat(_STAGING.findMatchingTableRow(tableId, colId, "00")).isEqualTo(Integer.valueOf(0));
        assertThat(_STAGING.findMatchingTableRow(tableId, colId, "99")).isEqualTo(Integer.valueOf(0));
        assertThat(_STAGING.findMatchingTableRow(tableId, colId, "10")).isEqualTo(Integer.valueOf(1));
        assertThat(_STAGING.findMatchingTableRow(tableId, colId, "30")).isEqualTo(Integer.valueOf(1));
        assertThat(_STAGING.findMatchingTableRow(tableId, colId, "70")).isEqualTo(Integer.valueOf(1));
    }

    @Test
    public void testStageOvarian() {
        TorontoStagingData data = new TorontoStagingInputBuilder()
                .withInput(TorontoInput.PRIMARY_SITE, "C569")
                .withInput(TorontoInput.HISTOLOGY, "9081")
                .withInput(TorontoInput.YEAR_DX, "2021")
                .withInput(TorontoInput.AGE_DX, "16")
                .withInput(TorontoInput.BEHAVIOR, "3")
                .withInput(TorontoInput.SCHEMA_ID, "00459")
                .withInput(TorontoInput.EOD_PRIMARY_TUMOR, "200")
                .withInput(TorontoInput.EOD_REGIONAL_NODES, "300")
                .withInput(TorontoInput.EOD_METS, "30").build();

        // perform the staging
        _STAGING.stage(data);

        assertThat(data.getResult()).isEqualTo(Result.STAGED);
        assertThat(data.getSchemaId()).isEqualTo("ovarian");
        assertThat(data.getErrors()).isEmpty();
        assertThat(data.getPath()).hasSize(4);
        assertThat(data.getOutput()).hasSize(7);

        // check outputs
        assertThat(data.getOutput(TorontoOutput.DERIVED_VERSION)).isEqualTo(TorontoVersion.LATEST.getVersion());

        assertThat(data.getOutput())
                .hasSize(7)
                .hasFieldOrPropertyWithValue(TorontoOutput.DERIVED_VERSION.toString(), TorontoVersion.LATEST.getVersion())
                .hasFieldOrPropertyWithValue(TorontoOutput.TORONTO_VERSION_NUMBER.toString(), "1")
                .hasFieldOrPropertyWithValue(TorontoOutput.TORONTO_ID.toString(), "10c2")
                .hasFieldOrPropertyWithValue(TorontoOutput.TORONTO_GROUP.toString(), "4")
                .hasFieldOrPropertyWithValue(TorontoOutput.TORONTO_T.toString(), "T2")
                .hasFieldOrPropertyWithValue(TorontoOutput.TORONTO_N.toString(), "N1")
                .hasFieldOrPropertyWithValue(TorontoOutput.TORONTO_M.toString(), "M1");
    }

    @Test
    public void testBadLookupInStage() {
        TorontoStagingData data = new TorontoStagingData();
        data.setInput(TorontoInput.AGE_DX, "15");

        // if site/hist are not supplied, no lookup
        _STAGING.stage(data);
        assertThat(data.getResult()).isEqualTo(Result.FAILED_MISSING_SITE_OR_HISTOLOGY);

        // add hist only and it should fail with same result
        data.setInput(TorontoInput.PRIMARY_SITE, "C489");
        _STAGING.stage(data);
        assertThat(data.getResult()).isEqualTo(Result.FAILED_MISSING_SITE_OR_HISTOLOGY);

        // put a site/hist combo that doesn't match a schema
        data.setInput(TorontoInput.HISTOLOGY, "9898");
        _STAGING.stage(data);
        assertThat(data.getResult()).isEqualTo(Result.FAILED_NO_MATCHING_SCHEMA);

        // now a site/hist that returns multiple results
        data.setInput(TorontoInput.PRIMARY_SITE, "C699");
        data.setInput(TorontoInput.HISTOLOGY, "9500");
        _STAGING.stage(data);
        assertThat(data.getResult()).isEqualTo(Result.FAILED_MULITPLE_MATCHING_SCHEMAS);

        // test other constructors
        _STAGING.stage(new TorontoStagingData("C699", "9500", "15"));
        assertThat(data.getResult()).isEqualTo(Result.FAILED_MULITPLE_MATCHING_SCHEMAS);

        data = new TorontoStagingData("C699", "9500");
        data.setInput(TorontoInput.AGE_DX, "15");
        _STAGING.stage(data);
        assertThat(data.getResult()).isEqualTo(Result.FAILED_MULITPLE_MATCHING_SCHEMAS);

        assertThat(data.getInput(TorontoInput.PRIMARY_SITE)).isEqualTo("C699");
        assertThat(data.getInput(TorontoInput.HISTOLOGY)).isEqualTo("9500");
        assertThat(data.getInput(TorontoInput.AGE_DX)).isEqualTo("15");
    }

    @Test
    public void testInvolvedTables() {
        Set<String> tables = _STAGING.getInvolvedTables("testicular");

        assertThat(tables).containsOnly("toronto_n_21728",
                "age_at_diagnosis_validation_3881",
                "toronto_t_57044",
                "toronto_m_65862",
                "combined_s_category_15139",
                "schema_selection_testicular",
                "hcg_post_orchiectomy_range_2422",
                "schema_id_42744",
                "s_category_clinical_11368",
                "nodes_pos_fpa",
                "hcg_pre_orchiectomy_range_1551",
                "toronto_stage_81706",
                "primary_site",
                "eod_primary_tumor_63650",
                "s_category_pathological_46197",
                "histology",
                "ldh_pre_orchiectomy_range_4903",
                "afp_alpha_fetoprotein_pre_orchiectomy_range_33270",
                "year_dx_validation",
                "eod_mets_68192",
                "behavior",
                "afp_alpha_fetoprotein_post_orchiectomy_range_40524",
                "eod_regional_nodes_4689",
                "ldh_post_orchiectomy_range_81426");
    }

    @Test
    public void testInvolvedSchemas() {
        Set<String> schemas = _STAGING.getInvolvedSchemas("s_category_clinical_11368");

        assertThat(schemas).isEqualTo(new HashSet<>(Collections.singletonList("testicular")));
    }

    @Test
    public void testGetInputs() {
        assertThat(_STAGING.getInputs(_STAGING.getSchema("nhl_nos"))).containsOnly("site", "hist", "age_dx", "behavior");
        assertThat(_STAGING.getInputs(_STAGING.getSchema("testicular"))).containsOnly("eod_mets", "site", "hist",
                "nodes_pos", "age_dx", "s_category_path", "schema_id", "eod_primary_tumor", "s_category_clin", "behavior", "eod_regional_nodes");
    }

    @Test
    public void testIsCodeValid() {
        // test bad parameters for schema or field
        assertThat(_STAGING.isCodeValid("bad_schema_name", "site", "C509")).isFalse();
        assertThat(_STAGING.isCodeValid("testicular", "bad_field_name", "C509")).isFalse();

        final String schemaId = "astrocytoma";

        // test null values
        assertThat(_STAGING.isCodeValid(null, null, null)).isFalse();
        assertThat(_STAGING.isCodeValid(schemaId, null, null)).isFalse();
        assertThat(_STAGING.isCodeValid(schemaId, "site", null)).isFalse();

        // test fields that have a "value" specified
        assertThat(_STAGING.isCodeValid(schemaId, "year_dx", null)).isFalse();
        assertThat(_STAGING.isCodeValid(schemaId, "year_dx", "200")).isFalse();
        assertThat(_STAGING.isCodeValid(schemaId, "year_dx", "2003")).isFalse();
        assertThat(_STAGING.isCodeValid(schemaId, "year_dx", "2145")).isFalse();
        assertThat(_STAGING.isCodeValid(schemaId, "year_dx", "2018")).isTrue();

        // test valid and invalid fields
        assertThat(_STAGING.isCodeValid(schemaId, "eod_mets", "10")).isTrue();
        assertThat(_STAGING.isCodeValid(schemaId, "eod_mets", "09")).isFalse();
        assertThat(_STAGING.isCodeValid(schemaId, "braf_mutational_analysis", "2")).isTrue();
        assertThat(_STAGING.isCodeValid(schemaId, "braf_mutational_analysis", "3")).isFalse();
    }

    @Test
    public void testGetSchema() {
        assertThat(_STAGING.getSchema("bad_schema_name")).isNull();
        assertThat(_STAGING.getSchema("hepatoblastoma")).isNotNull();
        assertThat(_STAGING.getSchema("hepatoblastoma").getName()).isEqualTo("Hepatoblastoma");
    }

    @Test
    public void testLookupOutputs() {
        TorontoSchemaLookup lookup = new TorontoSchemaLookup("C569", "9091");
        List<Schema> lookups = _STAGING.lookupSchema(lookup);
        assertThat(lookups).hasSize(2);

        Schema schema = _STAGING.getSchema(lookups.get(0).getId());
        assertThat(schema.getId()).isEqualTo("ovarian");

        // build list of output keys
        Set<String> definedOutputs = schema.getOutputs().stream().map(Output::getKey).collect(Collectors.toSet());

        // test without context
        assertThat(_STAGING.getOutputs(schema)).isEqualTo(definedOutputs);

        // test with context
        assertThat(_STAGING.getOutputs(schema, lookup.getInputs())).isEqualTo(definedOutputs);
    }

    @Test
    public void testGlossary() {
        assertThat(_STAGING.getGlossaryTerms()).isNotEmpty();
        GlossaryDefinition entry = _STAGING.getGlossaryDefinition("Cortex");
        assertThat(entry).isNotNull();
        assertThat(entry.getName()).isEqualTo("Cortex");
        assertThat(entry.getDefinition()).startsWith("The external or outer surface layer of an organ");
        assertThat(entry.getAlternateNames()).contains("Cortical");
        assertThat(entry.getLastModified()).isNotNull();
    }

    @Test
    public void testMetadata() {
        Schema schema = _STAGING.getSchema("testicular");
        assertThat(schema).isNotNull();

        Input input = schema.getInputMap().get("afp_pre_orch_range");
        assertThat(input).isNotNull();

        Set<String> metadata = input.getMetadata().stream().map(Metadata::getName).collect(Collectors.toSet());
        assertThat(metadata).containsExactlyInAnyOrder("SEER_REQUIRED", "SSDI");
    }

    @Test
    public void testCachedSiteAndHistology() {
        StagingDataProvider provider = getProvider();
        assertThat(provider.getValidSites().size() > 0).isTrue();
        assertThat(provider.getValidHistologies().size() > 0).isTrue();

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

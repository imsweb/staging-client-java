package com.imsweb.staging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.imsweb.staging.entities.ColumnDefinition.ColumnType;
import com.imsweb.staging.entities.Range;
import com.imsweb.staging.entities.impl.StagingColumnDefinition;
import com.imsweb.staging.entities.impl.StagingSchema;
import com.imsweb.staging.entities.impl.StagingTable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class StagingDataProviderTest {

    @Test
    void testExtraInput() {
        StagingTable table = new StagingTable();
        table.setId("test_table");
        StagingColumnDefinition def1 = new StagingColumnDefinition();
        def1.setKey("input1");
        def1.setName("Input 1");
        def1.setType(ColumnType.INPUT);
        StagingColumnDefinition def2 = new StagingColumnDefinition();
        def2.setKey("result1");
        def2.setName("Result1");
        def2.setType(ColumnType.ENDPOINT);
        table.setColumnDefinitions(Arrays.asList(def1, def2));
        table.setRawRows(new ArrayList<>());
        table.getRawRows().add(Arrays.asList("1", "MATCH"));
        table.getRawRows().add(Arrays.asList("2", "VALUE:{{extra1}}"));
        table.getRawRows().add(Arrays.asList("{{extra2}}", "MATCH"));
        table.getRawRows().add(Arrays.asList("{{ctx_year_current}}", "MATCH"));
        table.getRawRows().add(Arrays.asList("5", "VALUE:{{ctx_year_current}}"));
        table.getRawRows().add(Arrays.asList("6", "MATCH:{{match_extra}}"));
        table.getRawRows().add(Arrays.asList("7", "ERROR:{{error_extra}}"));

        StagingDataProvider provider = new InMemoryDataProvider("test", "1.0");

        provider.initTable(table);

        // since context variables are not user-supplied, they should not be included in the extra input
        assertEquals(new HashSet<>(Arrays.asList("extra1", "extra2")), table.getExtraInput());

        table.setRawRows(new ArrayList<>());
        table.getRawRows().add(Arrays.asList("{{ctx_year_current}}", "MATCH"));

        provider.initTable(table);

        assertNull(table.getExtraInput());
    }

    @Test
    void testSplitValues() {
        StagingDataProvider provider = new InMemoryDataProvider("algorithm", "verison");

        assertEquals(0, provider.splitValues(null).size());

        assertEquals(1, provider.splitValues("").size());
        assertEquals(1, provider.splitValues("*").size());
        assertEquals(1, provider.splitValues("1 2 3").size());
        assertEquals(1, provider.splitValues("23589258625086").size());

        assertEquals(2, provider.splitValues("A,B").size());
        assertEquals(2, provider.splitValues(" A , B ").size());
        assertEquals(2, provider.splitValues("A , B").size());

        assertEquals(10, provider.splitValues("A,B,C,D,E,F,G,H,I,J").size());

        List<? extends Range> ranges = provider.splitValues(",1,2,3,4");
        assertEquals(5, ranges.size());
        assertEquals("", ranges.getFirst().getLow());
        assertEquals("", ranges.getFirst().getHigh());

        ranges = provider.splitValues("     ,1,2,3,4");
        assertEquals(5, ranges.size());
        assertEquals("", ranges.getFirst().getLow());
        assertEquals("", ranges.getFirst().getHigh());

        ranges = provider.splitValues("1,2,3,4,");
        assertEquals(5, ranges.size());
        assertEquals("", ranges.get(4).getLow());
        assertEquals("", ranges.get(4).getHigh());

        ranges = provider.splitValues("1,2,3,4,    ");
        assertEquals(5, ranges.size());
        assertEquals("", ranges.get(4).getLow());
        assertEquals("", ranges.get(4).getHigh());

        ranges = provider.splitValues("1,11,111-222");
        assertEquals(3, ranges.size());
        assertEquals("1", ranges.get(0).getLow());
        assertEquals("1", ranges.get(0).getHigh());
        assertEquals("11", ranges.get(1).getLow());
        assertEquals("11", ranges.get(1).getHigh());
        assertEquals("111", ranges.get(2).getLow());
        assertEquals("222", ranges.get(2).getHigh());

        ranges = provider.splitValues("88,90-95,99");
        assertEquals(3, ranges.size());
        assertEquals("88", ranges.get(0).getLow());
        assertEquals("88", ranges.get(0).getHigh());
        assertEquals("90", ranges.get(1).getLow());
        assertEquals("95", ranges.get(1).getHigh());
        assertEquals("99", ranges.get(2).getLow());
        assertEquals("99", ranges.get(2).getHigh());

        ranges = provider.splitValues("p0I-");
        assertEquals(1, ranges.size());
        assertEquals("p0I-", ranges.getFirst().getLow());
        assertEquals("p0I-", ranges.getFirst().getHigh());

        ranges = provider.splitValues("N0(mol-)");
        assertEquals(1, ranges.size());
        assertEquals("N0(mol-)", ranges.getFirst().getLow());
        assertEquals("N0(mol-)", ranges.getFirst().getHigh());

        // test numeric ranges
        ranges = provider.splitValues("1-21");
        assertEquals(1, ranges.size());
        assertEquals("1", ranges.getFirst().getLow());
        assertEquals("21", ranges.getFirst().getHigh());

        ranges = provider.splitValues("21-111");
        assertEquals(1, ranges.size());
        assertEquals("21", ranges.getFirst().getLow());
        assertEquals("111", ranges.getFirst().getHigh());
    }

    @Test
    void testTableRowParsing() {
        StagingTable table = new StagingTable();
        table.setId("test_table");
        table.setColumnDefinitions(
            Collections.singletonList(new StagingColumnDefinition("key1", "Input 1", ColumnType.INPUT))
        );
        table.setRawRows(new ArrayList<>());
        table.getRawRows().add(Collections.singletonList(",1,2,3"));
        table.getRawRows().add(Collections.singletonList("1,2,3,"));

        StagingDataProvider provider = new InMemoryDataProvider("test", "1.0");

        provider.initTable(table);

        assertEquals(2, table.getTableRows().size());
        assertEquals(4, table.getTableRows().get(0).getInputs().get("key1").size());
        assertEquals(4, table.getTableRows().get(1).getInputs().get("key1").size());
    }

    @Test
    void testInMemoryProviderContract() {
        InMemoryDataProvider provider = new InMemoryDataProvider("Test Algorithm", "1.2.3");

        assertEquals("Test Algorithm", provider.getAlgorithm());
        assertEquals("1.2.3", provider.getVersion());
        assertTrue(provider.getTableIds().isEmpty());
        assertTrue(provider.getSchemaIds().isEmpty());
        assertNull(provider.getTable("missing"));
        assertNull(provider.getSchema("missing"));

        StagingSchema schema = new StagingSchema("present");
        schema.setSchemaSelectionTable("selection");
        provider.addSchema(schema);
        assertEquals(Set.of("present"), provider.getSchemaIds());
        assertEquals(schema, provider.getSchema("present"));
    }

    @Test
    void testInMemoryGlossaryIsUnsupported() {
        InMemoryDataProvider provider = new InMemoryDataProvider("test", "1.0");

        assertGlossaryUnsupported(provider::getGlossaryTerms);
        assertGlossaryUnsupported(() -> provider.getGlossaryDefinition("term"));
        assertGlossaryUnsupported(() -> provider.getGlossaryMatches("text"));
    }

    private static void assertGlossaryUnsupported(Runnable operation) {
        IllegalStateException exception = assertThrows(IllegalStateException.class, operation::run);
        assertEquals("Glossary not supported in this provider", exception.getMessage());
    }

    @Test
    void testValidValuesForMissingTable() {
        StagingDataProvider provider = new InMemoryDataProvider("test", "1.0");

        assertEquals(Collections.emptySet(), provider.getValidSites());
    }

    @Test
    void testValidValuesRequireExactlyOneInputDefinition() {
        InMemoryDataProvider noInputs = new InMemoryDataProvider("test", "1.0");
        noInputs.addTable(
            table(
                StagingDataProvider.PRIMARY_SITE_TABLE,
                new StagingColumnDefinition("result", "Result", ColumnType.ENDPOINT)
            )
        );

        RuntimeException noInputException = assertThrows(RuntimeException.class, noInputs::getValidSites);
        assertInvalidInputDefinition(noInputException);

        InMemoryDataProvider multipleInputs = new InMemoryDataProvider("test", "1.0");
        multipleInputs.addTable(
            table(
                StagingDataProvider.PRIMARY_SITE_TABLE,
                new StagingColumnDefinition("site", "Site", ColumnType.INPUT),
                new StagingColumnDefinition("other", "Other", ColumnType.INPUT)
            )
        );

        RuntimeException multipleInputException = assertThrows(RuntimeException.class, multipleInputs::getValidSites);
        assertInvalidInputDefinition(multipleInputException);
    }

    private static void assertInvalidInputDefinition(RuntimeException exception) {
        assertTrue(exception.getCause() instanceof IllegalStateException);
        assertEquals(
            "Table 'primary_site' must have one and only one INPUT column.",
            exception.getCause().getMessage()
        );
    }

    @Test
    void testValidValuesExpandRangesWithZeroPadding() {
        InMemoryDataProvider provider = new InMemoryDataProvider("test", "1.0");
        StagingTable table = table(
            StagingDataProvider.PRIMARY_SITE_TABLE,
            new StagingColumnDefinition("site", "Site", ColumnType.INPUT)
        );
        table.setRawRows(
            Arrays.asList(
                Collections.singletonList("001"),
                Collections.singletonList("003-005"),
                Collections.singletonList("010")
            )
        );
        provider.addTable(table);

        assertEquals(Set.of("001", "003", "004", "005", "010"), provider.getValidSites());
        assertTrue(provider.isValidSite("004"));
        assertFalse(provider.isValidSite("4"));
    }

    private static StagingTable table(String id, StagingColumnDefinition... definitions) {
        StagingTable table = new StagingTable(id);
        table.setColumnDefinitions(Arrays.asList(definitions));
        table.setRawRows(Collections.emptyList());
        return table;
    }

    @Test
    void testPadStart() {
        assertNull(StagingDataProvider.padStart(null, 1, '0'));

        assertEquals("123", StagingDataProvider.padStart("123", 1, '0'));
        assertEquals("123", StagingDataProvider.padStart("123", 3, '0'));
        assertEquals("0123", StagingDataProvider.padStart("123", 4, '0'));
        assertEquals("00001", StagingDataProvider.padStart("1", 5, '0'));
    }

    @Test
    void testIsNumeric() {
        assertTrue(StagingDataProvider.isNumeric("0"));
        assertTrue(StagingDataProvider.isNumeric("1"));
        assertTrue(StagingDataProvider.isNumeric("-1"));
        assertTrue(StagingDataProvider.isNumeric("1.1"));

        assertFalse(StagingDataProvider.isNumeric(null));
        assertFalse(StagingDataProvider.isNumeric(""));
        assertFalse(StagingDataProvider.isNumeric("1.1.1"));
        assertFalse(StagingDataProvider.isNumeric("NAN"));
    }
}

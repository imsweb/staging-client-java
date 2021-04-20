# staging-client-java

[![integration](https://github.com/imsweb/staging-client-java/workflows/integration/badge.svg)](https://github.com/imsweb/staging-client-java/actions)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.imsweb/staging-client-java/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.imsweb/staging-client-java)

A cancer staging client library for Java applications.

## Supported staging algorithms

Starting with version 4.0, the library does not contain any algorithms.  The algorithms are maintained in separate repositories which are 
described below.

### EOD ([view repository](https://github.com/imsweb/staging-algorithm-eod-public))

Extent of Disease (EOD) is a set of three data items that describe how far a cancer has spread at the time of diagnosis. EOD 2018 is effective for 
cases diagnosed in 2018 and later.
 
In each EOD schema, valid values, definitions, and registrar notes are provided for
 
- EOD Primary Tumor
- EOD Lymph Nodes
- EOD Mets
- Summary Stage 2018
- Site-Specific Data Items (SSDIs), including grade, pertinent to the schema

For cancer cases diagnosed January 1, 2018 and later, the NCI SEER program will collect Extent of Disease (EOD) revised for 2018 and Summary Stage 2018. The 
schemas have been developed to be compatible with the AJCC 8th Edition chapter definitions. 

All of the standard setting organizations will collect the predictive and prognostic factors through Site Specific Data Items (SSDIs). Unlike the 
SSFs, these data items have formats and code structures specific to the data item.


### TNM ([view repository](https://github.com/imsweb/staging-algorithm-tnm))

TNM is a widely accepted system of cancer staging. TNM stands for Tumor, Nodes, and Metastasis. T is assigned based on the extent of involvement at the 
primary tumor site, N for the extent of involvement in regional lymph nodes, and M for distant spread. Clinical TNM is assigned prior to treatment and 
pathologic TNM is assigned based on clinical information plus information from surgery. The clinical TNM and the pathologic TNM values are summarized 
as clinical stage group or pathologic stage group.

For each cancer site, or schema, valid values, definitions, and registrar notes are provided for clinical TNM and stage group, pathologic TNM and stage 
group, and relevant Site-Specific Factors (SSFs).

TNM categories, stage groups, and definitions are based on the Union for International Cancer Control ([UICC](http://www.uicc.org/)) TNM 7th edition 
classification.  UICC 7th edition and AJCC 7th edition TNM categories and stage groups are very similar; however, there are some differences.

For diagnosis years 2016-2017, SEER Summary Stage 2000 is required. SEER Summary Stage 2000 should be collected manually unless the registry is collecting 
Collaborative Stage, which would derive Summary Stage 2000.

### Collaborative Staging ([view repository](https://github.com/imsweb/staging-algorithm-cs))

Collaborative Stage is a unified data collection system designed to provide a common data set to meet the needs of all three
staging systems (TNM, SEER EOD, and SEER SS). It provides a comprehensive system to improve data quality by standardizing rules for timing, clinical and pathologic
assessments, and compatibility across all of the systems for all cancer sites.

## Download

Java 8 is the minimum version required to use the library.

Download [the latest JAR][1] or grab via Maven:

```xml
<dependency>
    <groupId>com.imsweb</groupId>
    <artifactId>staging-client-java</artifactId>
    <version>5.0.2</version>
</dependency>
```

or via Gradle:

```groovy
compile 'com.imsweb:staging-client-java:5.0.2'
```

## Usage

More detailed documentation can be found in the [Wiki](https://github.com/imsweb/staging-client-java/wiki/)

### Get a `Staging` instance

Everything starts with getting an instance of the `Staging` object.  There are `DataProvider` objects for each staging algorithm and version.  The `Staging`
object is thread safe and cached so subsequent calls to `Staging.getInstance()` will return the same object.

For example, if you include the [collaborative staging](https://github.com/imsweb/staging-algorithm-cs) algorithm, the call will look like this:

```java
Staging staging = Staging.getInstance(CsDataProvider.getInstance(CsVersion.v020550));
```

There are some algorithms that are distributed outside of the library.  If you need to instantiate a `Staging` instance with an external algorithm use 
the `ExternalStagingFileDataProvider`.

```java
Path path = Paths.get("C:/path/to/algorithm", "external_algorithm.zip");
try (InputStream is = Files.newInputStream(path)) {
    Staging staging = Staging.getInstance(new ExternalStagingFileDataProvider(is));

    // use staging instance
}
```

### Schemas

Schemas represent sets of specific staging instructions.  Determining the schema to use for staging is based on primary site, histology and sometimes additional
discrimator values.  Schemas include the following information:

- schema identifier (i.e. "prostate")
- algorithm identifier (i.e. "cs")
- algorithm version (i.e. "02.05.50")
- name
- title, subtitle, description and notes
- schema selection criteria
- input definitions describing the data needed for staging
- list of table identifiers involved in the schema
- a list of initial output values set at the start of staging
- a list of mappings which represent the logic used to calculate staging output

To get a list of all schema identifiers,

```java
Set<String> schemaIds = staging.getSchemaIds();
```

To get a single schema by identifer,

```java
StagingSchema schema = staging.getSchema("prostate");
```

### Tables

Tables represent the building blocks of the staging instructions specified in schemas.  Tables are used to define schema selection criteria, input 
validation and staging logic. Tables include the following information:

- table identifier (i.e. "ajcc7_stage")
- algorithm identifier (i.e. "cs")
- algorithm version (i.e. "02.05.50")
- name
- title, subtitle, description, notes and footnotes
- list of column definitions
- list of table data

To get a list of all table identifiers,

```java
Set<String> tableIds = staging.getTableIds();
```

That list will be quite large.  To get a list of table indentifiers involved in a particular schema,

```java
Set<String> tableIds = staging.getInvolvedTables("prostate");
```

To get a single table by identifer,

```java
StagingTable table = staging.getTable("ajcc7_stage");
```

### Lookup a schema

A common operation is to look up a schema based on primary site, histology and optionally one or more discriminators.  Each staging algorithm has a `SchemaLookup` object
customized for the specific inputs needed to lookup a schema.

For Collaborative Staging, use the `CsSchemaLookup` object.  Here is a lookup based on site and histology.

```java
List<StagingSchema> lookup = staging.lookupSchema(new CsSchemaLookup("C629", "9231"));
assertEquals(1, lookup.size());
assertEquals("testis", lookup.get(0).getId());
```

If the call returns a single result, then it was successful.  If it returns more than one result, then it needs a discriminator.  Information about the required discriminator
is included in the list of results.  In the Collaborative Staging example, the field `ssf25` is always used as the discriminator.  Other staging algorithms may use different
sets of discriminators that can be determined based on the result.

```java
// do not supply a discriminator
List<StagingSchema> lookup = staging.lookupSchema(new CsSchemaLookup("C111", "8200"));
assertEquals(2, lookup.size());
for (StagingSchema schema : lookup)
    assertTrue(schema.getSchemaDiscriminators().contains(CsStagingData.SSF25_KEY));

// supply a discriminator
lookup = staging.lookupSchema(new CsSchemaLookup("C111", "8200", "010"));
assertEquals(1, lookup.size());
assertEquals("nasopharynx", lookup.get(0).getId());
assertEquals(Integer.valueOf(34), lookup.get(0).getSchemaNum());
```

### Calculate stage

Staging a case requires first knowing which schema you are working with.  Once you have the schema, you can tell which fields (keys) need to be collected and supplied
to the `stage` method call.

A `StagingData` object is used to make staging calls.  All inputs to staging should be set on the `StagingData` object and the staging call will add the results.  The
results include:

- output - all output values resulting from the calculation
- errors - a list of errors and their descriptions
- path - an ordered list of the tables that were used in the calculation

Each algorithm has a specific `StagingData` entity which helps with preparing and evaluating staging calls.  For Collaborative Staging, use `CsStagingData`.  One
difference between this library and the original Collaborative Stage library is that you do not have pass all 25 site-specific factors for every staging call. Only
include the ones that are used in the schema being staged.

```java
CsStagingData data = new CsStagingData();
data.setInput(CsInput.PRIMARY_SITE, "C680");
data.setInput(CsInput.HISTOLOGY, "8000");
data.setInput(CsInput.BEHAVIOR, "3");
data.setInput(CsInput.GRADE, "9");
data.setInput(CsInput.DX_YEAR, "2013");
data.setInput(CsInput.CS_VERSION_ORIGINAL, "020550");
data.setInput(CsInput.TUMOR_SIZE, "075");
data.setInput(CsInput.EXTENSION, "100");
data.setInput(CsInput.EXTENSION_EVAL, "9");
data.setInput(CsInput.LYMPH_NODES, "100");
data.setInput(CsInput.LYMPH_NODES_EVAL, "9");
data.setInput(CsInput.REGIONAL_NODES_POSITIVE, "99");
data.setInput(CsInput.REGIONAL_NODES_EXAMINED, "99");
data.setInput(CsInput.METS_AT_DX, "10");
data.setInput(CsInput.METS_EVAL, "9");
data.setInput(CsInput.LVI, "9");
data.setInput(CsInput.AGE_AT_DX, "060");
data.setSsf(1, "020");

// perform the staging
staging.stage(data);

assertEquals(Result.STAGED, data.getResult());
assertEquals("urethra", data.getSchemaId());
assertEquals(0, data.getErrors().size());
assertEquals(37, data.getPath().size());

// check output
assertEquals("129", data.getOutput(CsOutput.SCHEMA_NUMBER));
assertEquals("020550", data.getOutput(CsOutput.CSVER_DERIVED));

// AJCC 6
assertEquals("T1", data.getOutput(CsOutput.AJCC6_T));
assertEquals("c", data.getOutput(CsOutput.AJCC6_TDESCRIPTOR));
assertEquals("N1", data.getOutput(CsOutput.AJCC6_N));
assertEquals("c", data.getOutput(CsOutput.AJCC6_NDESCRIPTOR));
assertEquals("M1", data.getOutput(CsOutput.AJCC6_M));
assertEquals("c", data.getOutput(CsOutput.AJCC6_MDESCRIPTOR));
assertEquals("IV", data.getOutput(CsOutput.AJCC6_STAGE));
assertEquals("10", data.getOutput(CsOutput.STOR_AJCC6_T));
assertEquals("c", data.getOutput(CsOutput.STOR_AJCC6_TDESCRIPTOR));
assertEquals("10", data.getOutput(CsOutput.STOR_AJCC6_N));
assertEquals("c", data.getOutput(CsOutput.STOR_AJCC6_NDESCRIPTOR));
assertEquals("10", data.getOutput(CsOutput.STOR_AJCC6_M));
assertEquals("c", data.getOutput(CsOutput.STOR_AJCC6_MDESCRIPTOR));
assertEquals("70", data.getOutput(CsOutput.STOR_AJCC6_STAGE));

// AJCC 7
assertEquals("T1", data.getOutput(CsOutput.AJCC7_T));
assertEquals("c", data.getOutput(CsOutput.AJCC7_TDESCRIPTOR));
assertEquals("N1", data.getOutput(CsOutput.AJCC7_N));
assertEquals("c", data.getOutput(CsOutput.AJCC7_NDESCRIPTOR));
assertEquals("M1", data.getOutput(CsOutput.AJCC7_M));
assertEquals("c", data.getOutput(CsOutput.AJCC7_MDESCRIPTOR));
assertEquals("IV", data.getOutput(CsOutput.AJCC7_STAGE));
assertEquals("100", data.getOutput(CsOutput.STOR_AJCC7_T));
assertEquals("c", data.getOutput(CsOutput.STOR_AJCC6_TDESCRIPTOR));
assertEquals("100", data.getOutput(CsOutput.STOR_AJCC7_N));
assertEquals("c", data.getOutput(CsOutput.STOR_AJCC7_NDESCRIPTOR));
assertEquals("100", data.getOutput(CsOutput.STOR_AJCC7_M));
assertEquals("c", data.getOutput(CsOutput.STOR_AJCC7_MDESCRIPTOR));
assertEquals("700", data.getOutput(CsOutput.STOR_AJCC7_STAGE));

// Summary Stage
assertEquals("L", data.getOutput(CsOutput.SS1977_T));
assertEquals("RN", data.getOutput(CsOutput.SS1977_N));
assertEquals("D", data.getOutput(CsOutput.SS1977_M));
assertEquals("D", data.getOutput(CsOutput.SS1977_STAGE));
assertEquals("L", data.getOutput(CsOutput.SS2000_T));
assertEquals("RN", data.getOutput(CsOutput.SS2000_N));
assertEquals("D", data.getOutput(CsOutput.SS2000_M));
assertEquals("D", data.getOutput(CsOutput.SS2000_STAGE));
assertEquals("7", data.getOutput(CsOutput.STOR_SS1977_STAGE));
assertEquals("7", data.getOutput(CsOutput.STOR_SS2000_STAGE));
```

## About SEER

The Surveillance, Epidemiology and End Results ([SEER](http://seer.cancer.gov)) Program is a premier source for cancer statistics in the United States. The SEER
Program collects information on incidence, prevalence and survival from specific geographic areas representing 28 percent of the US population and reports on all
these data plus cancer mortality data for the entire country.

[1]: http://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=com.imsweb&a=staging-client-java&v=LATEST

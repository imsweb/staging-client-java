# staging-client-java

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=imsweb_staging-client-java&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=imsweb_staging-client-java)
[![integration](https://github.com/imsweb/staging-client-java/workflows/integration/badge.svg)](https://github.com/imsweb/staging-client-java/actions)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.imsweb/staging-client-java/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.imsweb/staging-client-java)

A cancer staging client library for Java applications.

## Supported staging algorithms

### Toronto Pedatric Staging 0.5

Toronto Stage was developed based on a consensus meeting by the Union for Internal Cancer Control (UICC) in 2014. Since this time, multiple countries have
implemented the Toronto Staging Guidelines for pediatric cancers. Starting in 2024, the United States will also implement the Toronto Staging in the SEER
program. For the US, this requires the collection of the EOD 2018 data and expects the EOD 2018 API/library to be called prior to a call to the Toronto
API/library. Toronto Stage is effective for cases diagnosed in 2024 and later (2018 and later in beta testing).

In each Toronto schema, valid values, definitions, and registrar notes are provided for any Site-Specific Data Items (SSDIs) pertinent to the schema.

For cancer cases diagnosed January 1, 2024 and later (2018 and later in beta testing), the NCI SEER program will collect the related SSDIs for each schema.
The schemas have been developed to be compatible with the Toronto Staging definitions. For some schemas, additional data items may be derived. Derived Toronto
T, N, M and Stage Group will always be present, with defaults being set for those schemas where these concepts are not defined. A Toronto Schema ID will also
be calculated.

All the standard setting organizations will collect the predictive and prognostic factors through Site Specific Data Items (SSDIs). Unlike the SSFs, these data
items have formats and code structures specific to the data item.

To get started using the Toronto Pediatric algorithm, instantiate a `Staging` instance:

```java
Staging staging = Staging.getInstance(TorontoDataProvider.getInstance(TorontoVersion.LATEST));
```

### EOD 3.0

Extent of Disease (EOD) is a set of three data items that describe how far a cancer has spread at the time of diagnosis. EOD 2018 is effective for
cases diagnosed in 2018 and later.

In each EOD schema, valid values, definitions, and registrar notes are provided for

- EOD Primary Tumor
- EOD Lymph Nodes
- EOD Mets
- Summary Stage 2018
- Site-Specific Data Items (SSDIs), including grade, pertinent to the schema

For cancer cases diagnosed January 1, 2018 and later, the NCI SEER program will collect Extent of Disease (EOD) revised for 2018 and Summary Stage 2018. The schemas have
been developed to be compatible with the AJCC 8th Edition chapter definitions.

All the standard setting organizations will collect the predictive and prognostic factors through Site Specific Data Items (SSDIs). Unlike the SSFs, these data items have
formats and code structures specific to the data item.

To get started using the EOD algorithm, instantiate a `Staging` instance:

```java
Staging staging = Staging.getInstance(EodDataProvider.getInstance(EodVersion.LATEST));
```

### TNM 2.0

TNM is a widely accepted system of cancer staging. TNM stands for Tumor, Nodes, and Metastasis. T is assigned based on the extent of involvement at the
primary tumor site, N for the extent of involvement in regional lymph nodes, and M for distant spread. Clinical TNM is assigned prior to treatment and
pathologic TNM is assigned based on clinical information plus information from surgery. The clinical TNM and the pathologic TNM values are summarized
as clinical stage group or pathologic stage group.

For each cancer site, or schema, valid values, definitions, and registrar notes are provided for clinical TNM and stage group, pathologic TNM and stage
group, and relevant Site-Specific Factors (SSFs).

TNM categories, stage groups, and definitions are based on the Union for International Cancer Control ([UICC](http://www.uicc.org/)) TNM 7th edition
classification. UICC 7th edition and AJCC 7th edition TNM categories and stage groups are very similar; however, there are some differences.

For diagnosis years 2016-2017, SEER Summary Stage 2000 is required. SEER Summary Stage 2000 should be collected manually unless the registry is collecting
Collaborative Stage, which would derive Summary Stage 2000.

To get started using the TNM algorithm, instantiate a `Staging` instance:

```java
Staging staging = Staging.getInstance(TnmDataProvider.getInstance(TnmVersion.LATEST));
```

### Collaborative Staging 02.05.50

Collaborative Stage is a unified data collection system designed to provide a common data set to meet the needs of all three staging systems (TNM, SEER EOD, and SEER SS). It
provides a comprehensive system to improve data quality by standardizing rules for timing, clinical and pathologic assessments, and compatibility across all the systems for
all cancer sites.

To get started using the CS algorithm, instantiate a `Staging` instance:

```java
Staging staging = Staging.getInstance(CsDataProvider.getInstance(CsVersion.LATEST));
```

## Download

Java 8 is the minimum version required to use the library.

If you are interested in just the library without any bundled algorithm, it can be included with the following.

Maven

```xml
<dependency>
    <groupId>com.imsweb</groupId>
    <artifactId>staging-client-java</artifactId>
    <version>x.x.x</version>
</dependency>
```

Gradle

```groovy
compile 'com.imsweb:staging-client-java:x.x.x'
```

If you are interested in a specific algorithm, you can include them using their specific artifact.

Maven

```xml
<dependency>
    <groupId>com.imsweb</groupId>
    <artifactId>staging-client-java-cs</artifactId>
    <version>x.x.x</version>
</dependency>
<dependency>
    <groupId>com.imsweb</groupId>
    <artifactId>staging-client-java-eod-public</artifactId>
    <version>x.x.x</version>
</dependency>
<dependency>
    <groupId>com.imsweb</groupId>
    <artifactId>staging-client-java-tnm</artifactId>
    <version>x.x.x</version>
</dependency>
<dependency>
    <groupId>com.imsweb</groupId>
    <artifactId>staging-client-java-toronto</artifactId>
    <version>x.x.x</version>
</dependency>
```

Gradle

```groovy
implementation 'com.imsweb:staging-client-java-cs:x.x.x'
implementation 'com.imsweb:staging-client-java-eod-public:x.x.x'
implementation 'com.imsweb:staging-client-java-tnm:x.x.x'
implementation 'com.imsweb:staging-client-java-toronto:x.x.x'
```

Note that the algorithm artifacts only include the latest version of each algorithm. If an older version is necessary, the algorithm zip file can be downloaded and 
initialized using an `ExternalStagingFileDataProvider`.

Here are recent releases which included new algorithm versions.

| staging-client-java                                                          | Algorithm           | Version  | Algorithm ZIP                                                                                                    |
|------------------------------------------------------------------------------|---------------------|----------|------------------------------------------------------------------------------------------------------------------|
| [10.2.0](https://github.com/imsweb/staging-client-java/releases/tag/v10.2.0) | Toronto Pediatric   | 0.5      | [toronto-0.5.zip](https://github.com/imsweb/staging-client-java/releases/download/v10.2.0/toronto-0.5.zip)       |
| [10.1.0](https://github.com/imsweb/staging-client-java/releases/tag/v10.1.0) | Toronto Pediatric   | 0.4      | [toronto-0.4.zip](https://github.com/imsweb/staging-client-java/releases/download/v10.1.0/toronto-0.4.zip)       |
| [10.0.0](https://github.com/imsweb/staging-client-java/releases/tag/v10.0.0) | Toronto Pediatric   | 0.3      | [toronto-0.3.zip](https://github.com/imsweb/staging-client-java/releases/download/v10.0.0/toronto-0.3.zip)       |
| [10.0.0](https://github.com/imsweb/staging-client-java/releases/tag/v10.0.0) | EOD                 | 3.0      | [eod_public-3.0.zip](https://github.com/imsweb/staging-client-java/releases/download/v10.0.0/eod_public-3.0.zip) |
| [10.0.0](https://github.com/imsweb/staging-client-java/releases/tag/v10.0.0) | TNM                 | 2.0      | [tnm-2.0.zip](https://github.com/imsweb/staging-client-java/releases/download/v10.0.0/tnm-2.0.zip)               |
| [10.0.0](https://github.com/imsweb/staging-client-java/releases/tag/v10.0.0) | Collaborative Stage | 02.05.50 | [cs-02.05.50.zip](https://github.com/imsweb/staging-client-java/releases/download/v10.0.0/cs-02.05.50.zip)       |
| [9.1](https://github.com/imsweb/staging-client-java/releases/tag/v9.1)       | Toronto Pediatric   | 0.2      | [toronto-0.2.zip](https://github.com/imsweb/staging-client-java/releases/download/v9.1/toronto-0.2.zip)          |
| [9.0](https://github.com/imsweb/staging-client-java/releases/tag/v9.0)       | EOD                 | 2.1      | [eod_public-2.1.zip](https://github.com/imsweb/staging-client-java/releases/download/v8.0/eod_public-2.1.zip)    |
| [9.0](https://github.com/imsweb/staging-client-java/releases/tag/v9.0)       | TNM                 | 1.9      | [tnm-1.9.zip](https://github.com/imsweb/staging-client-java/releases/download/v8.0/tnm-1.9.zip)                  |

## Usage

More detailed documentation can be found in the [Wiki](https://github.com/imsweb/staging-client-java/wiki/)

### Get a `Staging` instance

Everything starts with getting an instance of the `Staging` object. There are `DataProvider` objects for each staging algorithm and version. The `Staging`
object is thread safe and cached so subsequent calls to `Staging.getInstance()` will return the same object.

For example, for the Collaborative Staging algorithm, the call will look like this:

```java
Staging staging = Staging.getInstance(CsDataProvider.getInstance(CsVersion.LATEST));
```

Each release contains links to all the current algorithms. There could be times when you want to load an older version of an algorithm. You can get the algorithm
zip file from the release page and load it using `ExternalStagingFileDataProvider`. 

```java
Path path = Paths.get("C:/path/to/algorithm", "tnm-1.9.zip");
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
Schema schema = staging.getSchema("prostate");
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
Table table = staging.getTable("ajcc7_stage");
```

### Lookup a schema

A common operation is to look up a schema based on primary site, histology and optionally one or more discriminators. Each staging algorithm has a `SchemaLookup` object
customized for the specific inputs needed to lookup a schema.

For Collaborative Staging, use the `CsSchemaLookup` object (each algorithm has their own lookup class). Here is a lookup based on site and histology.

```java
List<Schema> lookup = staging.lookupSchema(new CsSchemaLookup("C629", "9231"));
assertEquals(1, lookup.size());
assertEquals("testis", lookup.get(0).getId());
```

If the call returns a single result, then it was successful.  If it returns more than one result, then it needs a discriminator.  Information about the required discriminator
is included in the list of results.  In the Collaborative Staging example, the field `ssf25` is always used as the discriminator.  Other staging algorithms may use different
sets of discriminators that can be determined based on the result.

```java
// do not supply a discriminator
List<Schema> lookup = staging.lookupSchema(new CsSchemaLookup("C111", "8200"));
assertEquals(2, lookup.size());
for (Schema schema : lookup)
    assertTrue(schema.getSchemaDiscriminators().contains(CsStagingData.SSF25_KEY));

// supply a discriminator
lookup = staging.lookupSchema(new CsSchemaLookup("C111", "8200", "010"));
assertEquals(1, lookup.size());
assertEquals("nasopharynx", lookup.get(0).getId());
assertEquals(Integer.valueOf(34), lookup.get(0).getSchemaNum());
```

### Calculate stage

Staging a case requires first knowing which schema you are working with. Once you have the schema, you can tell which fields (keys) need to be collected and supplied
to the `stage` method call.

A `StagingData` object is used to make staging calls. All inputs to staging should be set on the `StagingData` object and the staging call will add the results. The
results include:

- output - all output values resulting from the calculation
- errors - a list of errors and their descriptions
- path - an ordered list of the tables that were used in the calculation

Each algorithm has a specific `StagingData` entity which helps with preparing and evaluating staging calls. For Collaborative Staging, use `CsStagingData`. One
difference between this library and the original Collaborative Stage library is that you no longer have to pass all 25 site-specific factors for every staging call. Only
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

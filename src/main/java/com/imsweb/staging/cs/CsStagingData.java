/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.cs;

import com.imsweb.staging.StagingData;

public class CsStagingData extends StagingData {

    // key definitions
    public static final String SSF25_KEY = "ssf25";

    // SSF prefix
    public static final String INPUT_SSF_PREFIX = "ssf";

    // input key definitions
    public enum CsInput {
        PRIMARY_SITE("site"),
        HISTOLOGY("hist"),
        DX_YEAR("year_dx"),
        CS_VERSION_ORIGINAL("cs_input_version_original"),
        BEHAVIOR("behavior"),
        GRADE("grade"),
        LVI("lvi"),
        AGE_AT_DX("age_dx"),
        SEX("sex"),
        TUMOR_SIZE("size"),
        EXTENSION("extension"),
        EXTENSION_EVAL("extension_eval"),
        LYMPH_NODES("nodes"),
        LYMPH_NODES_EVAL("nodes_eval"),
        REGIONAL_NODES_POSITIVE("nodes_pos"),
        REGIONAL_NODES_EXAMINED("nodes_exam"),
        METS_AT_DX("mets"),
        METS_EVAL("mets_eval"),
        SSF1("ssf1"),
        SSF2("ssf2"),
        SSF3("ssf3"),
        SSF4("ssf4"),
        SSF5("ssf5"),
        SSF6("ssf6"),
        SSF7("ssf7"),
        SSF8("ssf8"),
        SSF9("ssf9"),
        SSF10("ssf10"),
        SSF11("ssf11"),
        SSF12("ssf12"),
        SSF13("ssf13"),
        SSF14("ssf14"),
        SSF15("ssf15"),
        SSF16("ssf16"),
        SSF17("ssf17"),
        SSF18("ssf18"),
        SSF19("ssf19"),
        SSF20("ssf20"),
        SSF21("ssf21"),
        SSF22("ssf22"),
        SSF23("ssf23"),
        SSF24("ssf24"),
        SSF25("ssf25");

        private String _name;

        CsInput(String name) {
            _name = name;
        }

        @Override
        public String toString() {
            return _name;
        }
    }

    // output key definitions
    public enum CsOutput {
        SCHEMA_NUMBER("schema_number"),
        CSVER_DERIVED("csver_derived"),
        AJCC6_T("ajcc6_t"),
        AJCC6_TDESCRIPTOR("ajcc6_tdescriptor"),
        AJCC6_N("ajcc6_n"),
        AJCC6_NDESCRIPTOR("ajcc6_ndescriptor"),
        AJCC6_M("ajcc6_m"),
        AJCC6_MDESCRIPTOR("ajcc6_mdescriptor"),
        AJCC6_STAGE("ajcc6_stage"),
        AJCC7_T("ajcc7_t"),
        AJCC7_TDESCRIPTOR("ajcc7_tdescriptor"),
        AJCC7_N("ajcc7_n"),
        AJCC7_NDESCRIPTOR("ajcc7_ndescriptor"),
        AJCC7_M("ajcc7_m"),
        AJCC7_MDESCRIPTOR("ajcc7_mdescriptor"),
        AJCC7_STAGE("ajcc7_stage"),
        SS1977_T("t77"),
        SS1977_N("n77"),
        SS1977_M("m77"),
        SS1977_STAGE("ss77"),
        SS2000_T("t2000"),
        SS2000_N("n2000"),
        SS2000_M("m2000"),
        SS2000_STAGE("ss2000"),
        STOR_AJCC6_T("stor_ajcc6_t"),
        STOR_AJCC6_TDESCRIPTOR("stor_ajcc6_tdescriptor"),
        STOR_AJCC6_N("stor_ajcc6_n"),
        STOR_AJCC6_NDESCRIPTOR("stor_ajcc6_ndescriptor"),
        STOR_AJCC6_M("stor_ajcc6_m"),
        STOR_AJCC6_MDESCRIPTOR("stor_ajcc6_mdescriptor"),
        STOR_AJCC6_STAGE("stor_ajcc6_stage"),
        STOR_AJCC7_T("stor_ajcc7_t"),
        STOR_AJCC7_TDESCRIPTOR("stor_ajcc7_tdescriptor"),
        STOR_AJCC7_N("stor_ajcc7_n"),
        STOR_AJCC7_NDESCRIPTOR("stor_ajcc7_ndescriptor"),
        STOR_AJCC7_M("stor_ajcc7_m"),
        STOR_AJCC7_MDESCRIPTOR("stor_ajcc7_mdescriptor"),
        STOR_AJCC7_STAGE("stor_ajcc7_stage"),
        STOR_SS1977_STAGE("stor_ss77"),
        STOR_SS2000_STAGE("stor_ss2000");

        private String _name;

        CsOutput(String name) {
            _name = name;
        }

        @Override
        public String toString() {
            return _name;
        }
    }

    /**
     * Default constructor
     */
    public CsStagingData() {
        super();
    }

    /**
     * Construct with site and histology
     * @param site primary site
     * @param hist histology
     */
    public CsStagingData(String site, String hist) {
        super(site, hist);
    }

    /**
     * Construct with site, histology and SSF25
     * @param site primary site
     * @param hist histology
     * @param ssf25 site-specific factor 25
     */
    public CsStagingData(String site, String hist, String ssf25) {
        super(site, hist);

        setSsf(25, ssf25);
    }

    /**
     * Return the specified input value
     * @param key input key
     * @return input
     */
    public String getInput(CsInput key) {
        return getInput(key.toString());
    }

    /**
     * Set the specified input value
     * @param key input key
     * @param value value
     */
    public void setInput(CsInput key, String value) {
        setInput(key.toString(), value);
    }

    /**
     * Return the specified output value
     * @param key output key
     * @return output
     */
    public String getOutput(CsOutput key) {
        return getOutput(key.toString());
    }

    /**
     * Get the specified input site-specific factor
     * @param id site-specific factor number
     * @return ssf value
     */
    public String getSsf(Integer id) {
        if (id < 1 || id > 25)
            throw new IllegalStateException("Site specific factor must be between 1 and 25.");

        return getInput(INPUT_SSF_PREFIX + id);
    }

    /**
     * Set the specified input site-specific factor
     * @param id site-specific factor number
     * @param ssf site-specfic factor value
     */
    public void setSsf(Integer id, String ssf) {
        if (id < 1 || id > 25)
            throw new IllegalStateException("Site specific factor must be between 1 and 25.");

        setInput(INPUT_SSF_PREFIX + id, ssf);
    }

    /**
     * CsStagingInputBuilder builder
     */
    public static class CsStagingInputBuilder {

        private CsStagingData _data;

        public CsStagingInputBuilder() {
            _data = new CsStagingData();
        }

        public CsStagingInputBuilder withSsf(Integer id, String ssf) {
            _data.setSsf(id, ssf);
            return this;
        }

        public CsStagingInputBuilder withInput(CsInput key, String value) {
            _data.setInput(key, value);
            return this;
        }

        public CsStagingData build() {
            return _data;
        }
    }
}

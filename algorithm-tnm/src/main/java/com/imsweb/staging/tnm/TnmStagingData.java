/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.tnm;

import com.imsweb.staging.entities.StagingData;

public class TnmStagingData extends StagingData {

    // key definitions
    public static final String SEX_KEY = "sex";
    public static final String SSF25_KEY = "ssf25";

    // SSF prefix
    public static final String INPUT_SSF_PREFIX = "ssf";

    // input key definitions
    public enum TnmInput {
        PRIMARY_SITE("site"),
        HISTOLOGY("hist"),
        BEHAVIOR("behavior"),
        GRADE("grade"),
        SEX("sex"),
        DX_YEAR("year_dx"),
        AGE_AT_DX("age_dx"),
        CLIN_STAGE_GROUP_DIRECT("clin_stage_group_direct"),
        PATH_STAGE_GROUP_DIRECT("path_stage_group_direct"),
        RX_SUMM_SURGERY("systemic_surg_seq"),
        RX_SUMM_RADIATION("radiation_surg_seq"),
        CLIN_T("clin_t"),
        CLIN_N("clin_n"),
        CLIN_M("clin_m"),
        PATH_T("path_t"),
        PATH_N("path_n"),
        PATH_M("path_m"),
        REGIONAL_NODES_POSITIVE("nodes_pos"),
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
        SSF24("ssf24");

        private final String _name;

        TnmInput(String name) {
            _name = name;
        }

        @Override
        public String toString() {
            return _name;
        }
    }

    // output key definitions
    public enum TnmOutput {
        DERIVED_VERSION("derived_version"),
        CLIN_STAGE_GROUP("clin_stage_group"),
        PATH_STAGE_GROUP("path_stage_group"),
        COMBINED_STAGE_GROUP("combined_stage_group"),
        SOURCE_T("source_t"),
        COMBINED_T("combined_t"),
        SOURCE_N("source_n"),
        COMBINED_N("combined_n"),
        SOURCE_M("source_m"),
        COMBINED_M("combined_m");

        private final String _name;

        TnmOutput(String name) {
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
    public TnmStagingData() {
        super();
    }

    /**
     * Construct with site and histology
     * @param site primary site
     * @param hist histology
     */
    public TnmStagingData(String site, String hist) {
        super(site, hist);
    }

    /**
     * Construct with site, histology and SSF25
     * @param site primary site
     * @param hist histology
     * @param ssf25 site-specific factor 25
     */
    public TnmStagingData(String site, String hist, String ssf25) {
        super(site, hist);

        setSsf(25, ssf25);
    }

    /**
     * Return the specified input value
     * @param key input key
     * @return input
     */
    public String getInput(TnmInput key) {
        return getInput(key.toString());
    }

    /**
     * Set the specified input value
     * @param key input key
     * @param value value
     */
    public void setInput(TnmInput key, String value) {
        setInput(key.toString(), value);
    }

    /**
     * Return the specified output value
     * @param key output key
     * @return output
     */
    public String getOutput(TnmOutput key) {
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
     * TnmStagingInputBuilder builder
     */
    public static class TnmStagingInputBuilder {

        private final TnmStagingData _data;

        public TnmStagingInputBuilder() {
            _data = new TnmStagingData();
        }

        public TnmStagingInputBuilder withSsf(Integer id, String ssf) {
            _data.setSsf(id, ssf);
            return this;
        }

        public TnmStagingInputBuilder withInput(TnmInput key, String value) {
            _data.setInput(key, value);
            return this;
        }

        public TnmStagingData build() {
            return _data;
        }
    }
}

/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.eod;

import com.imsweb.staging.StagingData;

public class EodStagingData extends StagingData {

    /**
     * Default constructor
     */
    public EodStagingData() {
        super();
    }

    /**
     * Construct with site and histology
     * @param site primary site
     * @param hist histology
     */
    public EodStagingData(String site, String hist) {
        super(site, hist);
    }

    /**
     * Construct with site, histology and discriminators
     * @param site primary site
     * @param hist histology
     * @param discriminator1 first discriminator
     */
    public EodStagingData(String site, String hist, String discriminator1) {
        super(site, hist);

        setInput(EodInput.DISCRIMINATOR_1, discriminator1);
    }

    /**
     * Construct with site, histology and discriminators
     * @param site primary site
     * @param hist histology
     * @param discriminator1 first discriminator
     * @param discriminator2 second discriminator
     */
    public EodStagingData(String site, String hist, String discriminator1, String discriminator2) {
        super(site, hist);

        setInput(EodInput.DISCRIMINATOR_1, discriminator1);
        setInput(EodInput.DISCRIMINATOR_2, discriminator2);
    }

    /**
     * Return the specified input value
     * @param key input key
     * @return input
     */
    public String getInput(EodInput key) {
        return getInput(key.toString());
    }

    /**
     * Set the specified input value
     * @param key input key
     * @param value value
     */
    public void setInput(EodInput key, String value) {
        setInput(key.toString(), value);
    }

    /**
     * Return the specified output value
     * @param key output key
     * @return output
     */
    public String getOutput(EodOutput key) {
        return getOutput(key.toString());
    }

    // input key definitions
    public enum EodInput {
        PRIMARY_SITE("site"),
        HISTOLOGY("hist"),
        BEHAVIOR("behavior"),
        SEX("sex"),
        AGE_AT_DX("age_dx"),
        DISCRIMINATOR_1("discriminator_1"),
        DISCRIMINATOR_2("discriminator_2"),
        NODES_POS("nodes_pos"),
        NODES_EXAM("nodes_exam"),
        EOD_PRIMARY_TUMOR("eod_primary_tumor"),
        EOD_REGIONAL_NODES("eod_regional_nodes"),
        EOD_METS("eod_mets"),
        GRADE_CLIN("grade_clin"),
        GRADE_PATH("grade_path"),
        GRADE_POST_THERAPY("grade_post_therapy"),
        DX_YEAR("year_dx"),
        TUMOR_SIZE_CLIN("size_clin"),
        TUMOR_SIZE_PATH("size_path"),
        TUMOR_SIZE_SUMMARY("size_summary"),
        RADIATION_SURG_SEQ("radiation_surg_seq"),
        SYSTEMIC_SURG_SEQ("systemic_surg_seq"),
        SS_2018("ss2018");

        private String _name;

        EodInput(String name) {
            _name = name;
        }

        @Override
        public String toString() {
            return _name;
        }
    }

    // output key definitions
    public enum EodOutput {
        NAACCR_SCHEMA_ID("naaccr_schema_id"),
        AJCC_ID("ajcc_id"),
        DERIVED_VERSION("derived_version"),
        EOD_2018_T("eod_2018_t"),
        EOD_2018_N("eod_2018_n"),
        EOD_2018_M("eod_2018_m"),
        EOD_2018_STAGE_GROUP("eod_2018_stage_group"),
        SS_2018_DERIVED("ss2018_derived");

        private String _name;

        EodOutput(String name) {
            _name = name;
        }

        @Override
        public String toString() {
            return _name;
        }
    }

    /**
     * TnmStagingInputBuilder builder
     */
    public static class EodStagingInputBuilder {

        private EodStagingData _data;

        public EodStagingInputBuilder() {
            _data = new EodStagingData();
        }

        public EodStagingInputBuilder withDisciminator1(String discriminator1) {
            _data.setInput(EodInput.DISCRIMINATOR_1, discriminator1);
            return this;
        }

        public EodStagingInputBuilder withDisciminator2(String discriminator2) {
            _data.setInput(EodInput.DISCRIMINATOR_2, discriminator2);
            return this;
        }

        public EodStagingInputBuilder withInput(EodInput key, String value) {
            _data.setInput(key, value);
            return this;
        }

        public EodStagingData build() {
            return _data;
        }
    }
}

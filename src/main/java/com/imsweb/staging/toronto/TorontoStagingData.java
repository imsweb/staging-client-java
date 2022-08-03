/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.toronto;

import com.imsweb.staging.entities.StagingData;

public class TorontoStagingData extends StagingData {

    /**
     * Default constructor
     */
    public TorontoStagingData() {
        super();
    }

    /**
     * Construct with site and histology
     * @param site primary site
     * @param hist histology
     */
    public TorontoStagingData(String site, String hist) {
        super(site, hist);
    }

    /**
     * Construct with site, histology and age at diagnosis
     * @param site primary site
     * @param hist histology
     * @param ageAtDx age at diagnosis
     */
    public TorontoStagingData(String site, String hist, String ageAtDx) {
        super(site, hist);

        setInput(TorontoInput.AGE_DX, ageAtDx);
    }

    /**
     * Return the specified input value
     * @param key input key
     * @return input
     */
    public String getInput(TorontoInput key) {
        return getInput(key.toString());
    }

    /**
     * Set the specified input value
     * @param key input key
     * @param value value
     */
    public void setInput(TorontoInput key, String value) {
        setInput(key.toString(), value);
    }

    /**
     * Return the specified output value
     * @param key output key
     * @return output
     */
    public String getOutput(TorontoOutput key) {
        return getOutput(key.toString());
    }

    // input key definitions; note this only includes keys that are required for staging
    public enum TorontoInput {
        PRIMARY_SITE("site"),
        HISTOLOGY("hist"),
        BEHAVIOR("behavior"),
        AGE_DX("age_dx"),
        YEAR_DX("year_dx"),
        EOD_PRIMARY_TUMOR("eod_primary_tumor"),
        EOD_REGIONAL_NODES("eod_regional_nodes"),
        EOD_METS("eod_mets"),
        NODES_POS("nodes_pos"),
        GRADE_PATH("grade_path"),
        GRADE_CLIN("grade_clin"),
        GRADE_POST_THERAPY_CLIN("grade_post_therapy_clin"),
        GRADE_POST_THERAPY_PATH("grade_post_therapy_path"),
        SCHEMA_ID("schema_id"),
        SHIMADA_CLASSIFICATION("shimada_classification"),
        S_CATEGORY_CLIN("s_category_clin"),
        DNA_PLOIDY("dna_ploidy"),
        N_MYC_APMLIFICATION("n_myc_amplification"),
        S_CATEGORY_PATH("s_category_path"),
        SIZE_SUMMARY("size_summary"),
        INGRSS("ingrss"),
        B_SYMPTOMS("b_symptoms");

        private final String _name;

        TorontoInput(String name) {
            _name = name;
        }

        @Override
        public String toString() {
            return _name;
        }
    }

    // output key definitions
    public enum TorontoOutput {
        TORONTO_ID("toronto_id"),
        TORONTO_VERSION_NUMBER("toronto_version_number"),
        TORONTO_T("toronto_t"),
        TORONTO_N("toronto_n"),
        TORONTO_M("toronto_m"),
        TORONTO_GROUP("toronto_group"),
        DERIVED_VERSION("derived_version"),
        DERIVED_RISK_LEVEL("derived_risk_level"),
        DERIVED_ANN_ARBOR_STAGE("derived_ann_arbor_stage");

        private final String _name;

        TorontoOutput(String name) {
            _name = name;
        }

        @Override
        public String toString() {
            return _name;
        }
    }

    /**
     * TorontoStagingInputBuilder builder
     */
    public static class TorontoStagingInputBuilder {

        private final TorontoStagingData _data;

        public TorontoStagingInputBuilder() {
            _data = new TorontoStagingData();
        }

        public TorontoStagingInputBuilder withInput(TorontoInput key, String value) {
            _data.setInput(key, value);
            return this;
        }

        public TorontoStagingData build() {
            return _data;
        }
    }
}

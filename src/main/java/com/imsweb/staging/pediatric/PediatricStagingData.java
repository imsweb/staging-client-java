/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.pediatric;

import com.imsweb.staging.entities.StagingData;

public class PediatricStagingData extends StagingData {

    /**
     * Default constructor
     */
    public PediatricStagingData() {
        super();
    }

    /**
     * Construct with site and histology
     * @param site primary site
     * @param hist histology
     */
    public PediatricStagingData(String site, String hist) {
        super(site, hist);
    }

    /**
     * Construct with site, histology and age at diagnosis
     * @param site primary site
     * @param hist histology
     * @param ageAtDx age at diagnosis
     */
    public PediatricStagingData(String site, String hist, String ageAtDx) {
        super(site, hist);

        setInput(PediatricInput.AGE_DX, ageAtDx);
    }

    /**
     * Return the specified input value
     * @param key input key
     * @return input
     */
    public String getInput(PediatricInput key) {
        return getInput(key.toString());
    }

    /**
     * Set the specified input value
     * @param key input key
     * @param value value
     */
    public void setInput(PediatricInput key, String value) {
        setInput(key.toString(), value);
    }

    /**
     * Return the specified output value
     * @param key output key
     * @return output
     */
    public String getOutput(PediatricOutput key) {
        return getOutput(key.toString());
    }

    // input key definitions; note this only includes keys that are required for staging
    public enum PediatricInput {
        PRIMARY_SITE("site"),
        HISTOLOGY("hist"),
        BEHAVIOR("behavior"),
        AGE_DX("age_dx"),
        YEAR_DX("year_dx"),
        PED_PRIMARY_TUMOR("ped_primary_tumor"),
        PED_REGIONAL_NODES("ped_regional_nodes"),
        PED_METS("ped_mets"),
        NODES_POS("nodes_pos"),
        S_CATEGORY_CLIN("s_category_clin"),
        S_CATEGORY_PATH("s_category_path"),
        SIZE_SUMMARY("size_summary"),
        B_SYMPTOMS("b_symptoms"),
        DERIVED_SUMMARY_GRADE("derived_summary_grade"),
        SURG_2023("surg_2023"),
        SURGICAL_MARGINS("surgical_margins"),
        INGRESS("inrgss"),
        PRETEXT_CLINICAL_STAGING("pretext_clinical_staging");

        private final String _name;

        PediatricInput(String name) {
            _name = name;
        }

        @Override
        public String toString() {
            return _name;
        }
    }

    // output key definitions
    public enum PediatricOutput {
        PEDIATRIC_ID("pediatric_id"),
        TORONTO_VERSION_NUMBER("toronto_version_number"),
        PEDIATRIC_T("pediatric_t"),
        PEDIATRIC_N("pediatric_n"),
        PEDIATRIC_M("pediatric_m"),
        PEDIATRIC_GROUP("pediatric_group"),
        TORONTO_T("toronto_t"),
        TORONTO_N("toronto_n"),
        TORONTO_M("toronto_m"),
        TORONTO_STAGE_GROUP("toronto_stage_group"),
        DERIVED_VERSION("derived_version");

        private final String _name;

        PediatricOutput(String name) {
            _name = name;
        }

        @Override
        public String toString() {
            return _name;
        }
    }

    /**
     * PediatricStagingInputBuilder builder
     */
    public static class PediatricStagingInputBuilder {

        private final PediatricStagingData _data;

        public PediatricStagingInputBuilder() {
            _data = new PediatricStagingData();
        }

        public PediatricStagingInputBuilder withInput(PediatricInput key, String value) {
            _data.setInput(key, value);
            return this;
        }

        public PediatricStagingData build() {
            return _data;
        }
    }
}

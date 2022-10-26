/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.eod;

import com.imsweb.staging.entities.StagingData;

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

    // input key definitions; note this only includes keys that are required for staging
    public enum EodInput {
        PRIMARY_SITE("site"),
        HISTOLOGY("hist"),
        BEHAVIOR("behavior"),
        SEX("sex"),
        AGE_AT_DX("age_dx"),
        DISCRIMINATOR_1("discriminator_1"),
        DISCRIMINATOR_2("discriminator_2"),
        NODES_POS("nodes_pos"),
        EOD_PRIMARY_TUMOR("eod_primary_tumor"),
        EOD_REGIONAL_NODES("eod_regional_nodes"),
        EOD_METS("eod_mets"),
        GRADE_CLIN("grade_clin"),
        GRADE_PATH("grade_path"),
        GRADE_POST_THERAPY_CLIN("grade_post_therapy_clin"),
        GRADE_POST_THERAPY_PATH("grade_post_therapy_path"),
        DX_YEAR("year_dx"),
        TUMOR_SIZE_SUMMARY("size_summary"),
        RADIATION_SURG_SEQ("radiation_surg_seq"),
        SYSTEMIC_SURG_SEQ("systemic_surg_seq"),
        BRESLOW_THINKNESS("breslow_thickness"),
        EOD_PROSTATE_PATH_EXTENSION("eod_prostate_path_extension"),
        ER("er"),
        ESOPH_TUMOR_EPICENTER("esoph_tumor_epicenter"),
        GESTATIONAL_PROG_INDEX("gestational_prog_index"),
        HER2_SUMMARY("her2_summary"),
        LDH_LEVEL("ldh_level"),
        LN_POS_AXILLARY_LEVEL_1_2("ln_pos_axillary_level_1_2"),
        LN_SIZE_OF_METS("ln_size_of_mets"),
        MEASURED_BASAL_DIAMETER("measured_basal_diameter"),
        MEASURED_THICKNESS("measured_thickness"),
        ONCOTYPE_DX_SCORE("oncotype_dx_score"),
        PERIPHERAL_BLOOD_INVOLV("peripheral_blood_involv"),
        PERITONEAL_CYTOLOGY("peritoneal_cytology"),
        PR("pr"),
        PSA("psa"),
        S_CATEGORY_CLIN("s_category_clin"),
        S_CATEGORY_PATH("s_category_path"),
        ULCERATION("ulceration"),
        THROMBOCYTOPENIA("thrombocytopenia"),
        ORGANOMEGALY("organomegaly"),
        ADENOPATHY("adenopathy"),
        ANEMIA("anemia"),
        LYMPHOCYTOSIS("lymphocytosis");

        private final String _name;

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
        AJCC_VERSION_NUMBER("ajcc_version_number"),
        DERIVED_VERSION("derived_version"),
        EOD_2018_T("eod_2018_t"),
        EOD_2018_N("eod_2018_n"),
        EOD_2018_M("eod_2018_m"),
        EOD_2018_STAGE_GROUP("eod_2018_stage_group"),
        SS_2018_DERIVED("ss2018_derived"),
        DERIVED_RAI_STAGE("derived_rai_stage");

        private final String _name;

        EodOutput(String name) {
            _name = name;
        }

        @Override
        public String toString() {
            return _name;
        }
    }

    /**
     * EodStagingInputBuilder builder
     */
    public static class EodStagingInputBuilder {

        private final EodStagingData _data;

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

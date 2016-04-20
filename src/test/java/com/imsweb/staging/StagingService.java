/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.staging;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

import com.imsweb.staging.entities.StagingAlgorithm;
import com.imsweb.staging.entities.StagingSchema;
import com.imsweb.staging.entities.StagingTable;
import com.imsweb.staging.entities.StagingVersion;

public interface StagingService {

    /**
     * Return a list of all supported staging algorithms
     * @return a list of StagingAlgorithm objects
     */
    @GET("staging/algorithms")
    Call<List<StagingAlgorithm>> getAlgorithms();

    /**
     * Return a list of supported versions for the passed algorithm
     * @param algorithm an algorithm identifier
     * @return list of staging versions
     */
    @GET("staging/{algorithm}/versions")
    Call<List<StagingVersion>> getVersions(@Path("algorithm") String algorithm);

    /**
     * Returns a version
     * @param algorithm an algorithm identifier
     * @param version a version
     * @return list of staging versions
     */
    @GET("staging/{algorithm}/{version}")
    Call<StagingVersion> getVersion(@Path("algorithm") String algorithm, @Path("version") String version);

    /**
     * Return a list of matching schemas
     * @param algorithm an algorithm identifier
     * @param version a version
     * @return a list of schemas
     */
    @GET("staging/{algorithm}/{version}/schemas")
    Call<List<StagingSchema>> getSchemas(@Path("algorithm") String algorithm, @Path("version") String version);

    /**
     * Return a list of matching schemas
     * @param algorithm an algorithm identifier
     * @param version a version
     * @param query an optional text query
     * @return a list of schemas
     */
    @GET("staging/{algorithm}/{version}/schemas")
    Call<List<StagingSchema>> getSchemas(@Path("algorithm") String algorithm, @Path("version") String version, @Query("q") String query);

    /**
     * Perform a schema lookup
     * @param algorithm an algorithm identifier
     * @param version a version
     * @param data a map of key/value pairs containing the input for the lookup
     * @return list of schema information objects
     */
    @POST("staging/{algorithm}/{version}/schemas/lookup")
    Call<List<StagingSchema>> lookupSchema(@Path("algorithm") String algorithm, @Path("version") String version, @Body Map<String, String> data);

    /**
     * Return a single schema definition by schema identifier
     * @param algorithm an algorithm identifier
     * @param version a version
     * @param id a schema identifier
     * @return a schema object
     */
    @GET("staging/{algorithm}/{version}/schema/{id}")
    Call<StagingSchema> getSchema(@Path("algorithm") String algorithm, @Path("version") String version, @Path("id") String id);

    /**
     * Return a list of tables which are involved in the specified schema
     * @param algorithm an algorithm identifier
     * @param version a version
     * @param schemaId a schema identifier
     */
    @GET("staging/{algorithm}/{version}/schema/{id}/tables")
    Call<List<StagingTable>> getInvolvedTables(@Path("algorithm") String algorithm, @Path("version") String version, @Path("id") String schemaId);

    /**
     * Return a list of matching tables
     * @param algorithm an algorithm identifier
     * @param version a version
     * @return list of staging tables
     */
    @GET("staging/{algorithm}/{version}/tables")
    Call<List<StagingTable>> getTables(@Path("algorithm") String algorithm, @Path("version") String version);

    /**
     * Return a list of matching tables
     * @param algorithm an algorithm identifier
     * @param version a version
     * @param query an optional text query
     * @return list of staging tables
     */
    @GET("staging/{algorithm}/{version}/tables")
    Call<List<StagingTable>> getTables(@Path("algorithm") String algorithm, @Path("version") String version, @Query("q") String query);

    /**
     * Return a list of matching tables
     * @param algorithm an algorithm identifier
     * @param version a version
     * @param unusedOnly if true, return only unused tables
     * @return list of staging tables
     */
    @GET("staging/{algorithm}/{version}/tables")
    Call<List<StagingTable>> getTables(@Path("algorithm") String algorithm, @Path("version") String version, @Query("unused") Boolean unusedOnly);

    /**
     * Return a single table definition by table identifier
     * @param algorithm an algorithm identifier
     * @param version a version
     * @param id a table identifier
     * @return a staging table
     */
    @GET("staging/{algorithm}/{version}/table/{id}")
    Call<StagingTable> getTable(@Path("algorithm") String algorithm, @Path("version") String version, @Path("id") String id);

    /**
     * Return a list of schemas which the specified table is involved in
     * @param algorithm an algorithm identifier
     * @param version a version
     * @param tableId a table identifier
     */
    @GET("staging/{algorithm}/{version}/table/{id}/schemas")
    Call<List<StagingSchema>> getInvolvedSchemas(@Path("algorithm") String algorithm, @Path("version") String version, @Path("id") String tableId);

    /**
     * Stage the passed input
     * @param algorithm an algorithm identifier
     * @param version a version
     * @param input a map of key/value pairs containing the input for the staging call
     * @return a staging data object
     */
    @POST("staging/{algorithm}/{version}/stage")
    Call<StagingData> stage(@Path("algorithm") String algorithm, @Path("version") String version, @Body Map<String, String> input);
}

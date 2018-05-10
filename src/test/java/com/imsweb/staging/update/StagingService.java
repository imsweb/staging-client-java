/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.staging.update;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

import com.imsweb.staging.entities.StagingSchema;
import com.imsweb.staging.entities.StagingTable;

interface StagingService {

    /**
     * Return a list of matching schemas
     * @param algorithm an algorithm identifier
     * @param version a version
     * @return a list of schemas
     */
    @GET("staging/{algorithm}/{version}/schemas")
    Call<List<StagingSchema>> getSchemas(@Path("algorithm") String algorithm, @Path("version") String version);

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

}

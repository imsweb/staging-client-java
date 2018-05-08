/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.tnm;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.imsweb.staging.StagingFileDataProvider;

public final class TnmDataProvider extends StagingFileDataProvider {

    private static final Map<TnmVersion, TnmDataProvider> _PROVIDERS = new ConcurrentHashMap<>();

    public enum TnmVersion {
        LATEST("1.8"),
        v1_8("1.8");

        private String _version;

        TnmVersion(String version) {
            _version = version;
        }

        public String getVersion() {
            return _version;
        }
    }

    /**
     * Construct a TNM data provider with the passed version
     * @param version version to initialize
     */
    private TnmDataProvider(TnmVersion version) {
        super("tnm", version.getVersion());
    }

    /**
     * Return the TNM provider for the latest version
     * @return the data provider
     */
    public static synchronized TnmDataProvider getInstance() {
        return getInstance(TnmVersion.LATEST);
    }

    /**
     * Return the TNM provider for a specified version
     * @param version TNM version
     * @return the data provider
     */
    public static synchronized TnmDataProvider getInstance(TnmVersion version) {
        TnmDataProvider provider = _PROVIDERS.get(version);

        if (provider == null) {
            provider = new TnmDataProvider(version);
            _PROVIDERS.put(version, provider);
        }

        return provider;
    }
}

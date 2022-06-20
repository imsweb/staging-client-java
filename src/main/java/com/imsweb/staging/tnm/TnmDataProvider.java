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
        LATEST("1.9"),
        V1_9("1.9");

        private final String _version;

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
        return _PROVIDERS.computeIfAbsent(version, k -> new TnmDataProvider(version));
    }

}

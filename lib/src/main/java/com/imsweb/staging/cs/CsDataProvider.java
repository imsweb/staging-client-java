/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.cs;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.imsweb.staging.StagingFileDataProvider;

public final class CsDataProvider extends StagingFileDataProvider {

    private static final Map<CsVersion, CsDataProvider> _PROVIDERS = new ConcurrentHashMap<>();

    public enum CsVersion {
        LATEST("02.05.50"),
        V020550("02.05.50");

        private final String _version;

        CsVersion(String version) {
            _version = version;
        }

        public String getVersion() {
            return _version;
        }
    }

    /**
     * Construct a CS data provider with the passed version
     * @param version version to initialize
     */
    private CsDataProvider(CsVersion version) {
        super("cs", version.getVersion());
    }

    /**
     * Return the CS provider for the latest version
     * @return the data provider
     */
    public static synchronized CsDataProvider getInstance() {
        return getInstance(CsVersion.LATEST);
    }

    /**
     * Return the CS provider for a specified version
     * @param version CS version
     * @return the data provider
     */
    public static synchronized CsDataProvider getInstance(CsVersion version) {
        return _PROVIDERS.computeIfAbsent(version, k -> new CsDataProvider(version));
    }
}

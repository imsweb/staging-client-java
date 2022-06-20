/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.eod;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.imsweb.staging.StagingFileDataProvider;

public final class EodDataProvider extends StagingFileDataProvider {

    private static final Map<EodVersion, EodDataProvider> _PROVIDERS = new ConcurrentHashMap<>();

    /**
     * Construct a EOD data provider with the passed version
     * @param version version to initialize
     */
    private EodDataProvider(EodVersion version) {
        super("eod_public", version.getVersion());
    }

    /**
     * Return the EOD provider for the latest version
     * @return the data provider
     */
    public static synchronized EodDataProvider getInstance() {
        return getInstance(EodVersion.LATEST);
    }

    /**
     * Return the EOD provider for a specified version
     * @param version EOD version
     * @return the data provider
     */
    public static synchronized EodDataProvider getInstance(EodVersion version) {
        return _PROVIDERS.computeIfAbsent(version, k -> new EodDataProvider(version));
    }

    public enum EodVersion {
        LATEST("2.1"),
        V2_1("2.1");

        private final String _version;

        EodVersion(String version) {
            _version = version;
        }

        public String getVersion() {
            return _version;
        }
    }
}

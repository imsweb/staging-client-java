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
        EodDataProvider provider = _PROVIDERS.get(version);

        if (provider == null) {
            provider = new EodDataProvider(version);
            _PROVIDERS.put(version, provider);
        }

        return provider;
    }

    public enum EodVersion {
        LATEST("1.2"),
        v1_2("1.2");

        private String _version;

        EodVersion(String version) {
            _version = version;
        }

        public String getVersion() {
            return _version;
        }
    }
}

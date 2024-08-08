/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.pediatric;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.imsweb.staging.StagingFileDataProvider;

public final class PediatricDataProvider extends StagingFileDataProvider {

    private static final Map<PediatricVersion, PediatricDataProvider> _PROVIDERS = new ConcurrentHashMap<>();

    /**
     * Construct a pediatric data provider with the passed version
     * @param version version to initialize
     */
    private PediatricDataProvider(PediatricVersion version) {
        super("pediatric", version.getVersion());
    }

    /**
     * Return the pediatric provider for the latest version
     * @return the data provider
     */
    public static synchronized PediatricDataProvider getInstance() {
        return getInstance(PediatricVersion.LATEST);
    }

    /**
     * Return the pediatric provider for a specified version
     * @param version pediatric version
     * @return the data provider
     */
    public static synchronized PediatricDataProvider getInstance(PediatricVersion version) {
        return _PROVIDERS.computeIfAbsent(version, k -> new PediatricDataProvider(version));
    }

    public enum PediatricVersion {
        LATEST("1.2"),
        V1_2("1.2");

        private final String _version;

        PediatricVersion(String version) {
            _version = version;
        }

        public String getVersion() {
            return _version;
        }
    }
}

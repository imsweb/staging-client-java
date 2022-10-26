/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.toronto;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.imsweb.staging.StagingFileDataProvider;

public final class TorontoDataProvider extends StagingFileDataProvider {

    private static final Map<TorontoVersion, TorontoDataProvider> _PROVIDERS = new ConcurrentHashMap<>();

    /**
     * Construct a Toronto data provider with the passed version
     * @param version version to initialize
     */
    private TorontoDataProvider(TorontoVersion version) {
        super("toronto", version.getVersion());
    }

    /**
     * Return the Toronto provider for the latest version
     * @return the data provider
     */
    public static synchronized TorontoDataProvider getInstance() {
        return getInstance(TorontoVersion.LATEST);
    }

    /**
     * Return the Toronto provider for a specified version
     * @param version Toronto version
     * @return the data provider
     */
    public static synchronized TorontoDataProvider getInstance(TorontoVersion version) {
        return _PROVIDERS.computeIfAbsent(version, k -> new TorontoDataProvider(version));
    }

    public enum TorontoVersion {
        LATEST("0.3"),
        V0_3("0.3");

        private final String _version;

        TorontoVersion(String version) {
            _version = version;
        }

        public String getVersion() {
            return _version;
        }
    }
}

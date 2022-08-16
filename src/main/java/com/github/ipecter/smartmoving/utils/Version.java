package com.github.ipecter.smartmoving.utils;

import java.util.regex.Pattern;

public class Version implements Comparable<Version> {
    private static final Pattern validVersion = Pattern.compile("^[0-9]+(\\.[0-9]+)+$");
    private static final Pattern versionSeparator = Pattern.compile("\\.");
    private final String version;

    public Version(String version) {
        if (!validVersion.matcher(version).matches())
            throw new IllegalArgumentException("'" + version + "' is not a valid version");

        this.version = version;
    }

    public final String getVersion() {
        return version;
    }

    @Override
    public int compareTo(Version version) {
        String[] versionNodes = versionSeparator.split(getVersion());
        String[] greaterNodes = versionSeparator.split(version.getVersion());

        int length = Math.max(versionNodes.length, greaterNodes.length);

        for (int i = 0; i < length; ++i) {
            int versionNode = i < versionNodes.length ? Integer.parseInt(versionNodes[i]) : 0;
            int greaterNode = i < greaterNodes.length ? Integer.parseInt(greaterNodes[i]) : 0;

            if (versionNode < greaterNode)
                return -1;
            else if (versionNode != greaterNode)
                return 1;
        }

        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Version)) return false;

        Version that = (Version) o;
        return compareTo(that) == 0;
    }

    @Override
    public String toString() {
        return version;
    }
}
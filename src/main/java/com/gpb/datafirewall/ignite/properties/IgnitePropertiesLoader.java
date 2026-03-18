package com.gpb.datafirewall.ignite.properties;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class IgnitePropertiesLoader {

    private IgnitePropertiesLoader() {
    }

    public static Properties load(Path path) {
        try (InputStream in = Files.newInputStream(path)) {
            Properties props = new Properties();
            props.load(in);
            return props;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config file: " + path, e);
        }
    }
}
package com.gpb.datafirewall.ignite.config;

import org.apache.ignite.configuration.ClientConnectorConfiguration;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public final class IgniteNodeConfig {

    private IgniteNodeConfig() {
    }

    public static IgniteConfiguration createServerConfig(String instanceName, Properties props) {
        String localHost = getRequired(props, "ignite.local.host");
        String discoveryRaw = getRequired(props, "ignite.discovery.addresses");

        List<String> discoveryAddresses = Arrays.stream(discoveryRaw.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());

        if (discoveryAddresses.isEmpty()) {
            throw new IllegalStateException("ignite.discovery.addresses is empty");
        }

        IgniteConfiguration cfg = new IgniteConfiguration();

        cfg.setIgniteInstanceName(instanceName);
        cfg.setConsistentId(instanceName);
        cfg.setClientMode(false);
        cfg.setLocalHost(localHost);

        // Discovery
        TcpDiscoveryVmIpFinder ipFinder = new TcpDiscoveryVmIpFinder();
        ipFinder.setAddresses(discoveryAddresses);

        TcpDiscoverySpi discoverySpi = new TcpDiscoverySpi();
        discoverySpi.setIpFinder(ipFinder);
        discoverySpi.setLocalAddress(localHost);
        discoverySpi.setLocalPort(47500);
        discoverySpi.setLocalPortRange(20);
        cfg.setDiscoverySpi(discoverySpi);

        // Communication
        TcpCommunicationSpi communicationSpi = new TcpCommunicationSpi();
        communicationSpi.setLocalAddress(localHost);
        communicationSpi.setLocalPort(47100);
        communicationSpi.setLocalPortRange(20);
        cfg.setCommunicationSpi(communicationSpi);

        // Thin client
        ClientConnectorConfiguration clientCfg = new ClientConnectorConfiguration();
        clientCfg.setHost(localHost);
        clientCfg.setPort(10800);
        clientCfg.setPortRange(100);
        cfg.setClientConnectorConfiguration(clientCfg);

        // Persistence
        boolean persistenceEnabled = getBoolean(props, "ignite.persistence.enabled", false);
        if (persistenceEnabled) {
            String storagePath = getRequired(props, "ignite.persistence.storage.path");
            String walPath = getRequired(props, "ignite.persistence.wal.path");
            String walArchivePath = getRequired(props, "ignite.persistence.wal.archive.path");

            DataRegionConfiguration defaultRegion = new DataRegionConfiguration()
                .setName("Default_Region")
                .setInitialSize(256L * 1024 * 1024) // 256 MB
                .setMaxSize(512L * 1024 * 1024)     // 512 MB
                .setPersistenceEnabled(true);

            DataStorageConfiguration storageCfg = new DataStorageConfiguration();
            storageCfg.setStoragePath(storagePath);
            storageCfg.setWalPath(walPath);
            storageCfg.setWalArchivePath(walArchivePath);
            storageCfg.setDefaultDataRegionConfiguration(defaultRegion);

            cfg.setDataStorageConfiguration(storageCfg);
        }

        logStartup(localHost, discoveryAddresses, persistenceEnabled, props);

        return cfg;
    }

    private static String getRequired(Properties props, String key) {
        // 1. сначала system property
        String value = System.getProperty(key);

        // 2. потом файл
        if (value == null || value.isBlank()) {
            value = props.getProperty(key);
        }

        // 3. если нет — ошибка
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Required property not set: " + key +
                    " (neither -D" + key + " nor config file)"
            );
        }

        return value.trim();
    }

    private static boolean getBoolean(Properties props, String key, boolean defaultValue) {
        String value = System.getProperty(key);

        if (value == null || value.isBlank()) {
            value = props.getProperty(key);
        }

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return Boolean.parseBoolean(value.trim());
    }

    private static void logStartup(
            String localHost,
            List<String> discovery,
            boolean persistenceEnabled,
            Properties props
    ) {
        System.out.println("=== Ignite configuration ===");
        System.out.println("localHost = " + localHost);
        System.out.println("discovery = " + discovery);
        System.out.println("persistence.enabled = " + persistenceEnabled);

        if (persistenceEnabled) {
            System.out.println("storagePath = " + props.getProperty("ignite.persistence.storage.path"));
            System.out.println("walPath = " + props.getProperty("ignite.persistence.wal.path"));
            System.out.println("walArchivePath = " + props.getProperty("ignite.persistence.wal.archive.path"));
        }

        System.out.println("============================");
    }
}

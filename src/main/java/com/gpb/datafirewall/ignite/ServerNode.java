package com.gpb.datafirewall.ignite;

import java.nio.file.Path;
import java.util.Properties;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.cluster.ClusterState;

import com.gpb.datafirewall.ignite.config.IgniteNodeConfig;
import com.gpb.datafirewall.ignite.properties.IgnitePropertiesLoader;

public class ServerNode {

    public static void main(String[] args) {
        String configPath = System.getProperty("ignite.config.file");
        if (configPath == null || configPath.isBlank()) {
            throw new IllegalStateException(
                    "Required JVM parameter not set: -Dignite.config.file=/path/to/ignite-server.properties"
            );
        }

        Properties props = IgnitePropertiesLoader.load(Path.of(configPath));

        String instanceName = System.getProperty("ignite.instance.name", "ignite-server");

        Ignite ignite = Ignition.start(
                IgniteNodeConfig.createServerConfig(instanceName, props)
        );

        ignite.cluster().state(ClusterState.ACTIVE);

        Runtime.getRuntime().addShutdownHook(new Thread(() ->
                Ignition.stop(ignite.name(), true)
        ));

        System.out.println("Server started: " + ignite.cluster().localNode().id());
        System.out.println("Cluster size: " + ignite.cluster().nodes().size());
    }
}

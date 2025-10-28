package com.youtube.mvp.search.infrastructure.config;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.CosmosDBEmulatorContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Test configuration with Testcontainers for integration tests.
 */
@TestConfiguration
public class TestContainersConfiguration {
    
    private static final CosmosDBEmulatorContainer cosmosContainer = new CosmosDBEmulatorContainer(
            DockerImageName.parse("mcr.microsoft.com/cosmosdb/linux/azure-cosmos-emulator:latest")
    )
            .withExposedPorts(8081);
    
    static {
        cosmosContainer.start();
        Runtime.getRuntime().addShutdownHook(new Thread(cosmosContainer::stop));
    }
    
    @Bean
    @Primary
    public CosmosClient cosmosClient() {
        return new CosmosClientBuilder()
                .endpoint("https://" + cosmosContainer.getEmulatorEndpoint())
                .key(cosmosContainer.getEmulatorKey())
                .buildClient();
    }
}

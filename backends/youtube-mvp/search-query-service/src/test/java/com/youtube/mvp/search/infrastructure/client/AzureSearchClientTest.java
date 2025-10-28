package com.youtube.mvp.search.infrastructure.client;

import com.azure.core.http.rest.SimpleResponse;
import com.azure.search.documents.models.SearchResults;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "azure.search.endpoint=http://localhost:8080",
        "azure.search.api-key=test-key",
        "azure.search.index-name=test-index"
})
class AzureSearchClientTest {
    
    private WireMockServer wireMockServer;
    
    @Autowired
    private AzureSearchClient azureSearchClient;
    
    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(8080);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8080);
    }
    
    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }
    
    @Test
    void search_ShouldReturnResults() {
        // Given
        String searchQuery = "test query";
        
        stubFor(get(urlPathEqualTo("/docs/test-index/docs/search"))
                .withQueryParam("api-version", equalTo("2023-11-01"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"value\":[{\"documentId\":\"doc1\",\"title\":\"Test\"}],\"@odata.count\":1}")));
        
        // When
        SearchResults<Map> results = azureSearchClient.search(searchQuery, null);
        
        // Then
        assertThat(results).isNotNull();
        
        wireMockServer.verify(getRequestedFor(urlPathEqualTo("/docs/test-index/docs/search")));
    }
    
    @Test
    void uploadDocuments_ShouldPostToIndex() {
        // Given
        Map<String, Object> doc = new HashMap<>();
        doc.put("documentId", "doc1");
        doc.put("title", "Test");
        
        stubFor(post(urlPathEqualTo("/docs/test-index/docs/index"))
                .withQueryParam("api-version", equalTo("2023-11-01"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"value\":[{\"key\":\"doc1\",\"status\":true}]}")));
        
        // When
        azureSearchClient.uploadDocuments(List.of(doc));
        
        // Then
        wireMockServer.verify(postRequestedFor(urlPathEqualTo("/docs/test-index/docs/index")));
    }
}

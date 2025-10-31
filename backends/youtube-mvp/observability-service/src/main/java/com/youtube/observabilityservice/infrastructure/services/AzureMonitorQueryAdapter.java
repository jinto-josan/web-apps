package com.youtube.observabilityservice.infrastructure.services;

import com.azure.monitor.query.LogsQueryClient;
import com.azure.monitor.query.models.LogsQueryOptions;
import com.azure.monitor.query.models.LogsTable;
import com.azure.monitor.query.models.LogsTableRow;
import com.youtube.observabilityservice.domain.entities.SLI;
import com.youtube.observabilityservice.domain.services.AzureMonitorQueryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AzureMonitorQueryAdapter implements AzureMonitorQueryPort {
    
    private final LogsQueryClient logsQueryClient;
    
    @Value("${azure.monitor.workspace-id:}")
    private String workspaceId;
    
    @Override
    public Double executeQuery(String kqlQuery) {
        try {
            LogsTable table = logsQueryClient.queryWorkspace(
                    workspaceId,
                    kqlQuery,
                    new LogsQueryOptions().setServerTimeout(Duration.ofSeconds(30))
            );
            
            if (table == null || table.getRows().isEmpty()) {
                log.warn("Query returned no results: {}", kqlQuery);
                return null;
            }
            
            // Extract first numeric value from first row, first column
            LogsTableRow firstRow = table.getRows().get(0);
            if (firstRow.getRow().isEmpty()) {
                return null;
            }
            
            Object value = firstRow.getRow().get(0).getValue();
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            } else if (value instanceof String) {
                try {
                    return Double.parseDouble((String) value);
                } catch (NumberFormatException e) {
                    log.warn("Could not parse query result as number: {}", value);
                    return null;
                }
            }
            
            return null;
        } catch (Exception e) {
            log.error("Error executing Azure Monitor query: {}", kqlQuery, e);
            return null;
        }
    }
    
    @Override
    public Double calculateSLI(SLI sli) {
        return executeQuery(sli.getQuery());
    }
}


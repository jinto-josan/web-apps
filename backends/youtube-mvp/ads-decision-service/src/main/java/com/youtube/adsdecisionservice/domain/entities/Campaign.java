package com.youtube.adsdecisionservice.domain.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Campaign {
    private String id;
    private String name;
    private BigDecimal dailyBudget;
    private BigDecimal spentToday;
    private Instant startTime;
    private Instant endTime;
    private List<Creative> creatives;
}



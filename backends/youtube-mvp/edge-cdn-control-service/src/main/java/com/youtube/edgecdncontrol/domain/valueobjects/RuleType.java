package com.youtube.edgecdncontrol.domain.valueobjects;

public enum RuleType {
    ROUTING_RULE,
    WAF_POLICY,
    ORIGIN_FAILOVER,
    CACHE_RULE,
    COMPRESSION_RULE,
    HEADER_MODIFICATION,
    URL_REWRITE,
    RATE_LIMIT
}


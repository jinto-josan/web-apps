package com.youtube.edgecdncontrol.domain.valueobjects;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class RuleMatchCondition {
    MatchType matchType;
    String variable;
    String operator;
    List<String> values;
    boolean caseSensitive;

    public enum MatchType {
        REQUEST_URI,
        REQUEST_METHOD,
        REQUEST_HEADER,
        QUERY_STRING,
        REQUEST_SCHEME,
        REMOTE_ADDRESS,
        POST_ARGS,
        COOKIE,
        REQUEST_BODY
    }
}


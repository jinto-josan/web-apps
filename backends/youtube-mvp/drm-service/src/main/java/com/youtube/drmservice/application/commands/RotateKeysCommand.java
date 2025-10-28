package com.youtube.drmservice.application.commands;

import lombok.Builder;
import lombok.Value;
import java.util.List;

@Value
@Builder
public class RotateKeysCommand {
    List<String> policyIds;
    String rotatedBy;
}


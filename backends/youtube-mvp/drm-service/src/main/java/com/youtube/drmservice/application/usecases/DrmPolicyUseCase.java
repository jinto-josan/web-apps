package com.youtube.drmservice.application.usecases;

import com.youtube.drmservice.application.commands.CreateDrmPolicyCommand;
import com.youtube.drmservice.application.commands.UpdateDrmPolicyCommand;
import com.youtube.drmservice.application.commands.RotateKeysCommand;
import com.youtube.drmservice.application.queries.GetDrmPolicyQuery;
import com.youtube.drmservice.application.queries.GetDrmPolicyByVideoIdQuery;
import com.youtube.drmservice.domain.models.DrmPolicy;

public interface DrmPolicyUseCase {
    DrmPolicy createPolicy(CreateDrmPolicyCommand command);
    DrmPolicy updatePolicy(UpdateDrmPolicyCommand command);
    void rotateKeys(RotateKeysCommand command);
    DrmPolicy getPolicy(GetDrmPolicyQuery query);
    DrmPolicy getPolicyByVideoId(GetDrmPolicyByVideoIdQuery query);
}


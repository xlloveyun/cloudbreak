package com.sequenceiq.it.cloudbreak.newway.assertion;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sequenceiq.cloudbreak.api.model.ResourceStatus;
import com.sequenceiq.cloudbreak.api.model.template.ClusterTemplateResponse;
import com.sequenceiq.it.cloudbreak.newway.CloudbreakClient;
import com.sequenceiq.it.cloudbreak.newway.context.TestContext;
import com.sequenceiq.it.cloudbreak.newway.entity.ClusterTemplateEntity;

public class CheckClusterTemplateFirstResponse implements AssertionV2<ClusterTemplateEntity> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckClusterTemplateFirstResponse.class);

    @Override
    public ClusterTemplateEntity doAssertion(TestContext testContext, ClusterTemplateEntity entity, CloudbreakClient client) throws Exception {
        Optional<ClusterTemplateResponse> first = entity.getResponses().stream().findFirst();
        if (!first.isPresent()) {
            throw new IllegalArgumentException("No element in the result");
        }

        ClusterTemplateResponse clusterTemplateResponse = first.get();

        if (clusterTemplateResponse.getStackTemplate() == null) {
            throw new IllegalArgumentException("STack template is empty");
        }

        if (!"REQUIRED".equals(clusterTemplateResponse.getDatalakeRequired())) {
            throw new IllegalArgumentException(String
                    .format("Mismatch datalake required result, REQUIRED expected but got %s", clusterTemplateResponse.getDatalakeRequired()));
        }

        if (!ResourceStatus.USER_MANAGED.equals(clusterTemplateResponse.getStatus())) {
            throw new IllegalArgumentException(String
                    .format("Mismatch status result, USER_MANAGED expected but got %s", clusterTemplateResponse.getStatus()));
        }

        if (clusterTemplateResponse.getCloudPlatform() != null) {
            throw new IllegalArgumentException("Cloudplatform is enabled in DEFAULT template only");
        }

        return entity;
    }
}

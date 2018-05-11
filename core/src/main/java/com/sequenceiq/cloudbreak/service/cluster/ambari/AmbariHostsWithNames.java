package com.sequenceiq.cloudbreak.service.cluster.ambari;

import java.util.List;

import com.sequenceiq.ambari.client.AmbariClient;
import com.sequenceiq.cloudbreak.domain.stack.Stack;
import com.sequenceiq.cloudbreak.service.cluster.flow.AmbariClientPollerObject;

public class AmbariHostsWithNames extends AmbariClientPollerObject {

    private final List<String> hostNames;

    public AmbariHostsWithNames(Stack stack, AmbariClient ambariClient, List<String> hostNames) {
        super(stack, ambariClient);
        this.hostNames = hostNames;
    }

    public List<String> getHostNames() {
        return hostNames;
    }
}

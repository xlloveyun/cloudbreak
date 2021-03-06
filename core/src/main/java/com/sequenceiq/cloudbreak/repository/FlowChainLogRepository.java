package com.sequenceiq.cloudbreak.repository;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.sequenceiq.cloudbreak.aspect.DisableHasPermission;
import com.sequenceiq.cloudbreak.aspect.DisabledBaseRepository;
import com.sequenceiq.cloudbreak.domain.FlowChainLog;
import com.sequenceiq.cloudbreak.service.EntityType;

@EntityType(entityClass = FlowChainLog.class)
@Transactional(Transactional.TxType.REQUIRED)
@DisableHasPermission
public interface FlowChainLogRepository extends DisabledBaseRepository<FlowChainLog, Long> {

    FlowChainLog findFirstByFlowChainIdOrderByCreatedDesc(String flowChainId);

    @Modifying
    @Query("DELETE FROM FlowChainLog fch WHERE fch.flowChainId NOT IN ( SELECT DISTINCT fl.flowChainId FROM FlowLog fl )")
    int purgeOrphanFLowChainLogs();
}

package com.sequenceiq.cloudbreak.converter.v2;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.model.kdc.KdcExistingFreeIpa;
import com.sequenceiq.cloudbreak.converter.AbstractConversionServiceAwareConverter;
import com.sequenceiq.cloudbreak.domain.KerberosConfig;

@Component
public class KdcExistingFreeIpaToKerberosConfigConverter extends AbstractConversionServiceAwareConverter<KdcExistingFreeIpa, KerberosConfig> {

    @Override
    public KerberosConfig convert(KdcExistingFreeIpa source) {
        KerberosConfig config = new KerberosConfig();
        config.setAdminUrl(source.getAdminUrl());
        config.setRealm(source.getRealm());
        config.setUrl(source.getUrl());
        config.setName(source.getName());
        config.setType(source.getType());
        config.setDomain(source.getDomain());
        config.setNameServers(source.getNameServers());
        config.setPassword(source.getPassword());
        config.setVerifyKdcTrust(source.getVerifyKdcTrust());
        config.setTcpAllowed(source.getTcpAllowed());
        return config;
    }

}

package com.sequenceiq.it.cloudbreak.newway;

import java.util.function.Function;

import com.sequenceiq.cloudbreak.api.model.KerberosResponse;
import com.sequenceiq.cloudbreak.api.model.kdc.KerberosRequest;
import com.sequenceiq.cloudbreak.api.model.kdc.KerberosFreeIpaRequest;
import com.sequenceiq.it.IntegrationTestContext;
import com.sequenceiq.it.cloudbreak.newway.context.TestContext;

@Prototype
public class KerberosEntity extends AbstractCloudbreakEntity<KerberosRequest, KerberosResponse, KerberosEntity>  {
    public static final String KERBEROS_REQUEST = "KERBEROS_REQUEST";

    public static final String DEFAULT_MASTERKEY = "masterkey";

    public static final String DEFAULT_ADMIN_USER = "admin";

    public static final String DEFAULT_ADMIN_PASSWORD = "password";

    private KerberosRequest request;

    KerberosEntity(String newId) {
        super(newId);
        request = new KerberosRequest();
        KerberosFreeIpaRequest ipa = new KerberosFreeIpaRequest();
        ipa.setPassword(DEFAULT_ADMIN_PASSWORD);
        ipa.setAdminUrl("http://someurl.com");
        ipa.setRealm("somerealm");
        ipa.setUrl("someUrl");
        request.setName("FreeIpaKdc");
        request.setFreeIpa(ipa);
    }

    KerberosEntity() {
        this(KERBEROS_REQUEST);
    }

    public KerberosEntity(TestContext testContext) {
        super(new KerberosRequest(), testContext);
    }

    public KerberosRequest getRequest() {
        return request;
    }

    public void setRequest(KerberosRequest request) {
        this.request = request;
    }

    public KerberosEntity withMasterKey(String masterKey) {
        request.getFreeIpa().setMasterKey(masterKey);
        return this;
    }

    public KerberosEntity withPassword(String password) {
        request.getFreeIpa().setPassword(password);
        return this;
    }

    public static KerberosEntity request(String key) {
        return new KerberosEntity(key);
    }

    public static KerberosEntity request() {
        return new KerberosEntity();
    }

    public static Function<IntegrationTestContext, KerberosEntity> getTestContextCluster(String key) {
        return testContext -> testContext.getContextParam(key, KerberosEntity.class);
    }

    public static Function<IntegrationTestContext, KerberosEntity> getTestContextCluster() {
        return getTestContextCluster(KERBEROS_REQUEST);
    }
}

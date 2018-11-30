package com.sequenceiq.cloudbreak.converter.v2;

import static com.sequenceiq.cloudbreak.type.KerberosType.ACTIVE_DIRECTORY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sequenceiq.cloudbreak.api.model.kdc.KerberosAdRequest;
import com.sequenceiq.cloudbreak.domain.KerberosConfig;
import com.sequenceiq.cloudbreak.type.KerberosType;

public class KerberosAdRequestToKerberosConfigConverterTest {

    private static final String ADMIN_URL = "someurl";

    private static final String CONTAINER_DN = "dn";

    private static final String LDAP_URL = "ldap://someurl.com";

    private static final String REALM = "someRealm";

    private static final String URL = "someotherurl.com";

    private static final String PRINCIPAL = "principal";

    private static final String NAME = "configname";

    private static final KerberosType TYPE = ACTIVE_DIRECTORY;

    private static final String DOMAIN = "someDomain";

    private static final String NAME_SERVERS = "1.1.1.1";

    private static final String PASSWORD = "somePassword";

    private static final Boolean VERIFY_KDC_TRUST = false;

    private static final Boolean TCP_ALLOWED = false;

    private KerberosAdRequestToKerberosConfigConverter underTest;

    @Mock
    private KerberosAdRequest request;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        underTest = new KerberosAdRequestToKerberosConfigConverter();
        when(request.getAdminUrl()).thenReturn(ADMIN_URL);
        when(request.getContainerDn()).thenReturn(CONTAINER_DN);
        when(request.getLdapUrl()).thenReturn(LDAP_URL);
        when(request.getRealm()).thenReturn(REALM);
        when(request.getUrl()).thenReturn(URL);
        when(request.getPrincipal()).thenReturn(PRINCIPAL);
        when(request.getName()).thenReturn(NAME);
        when(request.getType()).thenReturn(TYPE);
        when(request.getDomain()).thenReturn(DOMAIN);
        when(request.getNameServers()).thenReturn(NAME_SERVERS);
        when(request.getPassword()).thenReturn(PASSWORD);
        when(request.getVerifyKdcTrust()).thenReturn(VERIFY_KDC_TRUST);
        when(request.getTcpAllowed()).thenReturn(TCP_ALLOWED);
    }

    @Test
    public void testConvertWhenPassingRequestThenExpectedOutputsShouldHavePlaced() {
        KerberosConfig result = underTest.convert(request);

        assertNotNull(result);
        assertEquals(ADMIN_URL, result.getAdminUrl());
        assertEquals(CONTAINER_DN, result.getContainerDn());
        assertEquals(LDAP_URL, result.getLdapUrl());
        assertEquals(REALM, result.getRealm());
        assertEquals(URL, result.getUrl());
        assertEquals(PRINCIPAL, result.getPrincipal());
        assertEquals(NAME, result.getName());
        assertEquals(TYPE, result.getType());
        assertEquals(DOMAIN, result.getDomain());
        assertEquals(NAME_SERVERS, result.getNameServers());
        assertEquals(PASSWORD, result.getPassword());
        assertEquals(VERIFY_KDC_TRUST, result.getVerifyKdcTrust());
        assertEquals(TCP_ALLOWED, result.isTcpAllowed());
    }

}
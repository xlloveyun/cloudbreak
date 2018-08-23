package com.sequenceiq.cloudbreak.service.stack;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.sequenceiq.cloudbreak.authorization.OrganizationPermissions.Action;
import com.sequenceiq.cloudbreak.authorization.OrganizationResource;
import com.sequenceiq.cloudbreak.authorization.PermissionCheckingUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.sequenceiq.cloudbreak.api.model.DetailedStackStatus;
import com.sequenceiq.cloudbreak.cloud.PlatformParameters;
import com.sequenceiq.cloudbreak.cloud.model.Variant;
import com.sequenceiq.cloudbreak.controller.exception.NotFoundException;
import com.sequenceiq.cloudbreak.core.CloudbreakImageCatalogException;
import com.sequenceiq.cloudbreak.core.CloudbreakImageNotFoundException;
import com.sequenceiq.cloudbreak.core.flow2.service.ReactorFlowManager;
import com.sequenceiq.cloudbreak.domain.SecurityConfig;
import com.sequenceiq.cloudbreak.domain.StackAuthentication;
import com.sequenceiq.cloudbreak.domain.organization.Organization;
import com.sequenceiq.cloudbreak.domain.organization.User;
import com.sequenceiq.cloudbreak.domain.stack.Stack;
import com.sequenceiq.cloudbreak.domain.stack.instance.InstanceMetaData;
import com.sequenceiq.cloudbreak.repository.InstanceGroupRepository;
import com.sequenceiq.cloudbreak.repository.InstanceMetaDataRepository;
import com.sequenceiq.cloudbreak.repository.SecurityConfigRepository;
import com.sequenceiq.cloudbreak.repository.StackRepository;
import com.sequenceiq.cloudbreak.service.ComponentConfigProvider;
import com.sequenceiq.cloudbreak.service.StackUpdater;
import com.sequenceiq.cloudbreak.service.TlsSecurityService;
import com.sequenceiq.cloudbreak.service.TransactionService;
import com.sequenceiq.cloudbreak.service.cluster.ClusterService;
import com.sequenceiq.cloudbreak.service.events.CloudbreakEventService;
import com.sequenceiq.cloudbreak.service.image.ImageService;
import com.sequenceiq.cloudbreak.service.image.StatedImage;
import com.sequenceiq.cloudbreak.service.messages.CloudbreakMessagesService;
import com.sequenceiq.cloudbreak.service.organization.OrganizationService;
import com.sequenceiq.cloudbreak.service.stack.connector.adapter.ServiceProviderConnectorAdapter;
import com.sequenceiq.cloudbreak.service.user.UserService;
import org.springframework.security.access.AccessDeniedException;

@RunWith(MockitoJUnitRunner.class)
public class StackServiceTest {

    private static final Long STACK_ID = 1L;

    private static final Long ORGANIZATION_ID = 1L;

    private static final String INSTANCE_ID = "instanceId";

    private static final String INSTANCE_ID2 = "instanceId2";

    private static final String INSTANCE_PUBLIC_IP = "2.2.2.2";

    private static final String INSTANCE_PUBLIC_IP2 = "3.3.3.3";

    private static final String OWNER = "1234567";

    private static final String USER_ID = OWNER;

    private static final String VARIANT_VALUE = "VARIANT_VALUE";

    private static final String IMAGE_CATALOG = "IMAGE_CATALOG";

    private static final String STACK_NAME = "name";

    private static final String STACK_DELETE_ACCESS_DENIED = "You cannot modify this Stack";

    private static final String STACK_NOT_FOUND_BY_ID_MESSAGE = "Stack '%d' not found";

    private static final String STACK_NOT_FOUND_BY_NAME_MESSAGE = "Stack '%s' not found";

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @InjectMocks
    private StackService underTest;

    @Mock
    private StackRepository stackRepository;

    @Mock
    private InstanceMetaDataRepository instanceMetaDataRepository;

    @Mock
    private ReactorFlowManager flowManager;

    @Mock
    private StackDownscaleValidatorService downscaleValidatorService;

    @Mock
    private Stack stack;

    @Mock
    private InstanceMetaData instanceMetaData;

    @Mock
    private InstanceMetaData instanceMetaData2;

    @Mock
    private User user;

    @Mock
    private Organization organization;

    @Mock
    private ServiceProviderConnectorAdapter connector;

    @Mock
    private Variant variant;

    @Mock
    private StackAuthentication stackAuthentication;

    @Mock
    private ComponentConfigProvider componentConfigProvider;

    @Mock
    private InstanceGroupRepository instanceGroupRepository;

    @Mock
    private TlsSecurityService tlsSecurityService;

    @Mock
    private SecurityConfig securityConfig;

    @Mock
    private SecurityConfigRepository securityConfigRepository;

    @Mock
    private ImageService imageService;

    @Mock
    private PlatformParameters parameters;

    @Mock
    private StackUpdater stackUpdater;

    @Mock
    private TransactionService transactionService;

    @Mock
    private CloudbreakEventService eventService;

    @Mock
    private ClusterService clusterService;

    @Mock
    private CloudbreakMessagesService cloudbreakMessagesService;

    @Mock
    private OrganizationService organizationService;

    @Mock
    private UserService userService;

    @Mock
    private PermissionCheckingUtils permissionCheckingUtils;

    @Before
    public void setup() {
        when(stack.getId()).thenReturn(STACK_ID);
        when(stack.getName()).thenReturn(STACK_NAME);
        when(stack.getOrganization()).thenReturn(organization);
        when(organization.getId()).thenReturn(ORGANIZATION_ID);
    }

    // TODO: have to write new tests

    @Test
    public void testWhenStackCouldNotFindByItsIdThenExceptionWouldThrown() {
        when(stackRepository.findById(STACK_ID)).thenReturn(Optional.empty());
        expectedException.expect(NotFoundException.class);
        expectedException.expectMessage(String.format(STACK_NOT_FOUND_BY_ID_MESSAGE, STACK_ID));
        underTest.getById(STACK_ID);
    }

    @Test
    public void testDeleteWhenStackCouldNotFindByItsNameForOrganizationAndForcedAndDeleteDepsThenExceptionShouldCome() {
        when(stackRepository.findByNameAndOrganizationId(STACK_NAME, ORGANIZATION_ID)).thenReturn(null);

        expectedException.expect(NotFoundException.class);
        expectedException.expectMessage(String.format(STACK_NOT_FOUND_BY_NAME_MESSAGE, STACK_NAME));

        underTest.delete(STACK_NAME, ORGANIZATION_ID, true, true, user);

        verify(stackRepository, times(0)).findById(anyLong());
        verify(stackRepository, times(0)).findById(STACK_ID);
        verify(stackRepository, times(1)).findByNameAndOrganizationId(anyString(), anyLong());
        verify(stackRepository, times(1)).findByNameAndOrganizationId(STACK_NAME, ORGANIZATION_ID);
    }

    @Test
    public void testDeleteWhenStackCouldNotFindByItsNameForOrganizationAndDeleteDepsButNotForcedThenExceptionShouldCome() {
        when(stackRepository.findByNameAndOrganizationId(STACK_NAME, ORGANIZATION_ID)).thenReturn(null);

        expectedException.expect(NotFoundException.class);
        expectedException.expectMessage(String.format(STACK_NOT_FOUND_BY_NAME_MESSAGE, STACK_NAME));

        underTest.delete(STACK_NAME, ORGANIZATION_ID, false, true, user);

        verify(stackRepository, times(0)).findById(anyLong());
        verify(stackRepository, times(0)).findById(STACK_ID);
        verify(stackRepository, times(1)).findByNameAndOrganizationId(anyString(), anyLong());
        verify(stackRepository, times(1)).findByNameAndOrganizationId(STACK_NAME, ORGANIZATION_ID);
    }

    @Test
    public void testDeleteWhenStackCouldNotFindByItsNameForOrganizationAndForcedButNotDeleteDepsThenExceptionShouldCome() {
        when(stackRepository.findByNameAndOrganizationId(STACK_NAME, ORGANIZATION_ID)).thenReturn(null);

        expectedException.expect(NotFoundException.class);
        expectedException.expectMessage(String.format(STACK_NOT_FOUND_BY_NAME_MESSAGE, STACK_NAME));

        underTest.delete(STACK_NAME, ORGANIZATION_ID, true, false, user);

        verify(stackRepository, times(0)).findById(anyLong());
        verify(stackRepository, times(0)).findById(STACK_ID);
        verify(stackRepository, times(1)).findByNameAndOrganizationId(anyString(), anyLong());
        verify(stackRepository, times(1)).findByNameAndOrganizationId(STACK_NAME, ORGANIZATION_ID);
    }

    @Test
    public void testDeleteWhenStackCouldNotFindByItsNameForOrganizationAndNotForcedAndNotDeleteDepsThenExceptionShouldCome() {
        when(stackRepository.findByNameAndOrganizationId(STACK_NAME, ORGANIZATION_ID)).thenReturn(null);

        expectedException.expect(NotFoundException.class);
        expectedException.expectMessage(String.format(STACK_NOT_FOUND_BY_NAME_MESSAGE, STACK_NAME));

        underTest.delete(STACK_NAME, ORGANIZATION_ID, false, false, user);

        verify(stackRepository, times(0)).findById(anyLong());
        verify(stackRepository, times(0)).findById(STACK_ID);
        verify(stackRepository, times(1)).findByNameAndOrganizationId(anyString(), anyLong());
        verify(stackRepository, times(1)).findByNameAndOrganizationId(STACK_NAME, ORGANIZATION_ID);
    }

    @Test
    public void testDeleteWhenStackCouldNotFindByItsIdForOrganizationAndForcedAndDeleteDepsThenExceptionShouldCome() {
        when(stackRepository.findById(STACK_ID)).thenReturn(Optional.empty());

        expectedException.expect(NotFoundException.class);
        expectedException.expectMessage(String.format(STACK_NOT_FOUND_BY_NAME_MESSAGE, STACK_ID));

        underTest.delete(STACK_ID, true, true, user);

        verify(stackRepository, times(1)).findById(anyLong());
        verify(stackRepository, times(1)).findById(STACK_ID);
        verify(stackRepository, times(1)).findByNameAndOrganizationId(anyString(), anyLong());
        verify(stackRepository, times(1)).findByNameAndOrganizationId(STACK_NAME, ORGANIZATION_ID);
    }

    @Test
    public void testDeleteWhenStackCouldNotFindByItsIdForOrganizationAndDeleteDepsButNotForcedThenExceptionShouldCome() {
        when(stackRepository.findById(STACK_ID)).thenReturn(Optional.empty());

        expectedException.expect(NotFoundException.class);
        expectedException.expectMessage(String.format(STACK_NOT_FOUND_BY_NAME_MESSAGE, STACK_ID));

        underTest.delete(STACK_ID, true, true, user);
        verify(stackRepository, times(1)).findById(anyLong());
        verify(stackRepository, times(1)).findById(STACK_ID);
        verify(stackRepository, times(1)).findByNameAndOrganizationId(anyString(), anyLong());
        verify(stackRepository, times(1)).findByNameAndOrganizationId(STACK_NAME, ORGANIZATION_ID);
    }

    @Test
    public void testDeleteWhenStackCouldNotFindByItsIdForOrganizationAndForcedButNotDeleteDepsThenExceptionShouldCome() {
        when(stackRepository.findById(STACK_ID)).thenReturn(Optional.empty());

        expectedException.expect(NotFoundException.class);
        expectedException.expectMessage(String.format(STACK_NOT_FOUND_BY_NAME_MESSAGE, STACK_ID));

        underTest.delete(STACK_ID, true, true, user);
        verify(stackRepository, times(1)).findById(anyLong());
        verify(stackRepository, times(1)).findById(STACK_ID);
        verify(stackRepository, times(1)).findByNameAndOrganizationId(anyString(), anyLong());
        verify(stackRepository, times(1)).findByNameAndOrganizationId(STACK_NAME, ORGANIZATION_ID);
    }

    @Test
    public void testDeleteWhenStackCouldNotFindByItsIdForOrganizationAndNotForcedAndNotDeleteDepsThenExceptionShouldCome() {
        when(stackRepository.findById(STACK_ID)).thenReturn(Optional.empty());

        expectedException.expect(NotFoundException.class);
        expectedException.expectMessage(String.format(STACK_NOT_FOUND_BY_NAME_MESSAGE, STACK_ID));

        underTest.delete(STACK_ID, true, true, user);
        verify(stackRepository, times(1)).findById(anyLong());
        verify(stackRepository, times(1)).findById(STACK_ID);
        verify(stackRepository, times(1)).findByNameAndOrganizationId(anyString(), anyLong());
        verify(stackRepository, times(1)).findByNameAndOrganizationId(STACK_NAME, ORGANIZATION_ID);
    }

    @Test
    public void testDeleteByIdWhenStackIsAlreadyDeletedThenDeletionWillNotTrigger() {
        when(stackRepository.findById(STACK_ID)).thenReturn(Optional.of(stack));
        when(stackRepository.findByNameAndOrganizationId(STACK_NAME, ORGANIZATION_ID)).thenReturn(stack);
        doNothing().when(permissionCheckingUtils).checkPermissionByOrgIdForUser(ORGANIZATION_ID, OrganizationResource.STACK, Action.WRITE, user);
        when(stack.isDeleteCompleted()).thenReturn(true);

        underTest.delete(STACK_ID, true, true, user);

        verify(flowManager, times(0)).triggerTermination(anyLong(), anyBoolean(), anyBoolean());
        verify(permissionCheckingUtils, times(1)).checkPermissionByOrgIdForUser(anyLong(), any(OrganizationResource.class), any(Action.class), any(User.class));
        verify(permissionCheckingUtils, times(1)).checkPermissionByOrgIdForUser(ORGANIZATION_ID, OrganizationResource.STACK, Action.WRITE, user);
    }

    @Test
    public void testDeleteByNameAndOrgIdWhenStackIsAlreadyDeletedThenDeletionWillNotTrigger() {
        when(stackRepository.findByNameAndOrganizationId(STACK_NAME, ORGANIZATION_ID)).thenReturn(stack);
        doNothing().when(permissionCheckingUtils).checkPermissionByOrgIdForUser(ORGANIZATION_ID, OrganizationResource.STACK, Action.WRITE, user);
        when(stack.isDeleteCompleted()).thenReturn(true);

        underTest.delete(STACK_NAME, ORGANIZATION_ID, true, true, user);

        verify(flowManager, times(0)).triggerTermination(anyLong(), anyBoolean(), anyBoolean());
        verify(permissionCheckingUtils, times(1)).checkPermissionByOrgIdForUser(anyLong(), any(OrganizationResource.class), any(Action.class), any(User.class));
        verify(permissionCheckingUtils, times(1)).checkPermissionByOrgIdForUser(ORGANIZATION_ID, OrganizationResource.STACK, Action.WRITE, user);
    }

    @Test
    public void testDeleteByIdWhenUserHasNoWriteRightOverStackThenExceptionShouldComeAndTerminationShouldNotBeCalled() {
        when(stackRepository.findById(STACK_ID)).thenReturn(Optional.of(stack));
        when(stackRepository.findByNameAndOrganizationId(STACK_NAME, ORGANIZATION_ID)).thenReturn(stack);
        doThrow(new AccessDeniedException(STACK_DELETE_ACCESS_DENIED)).when(permissionCheckingUtils).checkPermissionByOrgIdForUser(ORGANIZATION_ID,
                OrganizationResource.STACK, Action.WRITE, user);

        expectedException.expect(AccessDeniedException.class);
        expectedException.expectMessage(STACK_DELETE_ACCESS_DENIED);

        underTest.delete(STACK_ID, true, true, user);

        verify(flowManager, times(0)).triggerTermination(anyLong(), anyBoolean(), anyBoolean());
        verify(permissionCheckingUtils, times(1)).checkPermissionByOrgIdForUser(anyLong(), any(OrganizationResource.class), any(Action.class), any(User.class));
        verify(permissionCheckingUtils, times(1)).checkPermissionByOrgIdForUser(ORGANIZATION_ID, OrganizationResource.STACK, Action.WRITE, user);
    }

    @Test
    public void testDeleteByNameAndOrgIdWhenUserHasNoWriteRightOverStackThenExceptionShouldComeAndTerminationShouldNotBeCalled() {
        when(stackRepository.findByNameAndOrganizationId(STACK_NAME, ORGANIZATION_ID)).thenReturn(stack);
        doThrow(new AccessDeniedException(STACK_DELETE_ACCESS_DENIED)).when(permissionCheckingUtils).checkPermissionByOrgIdForUser(ORGANIZATION_ID,
                OrganizationResource.STACK, Action.WRITE, user);

        expectedException.expect(AccessDeniedException.class);
        expectedException.expectMessage(STACK_DELETE_ACCESS_DENIED);

        underTest.delete(STACK_NAME, ORGANIZATION_ID, true, true, user);

        verify(flowManager, times(0)).triggerTermination(anyLong(), anyBoolean(), anyBoolean());
        verify(permissionCheckingUtils, times(1)).checkPermissionByOrgIdForUser(anyLong(), any(OrganizationResource.class), any(Action.class), any(User.class));
        verify(permissionCheckingUtils, times(1)).checkPermissionByOrgIdForUser(ORGANIZATION_ID, OrganizationResource.STACK, Action.WRITE, user);
    }

    @Test
    public void testDeleteByIdWhenUserHasWriteRightOverStackAndStackIsNotDeletedThenTerminationShouldBeCalled() {
        when(stackRepository.findById(STACK_ID)).thenReturn(Optional.of(stack));
        when(stackRepository.findByNameAndOrganizationId(STACK_NAME, ORGANIZATION_ID)).thenReturn(stack);
        doNothing().when(permissionCheckingUtils).checkPermissionByOrgIdForUser(ORGANIZATION_ID, OrganizationResource.STACK, Action.WRITE, user);

        underTest.delete(STACK_ID, true, true, user);

        verify(flowManager, times(1)).triggerTermination(anyLong(), anyBoolean(), anyBoolean());
        verify(flowManager, times(1)).triggerTermination(STACK_ID, true, true);
        verify(permissionCheckingUtils, times(1)).checkPermissionByOrgIdForUser(anyLong(), any(OrganizationResource.class), any(Action.class), any(User.class));
        verify(permissionCheckingUtils, times(1)).checkPermissionByOrgIdForUser(ORGANIZATION_ID, OrganizationResource.STACK, Action.WRITE, user);
    }

    @Test
    public void testDeleteByNameAndOrgIdWhenUserHasWriteRightOverStackAndStackIsNotDeletedThenTerminationShouldBeCalled() {
        when(stackRepository.findByNameAndOrganizationId(STACK_NAME, ORGANIZATION_ID)).thenReturn(stack);
        doNothing().when(permissionCheckingUtils).checkPermissionByOrgIdForUser(ORGANIZATION_ID, OrganizationResource.STACK, Action.WRITE, user);

        underTest.delete(STACK_NAME, ORGANIZATION_ID, true, true, user);

        verify(flowManager, times(1)).triggerTermination(anyLong(), anyBoolean(), anyBoolean());
        verify(flowManager, times(1)).triggerTermination(STACK_ID, true, true);
        verify(permissionCheckingUtils, times(1)).checkPermissionByOrgIdForUser(anyLong(), any(OrganizationResource.class), any(Action.class), any(User.class));
        verify(permissionCheckingUtils, times(1)).checkPermissionByOrgIdForUser(ORGANIZATION_ID, OrganizationResource.STACK, Action.WRITE, user);
    }

    @Test
    public void testCreateFailsWithInvalidImageId() throws CloudbreakImageNotFoundException, CloudbreakImageCatalogException {
        when(connector.checkAndGetPlatformVariant(stack)).thenReturn(variant);
        when(variant.value()).thenReturn(VARIANT_VALUE);

        when(stack.getStackAuthentication()).thenReturn(stackAuthentication);
        when(stackAuthentication.passwordAuthenticationRequired()).thenReturn(false);

        when(stackRepository.save(stack)).thenReturn(stack);

        when(tlsSecurityService.storeSSHKeys()).thenReturn(securityConfig);
        when(connector.getPlatformParameters(stack)).thenReturn(parameters);

        expectedException.expectCause(org.hamcrest.Matchers.any(CloudbreakImageNotFoundException.class));

        String platformString = "AWS";
        doThrow(new CloudbreakImageNotFoundException("Image not found"))
                .when(imageService)
                .create(eq(stack), eq(platformString), eq(parameters), nullable(StatedImage.class));

        try {
            stack = underTest.create(stack, platformString, mock(StatedImage.class), user, organization);
        } finally {
            verify(stack, times(1)).setPlatformVariant(eq(VARIANT_VALUE));
            verify(securityConfig, times(1)).setSaltPassword(anyObject());
            verify(securityConfig, times(1)).setSaltBootPassword(anyObject());
            verify(securityConfig, times(1)).setKnoxMasterSecret(anyObject());
            verify(securityConfig, times(1)).setStack(stack);
            verify(securityConfigRepository, times(1)).save(securityConfig);
        }
    }

    @Test
    public void testCreateImageFoundNoStackStatusUpdate() {
        when(connector.checkAndGetPlatformVariant(stack)).thenReturn(variant);
        when(variant.value()).thenReturn(VARIANT_VALUE);

        when(stack.getStackAuthentication()).thenReturn(stackAuthentication);
        when(stackAuthentication.passwordAuthenticationRequired()).thenReturn(false);

        when(stackRepository.save(stack)).thenReturn(stack);

        when(tlsSecurityService.storeSSHKeys()).thenReturn(securityConfig);
        when(connector.getPlatformParameters(stack)).thenReturn(parameters);

        try {
            stack = underTest.create(stack, "AWS", mock(StatedImage.class), user, organization);
        } finally {
            verify(stack, times(1)).setPlatformVariant(eq(VARIANT_VALUE));
            verify(securityConfig, times(1)).setSaltPassword(anyObject());
            verify(securityConfig, times(1)).setSaltBootPassword(anyObject());
            verify(securityConfig, times(1)).setKnoxMasterSecret(anyObject());
            verify(securityConfig, times(1)).setStack(stack);
            verify(securityConfigRepository, times(1)).save(securityConfig);

            verify(stackUpdater, times(0)).updateStackStatus(eq(Long.MAX_VALUE), eq(DetailedStackStatus.PROVISION_FAILED), anyString());
        }
    }
}

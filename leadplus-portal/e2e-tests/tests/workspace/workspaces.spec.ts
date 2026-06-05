import { test } from '../../fixtures';
import { credentials } from '../../test-data/credentials.data';
import { generateWorkspaceUser } from '../../test-data/workspaces.data';

test.describe('Workspaces - Invite & Cancel Flow', () => {

    test.beforeEach(async ({
        vendorSignInPage,
        dashboardPage,
        workspacesPage,
    }) => {

        await vendorSignInPage.goto();
        await vendorSignInPage.login(
            credentials.vendor.username,
            credentials.vendor.password
        );
        await dashboardPage.isLoaded();
        await dashboardPage.layout.sidebar.goToWorkspaces();
        await workspacesPage.expectOnWorkspacesPage();
    });

    test('Invite member and validate in table', async ({
        workspacesPage
    }) => {

        const user = generateWorkspaceUser();

        await workspacesPage.openInviteMemberModal();
        await workspacesPage.fillInviteMemberForm(user.email, user.name);
        await workspacesPage.sendInvitation();
        await workspacesPage.validateRowByEmail(user.name, user.email);
    });

    test('Keep and Cancel invitation using first row',  async ({
        workspacesPage
    }) => {
        const user = generateWorkspaceUser();

        await workspacesPage.openInviteMemberModal();
        await workspacesPage.fillInviteMemberForm(user.email, user.name);
        await workspacesPage.sendInvitation();
        await workspacesPage.validateRowByEmail(user.name, user.email);

        const rowEmail = await workspacesPage.getFirstRowEmail();

        //Keep invitation
        await workspacesPage.openActionsAndCancelInvite(rowEmail);
        await workspacesPage.clickKeepInvitation();
        await workspacesPage.validateRowByEmail(user.name, rowEmail);

        //Cancel invitation
        await workspacesPage.openActionsAndCancelInvite(rowEmail);
        await workspacesPage.clickConfirmCancelInvitation();
        await workspacesPage.expectRowNotExists(rowEmail);

    });

});
import { BasePage } from '../Base.page';
import type { Page, Locator } from '@playwright/test';
import { expect } from '@playwright/test';

export class WorkspacesPage extends BasePage {

    readonly table: Locator;

    readonly workspaceTitle: Locator;
    readonly inviteMemberButton: Locator;

    readonly emailInput: Locator;
    readonly nameInput: Locator;
    readonly sendInvitationButton: Locator;

    readonly firstRow: Locator;

    readonly cancelMenuItem: Locator;
    readonly keepInvitationButton: Locator;
    readonly confirmCancelButton: Locator;

    constructor(page: Page) {
        super(page, { pagePath: '/workspace' });


        this.table = page.locator('table');

        this.workspaceTitle = page.getByRole('heading', {
            name: /workspace/i,
        });

        this.inviteMemberButton = page.getByRole('button', {
            name: /invite member/i,
        });

        this.emailInput = page.locator(
            'input[name="email"][type="email"]'
        );

        this.nameInput = page.locator(
            'input[name="name"][type="text"]'
        );

        this.sendInvitationButton = page.getByRole('button', {
            name: /send invitation/i,
        });

        this.firstRow = page.locator('table tbody tr').first();

        this.cancelMenuItem = page.getByRole('menuitem', {
            name: /cancel invitation/i,
        });

        this.keepInvitationButton = page.getByRole('button', {
            name: /keep invitation/i,
        });

        this.confirmCancelButton = page.getByRole('button', {
            name: /cancel invitation/i,
        });
    }


    async expectOnWorkspacesPage() {
        await expect(this.page).toHaveURL(/workspace/);
        await expect(this.workspaceTitle).toBeVisible();
    }

    async openInviteMemberModal() {
        await this.inviteMemberButton.click();
    }

    async fillInviteMemberForm(email: string, name: string) {
        await this.emailInput.fill(email);
        await this.nameInput.fill(name);
    }

    async sendInvitation() {
        await this.sendInvitationButton.click();
    }

    async validateRowByEmail(name: string, email: string) {
        const row = this.table.locator('tbody tr', {
            has: this.page.locator(`td:has-text("${email}")`),
        });

        await expect(row).toBeVisible();

        await expect(row.locator('td').nth(0)).toHaveText(name);
        await expect(row.locator('td').nth(1)).toHaveText(email);
        await expect(row.locator('td').nth(2)).toHaveText(/invited/i);
        await expect(row.locator('td').nth(3)).toHaveText(/n\/a/i);
    }

    async getFirstRowEmail() {
        return await this.firstRow.locator('td').nth(1).innerText();
    }

    async openActionsAndCancelInvite(email: string) {
        const row = this.table.locator('tbody tr', {
            has: this.page.locator(`td:has-text("${email}")`),
        });

        const actionsButton = row.locator('td').nth(4).getByRole('button');
        await actionsButton.click();

        await this.cancelMenuItem.click();
    }

    async clickKeepInvitation() {
        await this.keepInvitationButton.click();
    }

    async clickConfirmCancelInvitation() {
        await this.confirmCancelButton.click();
    }

    async expectRowExists(email: string) {
        const row = this.table.locator('tbody tr', {
            has: this.page.locator(`td:has-text("${email}")`),
        });

        await expect(row).toBeVisible();
    }

    async expectRowNotExists(email: string) {
        const row = this.table.locator('tbody tr', {
            has: this.page.locator(`td:has-text("${email}")`),
        });

        await expect(row).toHaveCount(0);
    }
}
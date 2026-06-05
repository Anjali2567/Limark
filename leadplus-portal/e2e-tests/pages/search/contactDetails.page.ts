import { expect, type Page, Locator } from '@playwright/test';
import { BasePage } from '../Base.page';

export class ContactDetailsPage extends BasePage {

    readonly contactLinkedInLink: Locator;
    readonly companyLinkedInLink: Locator;
    readonly companyWebsiteLink: Locator;
    readonly keywordsLink: Locator;
    readonly technologiesLink: Locator;
    readonly historyLink: Locator;

    constructor(page: Page) {
        super(page, { pagePath: '/contact-info' });

        this.contactLinkedInLink = page.getByRole('link', { name: /view profile/i }).first();
        this.companyLinkedInLink = page.getByRole('link', { name: /view profile/i }).nth(1);
        this.companyWebsiteLink = this.page.locator('div.flex.items-center.gap-2:has(svg.lucide-globe)').locator('a[href^="http"]');
        this.keywordsLink = page.getByRole('button', { name: /keywords/i });
        this.technologiesLink = page.getByRole('button', { name: /technologies/i });
        this.historyLink = page.getByRole('button', { name: /history/i });
    }
    async isLoaded() {
        await expect(this.page).toHaveURL(/contact-info/);
    }

    async openContactLinkedIn() {
        const [newTab] = await Promise.all([
            this.page.context().waitForEvent('page'),
            this.contactLinkedInLink.click(),
        ]);

        await newTab.waitForLoadState();
        return newTab;
    }

    async openCompanyLinkedIn() {
        const [newTab] = await Promise.all([
            this.page.context().waitForEvent('page'),
            this.companyLinkedInLink.click(),
        ]);

        await newTab.waitForLoadState();
        return newTab;
    }
    async openCompanyWebsite() {
        await expect(this.companyWebsiteLink).toBeVisible();

        const [newTab] = await Promise.all([
            this.page.context().waitForEvent('page'),
            this.companyWebsiteLink.click(),
        ]);

        await newTab.waitForLoadState();
        return newTab;
    }

    async assertLinkedInCompanyPage(tab: Page) {
        await expect(tab).toHaveURL(/linkedin\.com/);
    }

    async assertLinkedInProfilePage(tab: Page) {
        await expect(tab).toHaveURL(/linkedin\.com\/in/);
    }

    async assertCompanyWebsite(tab: Page) {
        await expect(tab).not.toHaveURL(/contact-info/);
        await expect(tab).toHaveURL(/http/);
    }

    async openKeywordsTab() {
        await this.keywordsLink.click();
        await this.page.waitForURL(/tab=keywords/);
    }

    async openTechnologiesTab() {
        await this.technologiesLink.click();
        await this.page.waitForURL(/tab=technologies/);
    }

    async openHistoryTab() {
        await this.historyLink.click();
        await this.page.waitForURL(/tab=history/);
    }
}
import { BasePage } from '../Base.page';
import { AppLayout } from '../../layouts/App.layout';
import type { Page, Locator } from '@playwright/test';
import { expect } from '@playwright/test';
export class ListsPage extends BasePage {
    readonly layout: AppLayout;
    readonly companiesButton: Locator;
    readonly tableRows: Locator;
    readonly firstRowName: Locator;
    readonly firstRow: Locator;
    readonly recordsCell: Locator;
    readonly lastModifiedCell: Locator;

    constructor(page: Page) {
        super(page, { pagePath: '/lists' });
        this.layout = new AppLayout(page);
        this.companiesButton = page.getByRole('button', { name: 'Companies' });
        this.tableRows = page.locator('table tbody tr');
        this.firstRowName = page.locator('table tbody tr:first-child td span');
        this.firstRow = page.locator('table tbody tr').first();
        this.recordsCell = this.firstRow.locator('td').nth(1);
        this.lastModifiedCell = this.firstRow.locator('td').nth(3);
    }

    async isLoaded() {
        await expect(this.page).toHaveURL(/lists/);
    }

    async switchToCompanies() {
        await this.companiesButton.click();
    }

    async assertListExistsInTable(listName: string) {
        const row = this.page.locator('table tbody tr', {
            has: this.page.locator('td span', { hasText: listName })
        });
        await expect(row).toBeVisible();
    }

    async getRowByListName(listName: string) {
        return this.page.locator('table tbody tr', {
            has: this.page.locator('td span', { hasText: listName })
        });
    }

    async assertRecordsCount(listName: string, expected: number) {
        const row = await this.getRowByListName(listName);

        const recordsCell = row.locator('td').nth(1);

        await expect(recordsCell).toBeVisible({ timeout: 15000 });
        await expect(recordsCell).toContainText(String(expected));
    }
    
    async assertLastModifiedIsToday(listName: string) {
        const row = this.page.locator('table tbody tr', {
            has: this.page.locator('td span', { hasText: listName })
        });

        const modifiedCell = row.locator('td').nth(4);

        await expect(modifiedCell).toBeVisible({ timeout: 15000 });

        const text = await modifiedCell.innerText();

        const today = new Date();

        const expected = today.toLocaleDateString('en-US', {
            month: 'short',
            day: 'numeric',
            year: 'numeric',
        });

        expect(text).toContain(expected);
    }

    async assertCreatedBy(listName: string, expectedUser: string) {
        const row = this.page.locator('table tbody tr', {
            has: this.page.locator('td span', { hasText: listName })
        });

        const createdByCell = row.locator('td').nth(3);

        await expect(createdByCell).toBeVisible({ timeout: 15000 });
        await expect(createdByCell).toHaveText(expectedUser);
    }
}

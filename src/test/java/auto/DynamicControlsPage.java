package auto;

import com.microsoft.playwright.Page;

public class DynamicControlsPage {
    private final Page page;

    public DynamicControlsPage(Page page) {
        this.page = page;
    }

    public void clickRemoveButton() {
        page.locator("button:has-text('Remove')").click();
    }

    public boolean isCheckboxVisible() {
        return page.locator("#checkbox").isVisible();
    }

    public Page getPage(){
        return this.page;
    }

}

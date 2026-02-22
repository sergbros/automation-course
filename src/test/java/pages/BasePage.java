package pages;

import com.microsoft.playwright.Page;

public class BasePage {
    protected final Page page;

    public BasePage(Page page) {
        this.page = page;
    }

    public void navigateTo(String url) {
        String baseUrl = "https://the-internet.herokuapp.com";
        page.navigate(baseUrl + url);
    }

    public String getCurrentUrl() {
        return page.url();
    }

    public void waitForPageLoad() {
        page.waitForLoadState();
    }
}

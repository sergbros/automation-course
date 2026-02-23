package base;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

public class TestContext {
    private Playwright playwright;
    private Browser browser;
    private Page page;

    public TestContext() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
        page = browser.newPage();
    }

    public Page getPage() {
        return page;
    }
}

package docker;

import com.microsoft.playwright.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.jupiter.api.Test;

public class LoginTest {
    static Playwright playwright;
    static Browser browser;
    static Page page;

    @BeforeClass
    public static void setup() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
        page = browser.newPage();
        page.navigate(System.getenv("BASE_URL") + "/login");
    }

    @Test
    public void testLogin() {
        page.locator("#username").fill("tomsmith");
        page.locator("#password").fill("SuperSecretPassword!");
        page.locator("button[type='submit']").click();
        assert page.locator(".flash.success").isVisible();
    }

    @AfterClass
    public static void teardown() {
        browser.close();
        playwright.close();
    }
}
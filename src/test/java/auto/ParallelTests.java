package auto;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
public class ParallelTests {
    private Playwright playwright;

    @BeforeAll
    void setup() {
        // Создаем Playwright один раз
        playwright = Playwright.create();
    }

    @Test
    void testLoginPage() {
        // КАЖДЫЙ тест создает свой собственный браузер
        try (Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(true))) {

            try (BrowserContext context = browser.newContext();
                 Page page = context.newPage()) {

                page.navigate("https://the-internet.herokuapp.com/login");
                assertEquals("The Internet", page.title());
            }
        }
    }

    @Test
    void testAddRemoveElements() {
        // КАЖДЫЙ тест создает свой собственный браузер
        try (Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(true))) {

            try (BrowserContext context = browser.newContext();
                 Page page = context.newPage()) {

                page.navigate("https://the-internet.herokuapp.com/add_remove_elements/");
                page.click("button:text('Add Element')");
                assertTrue(page.isVisible("button.added-manually"));
            }
        }
    }

    @AfterAll
    void teardown() {
        if (playwright != null) {
            playwright.close();
        }
    }
}
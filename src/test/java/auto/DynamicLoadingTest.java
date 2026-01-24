package auto;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitUntilState;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static java.rmi.server.LogStream.log;

public class DynamicLoadingTest {
    Playwright playwright;
    Browser browser;
    Page page;

    @Test
    void testDynamicLoading() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false)); // видимый браузер для отладки
        BrowserContext context = browser.newContext();
        page = context.newPage();
        context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true));

        // Перехват запросов на 200 ок
        page.onResponse(response -> {
            if (response.status() != 200) {
                System.out.println("Error not 200: " + response.url());
            }
            else if (response.status() == 200) {
                System.out.println("All ok 200: " + response.url());
            }
        });

        System.out.println("Navigate...");
        page.navigate("https://the-internet.herokuapp.com/dynamic_loading/1",
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));


        page.getByRole(AriaRole.BUTTON);
        Locator finishText = page.locator("#finish");
        //Ваш код...
        Assertions.assertTrue(finishText.textContent().contains("Hello World!"));
        context.tracing().stop(new Tracing.StopOptions().setPath(Paths.get("trace/trace-success.zip")));
    }

    @AfterEach
    void tearDown() {
        page.close();
        browser.close();
        playwright.close();
    }
}

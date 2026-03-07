package auto;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class HoverTest {
    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    Page page;

    @BeforeAll
    static void setupClass() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(true) // Для визуального наблюдения за тестом
                .setSlowMo(500));    // Замедление для наглядности
    }

    @BeforeEach
    void setup() {
        context = browser.newContext();
        page = context.newPage();
        page.setViewportSize(1920, 1080);
    }

    @Test
    void testHoverProfiles() {
        // Навигация с ожиданием загрузки страницы
        page.navigate("https://the-internet.herokuapp.com/hovers",
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));

        // Расширенный селектор: все элементы с классом figure
        Locator figures = page.locator(".figure");
        int count = figures.count();

        for (int i = 0; i < count; i++) {
            Locator figure = figures.nth(i);

            // Получаем информацию о пользователе из атрибута или контекста.
            String figureClass = figure.getAttribute("class");

            // Наводим курсор на элемент
            figure.hover(new Locator.HoverOptions()
                    .setForce(true)
                    .setTimeout(5000));

            // Небольшая пауза для появления анимации
            page.waitForTimeout(500);

            // Расширенный селектор: поиск ссылки внутри текущего элемента.
            Locator profileLink = figure.locator("a")
                    .and(page.locator("text=View profile"))
                    .first();

            // Проверяем, что ссылка видима после наведения
            assertTrue(profileLink.isVisible(),
                    "Profile link is not visible for figure " + (i + 1));

            // Проверяем текст ссылки
            String linkText = profileLink.textContent();
            assertEquals("View profile", linkText.trim(),
                    "Link text doesn't match for figure " + (i + 1));

            // Получаем href для проверки ID
            String href = profileLink.getAttribute("href");
            assertNotNull(href, "Href attribute is missing for figure " + (i + 1));

            // Извлекаем ID из href
            String expectedUrlPattern = "/users/" + (i + 1);
            assertTrue(href.contains(expectedUrlPattern),
                    "Expected href to contain " + expectedUrlPattern + " but was: " + href);

            // Кликаем по ссылке
            profileLink.click(new Locator.ClickOptions()
                    .setTimeout(5000)
                    .setForce(true));

            // Ждем навигации и проверяем URL
            page.waitForURL("**/users/**", new Page.WaitForURLOptions()
                    .setTimeout(5000));

            String currentUrl = page.url();
            System.out.println("Navigated to: " + currentUrl);

            // Проверяем, что URL содержит /users/{id}
            assertTrue(currentUrl.contains("/users/" + (i + 1)),
                    "URL doesn't contain expected user ID. Expected: /users/" + (i + 1) +
                            " Actual: " + currentUrl);

            // Саму страницу не проверяем, т.к. там 404.


            // Возвращаемся назад на главную
            page.goBack(new Page.GoBackOptions().setTimeout(5000));

            // Ждем загрузки главной страницы
            page.waitForSelector(".figure", new Page.WaitForSelectorOptions()
                    .setTimeout(5000));

            System.out.println("Successfully tested figure " + (i + 1));
        }
    }

    @AfterEach
    void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    @AfterAll
    static void teardownClass() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }
}
package auto;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StatusCodeApiUiTest {
    private Playwright playwright;
    private APIRequestContext apiRequest;
    private Browser browser;
    private Page page;

    // Тестовые данные
    private static final int[] STATUS_CODES = {200, 404};

    @BeforeEach
    void setup() {
        playwright = Playwright.create();

        // Настройка API контекста
        apiRequest = playwright.request().newContext(
                new APIRequest.NewContextOptions()
                        .setBaseURL("https://the-internet.herokuapp.com")
        );

        // Настройка браузера
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(false)
                        .setSlowMo(500)
        );

        page = browser.newPage();

        // Навигация на страницу статус кодов
        page.navigate("https://the-internet.herokuapp.com/status_codes");
        page.waitForSelector("div.example");
    }

    // ТЕСТ 1: Проверка статус кодов через API
    @Test @Order(1)
    void testStatusCodesViaApi() {
        System.out.println("=== ТЕСТ 1: Проверка статус кодов через API ===");

        for (int code : STATUS_CODES) {
            int actualStatusCode = getApiStatusCode(code);
            assertEquals(code, actualStatusCode,
                    "API должен вернуть статус код " + code);

            System.out.println("✓ API запрос к /status_codes/" + code +
                    " вернул статус: " + actualStatusCode);
        }
    }

    // ТЕСТ 2: Проверка статус кодов через UI
    @Test @Order(2)
    void testStatusCodesViaUi() {
        System.out.println("=== ТЕСТ 2: Проверка статус кодов через UI ===");

        for (int code : STATUS_CODES) {
            int actualStatusCode = getUiStatusCode(code);
            assertEquals(code, actualStatusCode,
                    "UI должен показать статус код " + code);

            System.out.println("✓ UI навигация к статус коду " + code +
                    " вернула статус: " + actualStatusCode);
        }
    }

    // ТЕСТ 3: Сравнение статус кодов полученных через API и UI
    @Test @Order(3)
    void testCompareApiAndUiStatusCodes() {
        System.out.println("=== ТЕСТ 3: Сравнение API и UI статус кодов ===");

        for (int code : STATUS_CODES) {
            // Получаем статус код через API
            int apiStatusCode = getApiStatusCode(code);

            // Получаем статус код через UI
            int uiStatusCode = getUiStatusCode(code);

            // Сравниваем результаты
            assertEquals(apiStatusCode, uiStatusCode,
                    "API и UI статус коды не совпадают для кода " + code);

            System.out.println("✓ Статус код " + code + " - API: " + apiStatusCode +
                    ", UI: " + uiStatusCode + " (совпадают)");
        }
    }

    // Вспомогательный метод для получения статус кода через API
    private int getApiStatusCode(int code) {
        APIResponse response = apiRequest.get("/status_codes/" + code);
        return response.status();
    }

    // Вспомогательный метод для получения статус кода через UI
    private int getUiStatusCode(int code) {
        try {
            // Находим ссылку на странице
            Locator link = page.locator("a[href*='/" + code + "']").first();

            // Проверяем, что ссылка существует
            assertTrue(link.isVisible(), "Ссылка на статус код " + code + " не найдена");

            // Кликаем по ссылке и ожидаем ответ
            Response response = page.waitForResponse(
                    res -> res.url().contains("/" + code),
                    () -> link.click(new Locator.ClickOptions().setTimeout(15000))
            );

            // Возвращаемся на главную страницу статус кодов для следующей проверки
            page.navigate("https://the-internet.herokuapp.com/status_codes");
            page.waitForLoadState(LoadState.NETWORKIDLE);

            return response.status();

        } catch (Exception e) {
            System.err.println("Ошибка при получении UI статус кода " + code + ": " + e.getMessage());
            return -1;
        }
    }

    @AfterEach
    void teardown() {
        if (page != null) {
            page.close();
        }
        if (browser != null) {
            browser.close();
        }
        if (apiRequest != null) {
            apiRequest.dispose();
        }
        if (playwright != null) {
            playwright.close();
        }
    }
}
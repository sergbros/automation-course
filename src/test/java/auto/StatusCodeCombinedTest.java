package auto;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.example.config.EnvironmentConfig;
import org.aeonbits.owner.ConfigFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StatusCodeCombinedTest {
    private Playwright playwright;
    private APIRequestContext apiRequest;
    private Browser browser;
    private Page page;
    private static EnvironmentConfig config;

    @BeforeAll
    static void loadConfig() {
        // Если нужно задать окружение (по умолчанию будет dev, т.к. файл config-dev.properties)
        // Можно передать системное свойство: -Denv=dev
        String env = System.getProperty("env", "dev");
        System.setProperty("env", env);

        // Загружаем конфигурацию с поддержкой окружения
        config = ConfigFactory.create(EnvironmentConfig.class, System.getProperties());

        System.out.println("=== ЗАГРУЖЕНА КОНФИГУРАЦИЯ ===");
        System.out.println("Окружение: " + env);
        System.out.println("Base URL: " + config.baseUrl());
        System.out.println("Браузер: " + config.browser());
        System.out.println("Headless: " + config.headless());
        System.out.println("Timeout: " + config.timeout() + " мс");
        System.out.println("==============================");
    }

    @BeforeEach
    void setup() {
        playwright = Playwright.create();

        // Настройка API контекста с базовым URL из конфига
        apiRequest = playwright.request().newContext(
                new APIRequest.NewContextOptions()
                        .setBaseURL(config.baseUrl())
        );

        // Настройка браузера из конфига
        BrowserType browserType;
        switch (config.browser().toLowerCase()) {
            case "firefox":
                browserType = playwright.firefox();
                break;
            case "webkit":
                browserType = playwright.webkit();
                break;
            case "chromium":
            default:
                browserType = playwright.chromium();
                break;
        }

        browser = browserType.launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(config.headless())
                        .setSlowMo(500) // Можно тоже вынести в конфиг при необходимости
        );

        page = browser.newPage();
        page.setDefaultTimeout(config.timeout());
    }

    @ParameterizedTest(name = "Тест для статус кода {0}")
    @ValueSource(ints = {200, 404})
    void testStatusCodeCombined(int statusCode) {
        System.out.println("\n=== ТЕСТ ДЛЯ СТАТУС КОДА: " + statusCode + " ===");

        // API проверка
        int apiStatusCode = getApiStatusCode(statusCode);
        System.out.println("✓ API запрос вернул статус: " + apiStatusCode);

        // UI проверка
        int uiStatusCode = getUiStatusCode(statusCode);
        System.out.println("✓ UI навигация вернула статус: " + uiStatusCode);

        // Сравнение результатов
        assertEquals(apiStatusCode, uiStatusCode,
                "Статус коды из API и UI не совпадают для кода " + statusCode);
        System.out.println("✓ API и UI статус коды совпадают: " + apiStatusCode + " = " + uiStatusCode);
    }

    private int getApiStatusCode(int code) {
        APIResponse response = apiRequest.get("/status_codes/" + code);
        assertEquals(code, response.status(),
                "API: Неверный статус код для " + code);
        return response.status();
    }

    private int getUiStatusCode(int code) {
        try {
            // Навигация на страницу статус кодов
            System.out.println("Навигация на страницу статус кодов...");
            page.navigate(config.baseUrl() + "/status_codes");
            page.waitForSelector("div.example", new Page.WaitForSelectorOptions()
                    .setTimeout(config.timeout()));

            // Поиск ссылки на нужный статус код
            Locator link = page.locator(
                    String.format("a[href*='status_codes/%d']", code)
            ).first();

            // Проверяем, что ссылка видима
            link.waitFor(new Locator.WaitForOptions()
                    .setTimeout(config.timeout()));

            System.out.println("Кликаем по ссылке для кода " + code + "...");

            // Перехват ответа перед кликом
            Response response = page.waitForResponse(
                    res -> res.url().contains("/status_codes/" + code),
                    () -> link.click(new Locator.ClickOptions().setTimeout(10000))
            );

            // Возвращаемся на главную страницу статус кодов
            page.navigate(config.baseUrl() + "/status_codes");
            page.waitForLoadState(LoadState.NETWORKIDLE);

            return response.status();

        } catch (Exception e) {
            throw new RuntimeException("UI проверка упала для кода " + code, e);
        }
    }

    @AfterEach
    void teardown() {
        System.out.println("Очистка ресурсов...");
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
        System.out.println("=== ТЕСТ ЗАВЕРШЕН ===\n");
    }
}
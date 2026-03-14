package auto;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.Cookie;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static auto.HomePageVisualTest.page;

public class OptimizedLoginTest {
    static private Playwright playwright;
    static private Browser browser;
    private BrowserContext context;
    private Page page;
    static private List<Cookie> authCookies = new ArrayList<>();

    @BeforeAll
    static void setUpClass() {
        // Ваш код...
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
    }


    @BeforeEach
    void setUp() {
        // Создаём новый контекст и добавляем сохранённые cookies для каждого теста
        // Ваш код...
        // Создаём новый контекст с сохранёнными cookies
        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions();

        // Добавляем сохранённые cookies, если они есть
        if (authCookies != null && !authCookies.isEmpty()) {
            context = browser.newContext(contextOptions);
            context.addCookies(authCookies);
        } else {
            context = browser.newContext();
        }

        page = context.newPage();

        // Если cookies нет, выполняем вход один раз
        if (authCookies == null || authCookies.isEmpty()) {
            authCookies = performLogin(page);
        }
    }

    @Test
    void testSecureArea() {
        page.navigate("https://the-internet.herokuapp.com/secure");
        // Проверяем, что пользователь аутентифицирован
        Assertions.assertTrue(page.locator("h2").textContent().contains("Secure Area"));
    }

    private static List<Cookie> performLogin(Page page) {
        // Аутентификация один раз перед всеми тестами
        page.navigate("https://the-internet.herokuapp.com/login");
        page.locator("#username").fill("tomsmith");
        page.locator("#password").fill("SuperSecretPassword!");
        page.locator("button[type='submit']").click();
        // Ждем загрузки страницы и проверяем успешность входа
        page.waitForSelector(".flash.success");
        // Сохраняем cookies
        List<Cookie> cookies = page.context().cookies();
        return cookies;
    }

    @AfterEach
    void tearDown() {
        if (page != null) page.close();
        if (context != null) context.close();
    }

    @AfterAll
    static void tearDownClass() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
}


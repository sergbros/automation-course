package auto;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MockedApiTest {
    static Playwright playwright;
    static Browser browser;
    private BrowserContext context;
    private Page page;

    // Мок-сервис для имитации API
    private static ApiService apiService;

    @BeforeAll
    static void setUpClass() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(true)
        );

        // Создаем мок ApiService
        apiService = mock(ApiService.class);

        // Настраиваем поведение мока - возвращаем тестовые данные
        when(apiService.fetchUserData()).thenReturn("{\"name\": \"Test User\", \"email\": \"test@example.com\"}");
    }

    @BeforeEach
    void setUp() {
        context = browser.newContext();
        page = context.newPage();
    }

    @Test
    void testUserProfileWithMockedApi() {
        // Сначала переходим на страницу
        page.navigate("https://the-internet.herokuapp.com/dynamic_content");

        // Потом передаем данные
        String userData = apiService.fetchUserData();
        page.evaluate("(data) => { window.userData = data; }", userData);

        // Теперь данные сохранятся на текущей странице
        Object result = page.evaluate("() => window.userData");
        System.out.println("Результат: " + result);

        assertNotNull(result);
        assertTrue(result.toString().contains("Test User"));
    }


    // Тестовый класс-заглушка для API сервиса
    static class ApiService {
        public String fetchUserData() {
            // Имитация медленного API-запроса
            try {
                Thread.sleep(3000); // 3 секунды задержки
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "{\"name\": \"Real User\", \"email\": \"real@example.com\"}";
        }
    }

    @AfterEach
    void tearDown() {
        if (context != null) context.close();
    }

    @AfterAll
    static void tearDownClass() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
}
package auto;

import com.microsoft.playwright.*;
import io.qameta.allure.*;
import org.junit.jupiter.api.*;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@Epic("Тесты для the-internet.herokuapp.com")       // Крупная функциональная категория (например, весь проект)
@Feature("Проверка скорости логина")
public class LoginPerformanceStopwatchTest {
    static Playwright playwright;
    static Browser browser;
    private BrowserContext context;
    private Page page;

    @BeforeAll
    static void setUpClass() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(true)
        );
    }

    @BeforeEach
    void setUp() {
        context = browser.newContext();
        page = context.newPage();
    }

    @Test                                          // Маркирует метод как тестовый случай JUnit
    @Story("Проверка главной страницы")            // Пользовательский сценарий/история из Jira/других систем
    @Description("Тест проверяет скорость логина") // Человекочитаемое описание
    @Severity(SeverityLevel.CRITICAL)              // Приоритет теста (CRITICAL, BLOCKER, NORMAL, MINOR)
    void loginPerformanceWithStopwatchTest() {
        // Используем Instant для более точного замера
        Instant start = Instant.now();

        // Выполняем сценарий входа
        page.navigate("https://the-internet.herokuapp.com/login");
        page.fill("#username", "tomsmith");
        page.fill("#password", "SuperSecretPassword!");
        page.click("button[type='submit']");

        // Проверка успешного входа
        assertTrue(page.isVisible("text='You logged into a secure area!'"));

        // Вычисляем длительность
        Duration duration = Duration.between(start, Instant.now());
        long millis = duration.toMillis();

        System.out.printf("Вход выполнен за %d мс (%.2f секунд)%n", millis, millis / 1000.0);

        Allure.addAttachment("Время выполнения теста (сек)", String.format("%.2f", millis / 1000.0));

        // Сохраняем трассировку для 10% запусков
        if (new Random().nextInt(10) == 0) {  // 10% вероятность
            context.tracing().start(new Tracing.StartOptions());

            context.tracing().stop(new Tracing.StopOptions()
                    .setPath(Paths.get("trace-" + System.currentTimeMillis() + ".zip"))
            );

            System.out.println("Трассировка сохранена (10% запусков)");
        }

        // Проверка производительности
        assertTrue(millis < 3000,
                String.format("Превышение лимита времени: %d мс > 3000 мс", millis));
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
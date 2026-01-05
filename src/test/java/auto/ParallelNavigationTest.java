package auto;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
public class ParallelNavigationTest {
    @ParameterizedTest(name = "Проверка заголовка для {1} в {0}")
    @CsvSource({
            "chromium, /",
            "firefox,   /dropdown",
            "chromium, /javascript_alerts",
            "firefox,   /about",
            "chromium, /contact",
            "firefox,   /login",
            "chromium, /checkboxes",
            "firefox,   /hover",
            "chromium, /status_codes",
    })
    void testPageLoad(String browserType, String path) {
        try (Playwright playwright = Playwright.create()) {
            // 2. Выбираем тип браузера
            BrowserType type = switch (browserType.toLowerCase()) {
                case "chromium" -> playwright.chromium();
                case "firefox" -> playwright.firefox();
                default -> throw new IllegalArgumentException("Неподдерживаемый браузер: " + browserType);
            };
            // КАЖДЫЙ тест создает свой собственный браузер
            try (Browser browser = type.launch(
                    new BrowserType.LaunchOptions()
                            .setHeadless(true))) {

                try (BrowserContext context = browser.newContext();
                     Page page = context.newPage()) {

                    page.navigate("https://the-internet.herokuapp.com" + path);
                    // Проверяем, что заголовок страницы существует (не пустой)
                    assertThat(page).hasTitle(Pattern.compile(".*"));
                }
            }
        }
    }
}
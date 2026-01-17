package auto;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import io.qameta.allure.*;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

// Аннотации для структурирования отчета Allure
@Epic("Тесты для the-internet.herokuapp.com")       // Крупная функциональная категория (например, весь проект)
@Feature("Проверка страниц")                       // Группа связанных тестов (например, проверка UI-страниц)
public class AllureReportTest {

    // Аннотации для метаданных теста
    @Test                                          // Маркирует метод как тестовый случай JUnit
    @Story("Проверка главной страницы")            // Пользовательский сценарий/история из Jira/других систем
    @Description("Тест проверяет заголовок главной страницы") // Человекочитаемое описание
    @Severity(SeverityLevel.CRITICAL)              // Приоритет теста (CRITICAL, BLOCKER, NORMAL, MINOR)
    void testHomePage() {
        // Использование try-with-resources для автоматического закрытия ресурсов
        try (Playwright playwright = Playwright.create();      // Создаем экземпляр Playwright
             Browser browser = playwright.chromium().launch()) { // Запускаем браузер Chromium

            // Создаем изолированный контекст и страницу
            BrowserContext context = browser.newContext();     // Контекст (куки, права и т.д.)
            Page page = context.newPage();                     // Новая вкладка браузера

            // Навигация на тестируемую страницу
            page.navigate("https://the-internet.herokuapp.com");

            // Шаг в отчете Allure с проверкой
            Allure.step("Проверка заголовка", () -> {
                // Проверка, что заголовок страницы соответствует ожидаемому
                assertThat(page.title()).isEqualTo("The Internet");
            });

            // Прикрепление скриншота к отчету
            Allure.addAttachment(
                    "Скриншот",                                  // Название вложения
                    "image/png",                                 // MIME-тип
                    Arrays.toString(page.screenshot(                             // Делаем скриншот
                            new Page.ScreenshotOptions()
                                    .setPath(Paths.get("screenshot.png")) // Сохраняем в файл
                    )));
        } // Playwright и Browser автоматически закрываются здесь
    }
}

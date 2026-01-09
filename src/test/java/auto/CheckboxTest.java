package auto;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestWatcher;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.microsoft.playwright.options.AriaRole.CHECKBOX;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Epic("Веб-интерфейс тестов")
@Feature("Операции с чекбоксами")
public class CheckboxTest {
    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;
    private Path screenshotDir;

    Locator checkboxLocator0;
    Locator checkboxLocator1;

    boolean isChecked0;
    boolean isChecked1;

    @BeforeEach
    @Step("Инициализация браузера и контекста")
    void setUp() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        context = browser.newContext();
        page = context.newPage();



        // Создаем директорию для скриншотов
        screenshotDir = Paths.get("screenshots/");
        try {
            Files.createDirectories(screenshotDir);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create screenshots directory", e);
        }
    }

    @Test
    @Story("Проверка работы чекбоксов")
    @DisplayName("Тестирование выбора/снятия чекбоксов")
    @Severity(SeverityLevel.CRITICAL)
    void testCheckboxes() {
        navigateToCheckboxesPage();
        verifyInitialState();
        toggleCheckboxes();
        verifyToggledState();
    }

    @Step("Переход на страницу /checkboxes")
    private void navigateToCheckboxesPage() {
        //Код...
        page.navigate("https://the-internet.herokuapp.com/checkboxes");
        checkboxLocator0 = page.getByRole(CHECKBOX).first();
        checkboxLocator1 = page.getByRole(CHECKBOX).nth(1);
    }

    @Step("Проверка начального состояния чекбоксов")
    private void verifyInitialState() {
        //Код...
        // Проверяем, установлен ли чекбокс
        isChecked0 = checkboxLocator0.isChecked();
        isChecked1 = checkboxLocator1.isChecked();
    }

    @Step("Изменение состояния чекбоксов")
    private void toggleCheckboxes() {
        //Код...
        checkboxLocator0.setChecked(!isChecked0);
        checkboxLocator1.setChecked(!isChecked1);
    }

    @Step("Проверка конечного состояния чекбоксов")
    private void verifyToggledState() {
        //Код...
        // Проверяем, установлен ли чекбокс
        Allure.step("Проверка заголовка", () -> {
            assertTrue(isChecked0 != checkboxLocator0.isChecked());
            assertTrue(isChecked1 != checkboxLocator1.isChecked());
            fail();
        });

        // Прикрепление скриншота к отчету
        Allure.addAttachment(
                "Скриншот",                                  // Название вложения
                "image/png",                                 // MIME-тип
                Arrays.toString(page.screenshot(                             // Делаем скриншот
                        new Page.ScreenshotOptions()
                                .setPath(Paths.get("screenshot.png")) // Сохраняем в файл
                )));
    }

    @AfterEach
    void tearDown() {
        context.close();
        browser.close();
        playwright.close();
    }
}

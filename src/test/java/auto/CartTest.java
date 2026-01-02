package auto;

import com.microsoft.playwright.*;
import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestWatcher;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class CartTest {
    static Playwright playwright;
    private BrowserContext context;
    private Page page;
    static Browser browser;
    private Path screenshotDir;

    @BeforeAll
    static void setupAll() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
    }

    @BeforeEach
    void setup() {
        // Создаем директорию для скриншотов
        screenshotDir = Paths.get("screenshots/");
        try {
            Files.createDirectories(screenshotDir);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create screenshots directory", e);
        }

        context = browser.newContext(new Browser.NewContextOptions()
                .setRecordVideoDir(Paths.get("videos/")));
        page = context.newPage();
    }

    @Test
    void testCartActions() {
        page.navigate("https://the-internet.herokuapp.com/add_remove_elements/");

        // Добавление товара
        page.click("button[onclick='addElement()']");
        page.locator("div.example").screenshot(new Locator.ScreenshotOptions()
                .setPath(getTimestampPath("cart_after_add.png")));

        // Удаление товара
        page.click("button[onclick='deleteElement()']");
        page.locator("div.example").screenshot(new Locator.ScreenshotOptions()
                .setPath(getTimestampPath("cart_after_remove.png")));

        throw new AssertionError("Тест упадёт намеренно");
    }

    private Path getTimestampPath(String filename) {
        return Paths.get(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + "/" + filename);
    }

    // Расширение для перехвата падений тестов
    @RegisterExtension
    TestWatcher watcher = new TestWatcher() {
        @Override
        public void testFailed(ExtensionContext extensionContext, Throwable cause) {
            try {
                if (page != null && !page.isClosed()) {
                    // Генерируем имя файла
                    String testName = extensionContext.getDisplayName();
                    Path screenshotPath = screenshotDir.resolve(testName + ".png");

                    // Делаем и сохраняем скриншот
                    byte[] screenshot = page.screenshot(new Page.ScreenshotOptions()
                            .setPath(screenshotPath)
                            .setFullPage(true));

                    // Прикрепляем в Allure
                    saveScreenshotToAllure(screenshot, testName);
                    System.out.println("Скриншот сохранен: " + screenshotPath);
                }
            } catch (Exception e) {
                System.err.println("Ошибка при создании скриншота: " + e.getMessage());
            }
        }

        // Метод для прикрепления скриншота в Allure
        @Attachment(value = "Скриншот при падении: {name}", type = "image/png")
        private byte[] saveScreenshotToAllure(byte[] screenshot, String name) {
            return screenshot;
        }
    };

    private Throwable getCurrentTestException() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            if (element.getClassName().startsWith("org.junit.jupiter.engine.descriptor")) {
                try {
                    return (Throwable) Thread.currentThread().getStackTrace()[1].getClass().getEnclosingMethod().invoke(null);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
        return null;
    }

    @AfterEach
    void attachScreenshotOnFailure() {
        // Проверяем наличие исключения
        Throwable exception = getCurrentTestException();
        if (exception != null) {
            byte[] screenshot = page.screenshot();
            Allure.addAttachment(
                    "Screenshot on Failure",
                    "image/png",
                    new ByteArrayInputStream(screenshot),
                    ".png"
            );
        }
        context.close();
    }
}
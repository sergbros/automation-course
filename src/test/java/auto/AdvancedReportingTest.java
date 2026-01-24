package auto;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import io.qameta.allure.*;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Epic("Тесты для the-internet.herokuapp.com")
@Feature("Работа с JavaScript-алертами")
public class AdvancedReportingTest {
    private static ExtentReports extent;
    private Browser browser;
    private Playwright playwright;
    private BrowserContext context;
    private Page page;
    private ExtentTest test;

    @BeforeAll
    static void setupExtent() {
        ExtentSparkReporter reporter = new ExtentSparkReporter("allure-results/extent-report.html");
        reporter.config().setDocumentTitle("Playwright Extent Report");
        extent = new ExtentReports();
        extent.attachReporter(reporter);
    }

    @BeforeEach
    void setUp(TestInfo testInfo) {
        playwright = Playwright.create();
        //Код...
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        context = browser.newContext();
        page = context.newPage();
    }

    @Test
    @Story("Проверка JS Alert")
    @Description("Тест взаимодействия с JS Alert и проверка результата")
    @Severity(SeverityLevel.NORMAL)
    void testJavaScriptAlert() {
        try {
            navigateToAlertsPage();
            String alertMessage = foJsAlert();
            verifyResultText();
            captureSuccessScreenshot();

            logExtent(Status.PASS, "Тест успешно завершен с сообщением: " + alertMessage);

        } catch (Exception e) {
            foTestFailure(e);
            throw e;
        }
    }

    @Step("Открыть страницу с алертами")
    private void navigateToAlertsPage() {
        page.navigate("https://the-internet.herokuapp.com/javascript_alerts",
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        assertEquals("JavaScript Alerts", page.locator("h3").textContent(),
                "Страница должна содержать заголовок 'JavaScript Alerts'");
        logExtent(Status.INFO, "Страница с алертами загружена");
    }

    @Step("Обработать JS Alert")
    private String foJsAlert() {
        CompletableFuture<String> alertMessageFuture = new CompletableFuture<>();

        // Тут устанавливаем обработчик диалога
        page.onDialog(dialog -> {
            String message = dialog.message();
            alertMessageFuture.complete(message);
            dialog.accept();
        });

        // Тут кликаем по кнопке, которая вызывает alert
        page.click("button[onclick='jsAlert()']");
        logExtent(Status.INFO, "Клик по кнопке JS Alert выполнен");

        // Тут ожидаем результат с таймаутом
        //Код...
        try {
            String alertMessage = alertMessageFuture.get(5, TimeUnit.SECONDS);
            logExtent(Status.INFO, "Получено сообщение алерта: " + alertMessage);
            return alertMessage;
        } catch (TimeoutException e) {
            logExtent(Status.WARNING, "Таймаут ожидания алерта");
            throw new RuntimeException("Alert не появился в течение 5 секунд", e);
        } catch (Exception e) {
            logExtent(Status.FAIL, "Ошибка при обработке алерта: " + e.getMessage());
            throw new RuntimeException("Ошибка обработки алерта", e);
        }
    }

    @Step("Проверить текст результата")
    private void verifyResultText() {
        page.waitForCondition(() ->
                page.locator("#result").textContent().contains("successfully"));

        String resultText = page.locator("#result").textContent();
        assertEquals("You successfully clicked an alert", resultText,
                "Текст результата должен соответствовать ожидаемому");
        logExtent(Status.INFO, "Результирующий текст проверен: " + resultText);
    }

    private void captureSuccessScreenshot() {
        String screenshotName = "success-screenshot.png";
        Path screenshotPath = Paths.get("allure-results", screenshotName);
        //Код...
        try {
            // Делаем скриншот
            byte[] screenshot = page.screenshot(new Page.ScreenshotOptions()
                    .setPath(screenshotPath)
                    .setFullPage(true));

            // Для Allure
            try (InputStream screenshotStream = new ByteArrayInputStream(screenshot)) {
                Allure.addAttachment("Успешное выполнение", "image/png", screenshotStream, ".png");
            } //Код...

            // Для ExtentReports
            test.pass("Скриншот успешного выполнения",
                    MediaEntityBuilder.createScreenCaptureFromPath("success-screenshot.png").build());
            //Код...
        } catch (Exception e) {
            logExtent(Status.WARNING, "Не удалось сделать скриншот: " + e.getMessage());
        }
    }

    private void logExtent(Status status, String message) {
        test.log(status, message);
    }

    private void foTestFailure(Exception e) {
        // Скриншот для Allure при ошибке
        byte[] failureScreenshot = page.screenshot();

        try (InputStream failureStream = new ByteArrayInputStream(failureScreenshot)) {
            Allure.addAttachment("Ошибка теста", "image/png", failureStream, ".png");
        } catch (Exception ex) {
            logExtent(Status.WARNING, "Не удалось добавить скриншот ошибки в Allure: " + ex.getMessage());
        }

        // Логирование ошибки в ExtentReports
        String screenshotName = "error-screenshot.png";
        Path screenshotPath = Paths.get("allure-results", screenshotName);
        //Код...
        try {
            byte[] screenshot = page.screenshot(new Page.ScreenshotOptions()
                    .setPath(screenshotPath)
                    .setFullPage(true));

            test.fail("Скриншот при ошибке: " + e.getMessage(),
                    MediaEntityBuilder.createScreenCaptureFromPath(
                            screenshotName).build());

        } catch (Exception screenshotEx) {
            logExtent(Status.WARNING, "Не удалось сохранить скриншот для ExtentReports: " + screenshotEx.getMessage());
        }

        // Добавляем стектрейс в ExtentReports
        test.fail(e);
    }

    @AfterEach
    void tearDownEach() {
        //Код...
        page.close();
        context.close();
        browser.close();
        playwright.close();
    }

    @AfterAll
    static void tearDown() {
        //Код...
        extent.flush();
    }
}

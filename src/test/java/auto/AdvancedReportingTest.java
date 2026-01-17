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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Epic("Тесты для the-internet.herokuapp.com")
@Feature("Работа с JavaScript-алертами")
public class AdvancedReportingTest {
    private static ExtentReports extent;
    private Browser browser;
    private Playwright playwright;
    private Page page;
    private ExtentTest test;

    @BeforeAll
    static void setupExtent() {
        // Создаем директорию для результатов
        Path resultsDir = Paths.get("allure-results");
        if (!resultsDir.toFile().exists()) {
            resultsDir.toFile().mkdirs();
        }

        ExtentSparkReporter reporter = new ExtentSparkReporter("allure-results/extent-report.html");
        reporter.config().setDocumentTitle("Playwright Extent Report");
        reporter.config().setReportName("JavaScript Alerts Test Report");
        reporter.config().setEncoding("utf-8");

        extent = new ExtentReports();
        extent.attachReporter(reporter);
        extent.setSystemInfo("OS", System.getProperty("os.name"));
        extent.setSystemInfo("Java Version", System.getProperty("java.version"));
    }

    @BeforeEach
    void setUp(TestInfo testInfo) {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(true));

        BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1920, 1080));
        page = context.newPage();

        // Создаем тест в ExtentReports
        String testName = testInfo.getDisplayName();
        if (testName == null || testName.isEmpty()) {
            testName = testInfo.getTestMethod().get().getName();
        }
        test = extent.createTest(testName);

        // Логируем начало теста
        test.log(Status.INFO, "Начинаем тест: " + testName);
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

//    @Test
//    @Story("Проверка JS Confirm")
//    @Description("Тест взаимодействия с JS Confirm при подтверждении")
//    @Severity(SeverityLevel.NORMAL)
//    void testJavaScriptConfirm() {
//        try {
//            navigateToAlertsPage();
//            String confirmMessage = foJsConfirm();
//            verifyConfirmResultText();
//            captureSuccessScreenshot();
//
//            logExtent(Status.PASS, "Тест JS Confirm успешно завершен с сообщением: " + confirmMessage);
//
//        } catch (Exception e) {
//            foTestFailure(e);
//            throw e;
//        }
//    }

    @Step("Открыть страницу с алертами")
    private void navigateToAlertsPage() {
        page.navigate("https://the-internet.herokuapp.com/javascript_alerts",
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

        // Ждем загрузки заголовка
        page.waitForSelector("h3", new Page.WaitForSelectorOptions().setTimeout(10000));

        String actualTitle = page.locator("h3").textContent();
        assertEquals("JavaScript Alerts", actualTitle,
                "Страница должна содержать заголовок 'JavaScript Alerts'");

        logExtent(Status.INFO, "Страница с алертами загружена");
        logExtent(Status.INFO, "Заголовок страницы: " + actualTitle);
    }

    @Step("Обработать JS Alert")
    private String foJsAlert() {
        CompletableFuture<String> alertMessageFuture = new CompletableFuture<>();

        // Устанавливаем обработчик диалога
        page.onDialog(dialog -> {
            String message = dialog.message();
            alertMessageFuture.complete(message);
            dialog.accept();
            logExtent(Status.INFO, "Alert принят: " + message);
        });

        // Кликаем по кнопке, которая вызывает alert
        page.click("button[onclick='jsAlert()']");
        logExtent(Status.INFO, "Клик по кнопке JS Alert выполнен");

        // Ожидаем результат с таймаутом
        try {
            String alertMessage = alertMessageFuture.get(5, TimeUnit.SECONDS);
            assertEquals("I am a JS Alert", alertMessage,
                    "Сообщение alert должно быть 'I am a JS Alert'");

            logExtent(Status.INFO, "Получено сообщение alert: " + alertMessage);
            return alertMessage;
        } catch (Exception e) {
            logExtent(Status.FAIL, "Ошибка при ожидании alert: " + e.getMessage());
            throw new RuntimeException("Alert не появился в течение 5 секунд", e);
        }
    }

//    @Step("Обработать JS Confirm")
//    private String foJsConfirm() {
//        CompletableFuture<String> confirmMessageFuture = new CompletableFuture<>();
//
//        // Устанавливаем обработчик диалога
//        page.onDialog(dialog -> {
//            String message = dialog.message();
//            confirmMessageFuture.complete(message);
//            dialog.accept(); // Подтверждаем диалог
//            logExtent(Status.INFO, "Confirm принят: " + message);
//        });
//
//        // Кликаем по кнопке, которая вызывает confirm
//        page.click("button[onclick='jsConfirm()']");
//        logExtent(Status.INFO, "Клик по кнопке JS Confirm выполнен");
//
//        // Ожидаем результат с таймаутом
//        try {
//            String confirmMessage = confirmMessageFuture.get(5, TimeUnit.SECONDS);
//            assertEquals("I am a JS Confirm", confirmMessage,
//                    "Сообщение confirm должно быть 'I am a JS Confirm'");
//
//            logExtent(Status.INFO, "Получено сообщение confirm: " + confirmMessage);
//            return confirmMessage;
//        } catch (Exception e) {
//            logExtent(Status.FAIL, "Ошибка при ожидании confirm: " + e.getMessage());
//            throw new RuntimeException("Confirm не появился в течение 5 секунд", e);
//        }
//    }

    @Step("Проверить текст результата")
    private void verifyResultText() {
        // Ждем появления результата
        page.waitForSelector("#result", new Page.WaitForSelectorOptions().setTimeout(5000));

        // Проверяем, что текст содержит нужное сообщение
        page.waitForCondition(() ->
                        page.locator("#result").textContent().contains("successfully"),
                new Page.WaitForConditionOptions().setTimeout(5000));

        String resultText = page.locator("#result").textContent();
        assertEquals("You successfully clicked an alert", resultText,
                "Текст результата должен соответствовать ожидаемому");

        logExtent(Status.INFO, "Результирующий текст проверен: " + resultText);
    }

//    @Step("Проверить текст результата для Confirm")
//    private void verifyConfirmResultText() {
//        // Ждем появления результата
//        page.waitForSelector("#result", new Page.WaitForSelectorOptions().setTimeout(5000));
//
//        // Проверяем, что текст содержит нужное сообщение
//        page.waitForCondition(() ->
//                        page.locator("#result").textContent().contains("Ok"),
//                new Page.WaitForConditionOptions().setTimeout(5000));
//
//        String resultText = page.locator("#result").textContent();
//        assertEquals("You clicked: Ok", resultText,
//                "Текст результата должен быть 'You clicked: Ok'");
//
//        logExtent(Status.INFO, "Результирующий текст проверен: " + resultText);
//    }

    private void captureSuccessScreenshot() {
        try {
            // Делаем скриншот
            byte[] screenshot = page.screenshot(new Page.ScreenshotOptions()
                    .setFullPage(false));

            String screenshotName = "success-screenshot.png";
            Path screenshotPath = Paths.get("allure-results", screenshotName);

            // Сохраняем скриншот в файл для ExtentReports
            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(screenshotPath)
                    .setFullPage(false));

            // Для Allure
            try (InputStream screenshotStream = new ByteArrayInputStream(screenshot)) {
                Allure.addAttachment("Успешное выполнение", "image/png", screenshotStream, ".png");
            }

            // Для ExtentReports
            String base64Screenshot = java.util.Base64.getEncoder().encodeToString(screenshot);
            test.pass("Скриншот успешного выполнения",
                    MediaEntityBuilder.createScreenCaptureFromBase64String(base64Screenshot).build());

            logExtent(Status.INFO, "Скриншот успешного выполнения сохранен");

        } catch (Exception e) {
            logExtent(Status.WARNING, "Не удалось создать скриншот: " + e.getMessage());
        }
    }

    private void logExtent(Status status, String message) {
        test.log(status, message);
    }

    private void foTestFailure(Exception e) {
        // Скриншот для Allure при ошибке
        try {
            byte[] failureScreenshot = page.screenshot(new Page.ScreenshotOptions()
                    .setFullPage(true));

            try (InputStream failureStream = new ByteArrayInputStream(failureScreenshot)) {
                Allure.addAttachment("Ошибка теста", "image/png", failureStream, ".png");
            }

            // Логирование ошибки в ExtentReports
            String screenshotName = "error-screenshot.png";
            Path screenshotPath = Paths.get("allure-results", screenshotName);

            // Сохраняем скриншот в файл
            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(screenshotPath)
                    .setFullPage(true));

            // Добавляем скриншот в ExtentReports
            String base64Screenshot = java.util.Base64.getEncoder().encodeToString(failureScreenshot);
            test.fail("Тест завершился с ошибкой: " + e.getMessage(),
                    MediaEntityBuilder.createScreenCaptureFromBase64String(base64Screenshot).build());

            // Логируем детали ошибки
            test.log(Status.FAIL, "Тип ошибки: " + e.getClass().getName());
            test.log(Status.FAIL, "Сообщение: " + e.getMessage());

            // Добавляем трассировку стека
            StringBuilder stackTrace = new StringBuilder();
            for (StackTraceElement element : e.getStackTrace()) {
                stackTrace.append(element.toString()).append("\n");
                if (stackTrace.length() > 500) { // Ограничиваем длину
                    stackTrace.append("...\n");
                    break;
                }
            }
            test.log(Status.INFO, "Трассировка стека:\n" + stackTrace.toString());

        } catch (Exception ex) {
            logExtent(Status.WARNING, "Не удалось добавить скриншот ошибки в Allure: " + ex.getMessage());
            test.fail("Тест завершился с ошибкой: " + e.getMessage());
        }
    }

    @AfterEach
    void tearDownEach() {
        try {
            // Логируем завершение теста
            String status = (test.getStatus() != null) ? test.getStatus().toString() : "UNKNOWN";
            logExtent(Status.INFO, "Тест завершен со статусом: " + status);

            // Закрываем браузер и playwright
            if (browser != null) {
                browser.close();
            }
            if (playwright != null) {
                playwright.close();
            }
        } catch (Exception e) {
            System.err.println("Ошибка при завершении теста: " + e.getMessage());
        }
    }

    @AfterAll
    static void tearDown() {
        // Закрываем ExtentReports
        if (extent != null) {
            extent.flush();
            System.out.println("Extent отчет сохранен в: allure-results/extent-report.html");
        }

        System.out.println("\n=== Для просмотра отчетов выполните: ===");
        System.out.println("1. Allure отчет: allure serve allure-results");
        System.out.println("2. Extent отчет: откройте файл allure-results/extent-report.html в браузере");
    }
}
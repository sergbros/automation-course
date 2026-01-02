package auto;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.FormData;
import org.junit.jupiter.api.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class SimpleInterceptionTest {

    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    Page page;

    @BeforeAll
    static void setupAll() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
    }

    @BeforeEach
    void setup() {
        context = browser.newContext();
        page = context.newPage();
    }

    @Test
    void simpleInterceptionTest() {
        // 1. Настраиваем перехват
        page.route("**/authenticate", route -> {
            System.out.println("Запрос перехвачен!");

            // Получаем оригинальные данные
            //Ваш код...
            String postData = route.request().postData();


            // Меняем username
            //Ваш код...
            postData = postData.replace("tomsmith", "HACKED_USER");

            // Создаем ResumeOptions с новыми данными
            //Ваш код...
            Route.ResumeOptions options = new Route.ResumeOptions()
                    .setPostData(postData.getBytes());

            System.out.println("Отправляем измененный запрос");
            route.resume(options);
        });

        // 2. Переходим на страницу
        page.navigate("https://the-internet.herokuapp.com/login");

        // 3. Заполняем форму
        //Ваш код...
        page.fill("input[name='username']", "tomsmith");
        page.fill("input[name='password']", "SuperSecretPassword!");

        page.onRequest(request -> {
            System.out.println("Request URL: " + request.url());
            System.out.println("Request Method: " + request.method());
            System.out.println("Request PostData: " + request.postData());
            System.out.println("Is Navigation Request: " + request.isNavigationRequest());
        });

        // 4. Нажимаем кнопку
        page.click("button[type='submit']");
        System.out.println("Нажали кнопку");

        // 5. Ждем и проверяем результат
        //Ваш код...
        assertThat(page.locator("#flash")).containsText("Your username is invalid!");
    }

    @AfterAll
    static void tearDownAll() {
        browser.close();
        playwright.close();
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

}
package auto;

import com.github.javafaker.Faker;
import com.microsoft.playwright.*;
import org.junit.jupiter.api.Test;

public class FakerGenerTest {
    @Test
    void mainTest() {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));

            //Ваш код
            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            // Генерация данных
            //Ваш код
            Faker faker = new Faker();
            String fakeName = faker.name().fullName();

            // Мокирование API
            page.route("**/dynamic_content", route -> {
                route.fulfill(new Route.FulfillOptions()
                        //Ваш код
                        .setStatus(200)
                        .setBody(fakeName)
                );
            });

            // Запуск теста
            page.navigate("https://the-internet.herokuapp.com/dynamic_content");
            //Ваш код
            assert page.textContent("pre").equals(fakeName);
        }
    }
}

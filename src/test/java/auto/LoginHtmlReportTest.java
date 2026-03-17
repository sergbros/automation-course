package auto;

import base.BaseTest;
import base.ThymeleafReportGenerator;
import com.microsoft.playwright.*;
import io.qameta.allure.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class LoginHtmlReportTest extends BaseTest {

    @Test
    public void testLogin() {
        page.navigate("https://the-internet.herokuapp.com/login");
        page.locator("#username").fill("tomsmith");
        page.locator("#password").fill("SuperSecretPassword!");
        page.locator("button[type='submit']").click();
        assertTrue(page.locator(".flash.success").isVisible());
    }

}
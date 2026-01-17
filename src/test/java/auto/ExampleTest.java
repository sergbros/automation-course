package auto;

import io.qameta.allure.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExampleTest {
    @Feature("Тестирование основного функционала")
    @Story("Проверка базового сценария")
    @Description("Пример простейшего теста с Allure-отчетностью.")
    @Severity(SeverityLevel.NORMAL)
    @Test
    void dummyTest() {
        assertTrue(true);
    }
}
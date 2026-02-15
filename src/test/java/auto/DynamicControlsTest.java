package auto;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class DynamicControlsTest {
    Playwright playwright;
    Browser browser;
    Page page;

    @BeforeEach
    void setUp() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false)  // Для визуального наблюдения
                .setSlowMo(500));     // Замедление для наглядности
        page = browser.newPage();
        page.setViewportSize(1280, 720);
    }

    @Test
    void testDynamicCheckbox() {
        // Навигация на страницу
        page.navigate("https://the-internet.herokuapp.com/dynamic_controls");

        // Ожидаем загрузки страницы
        page.waitForSelector("#checkbox", new Page.WaitForSelectorOptions()
                .setTimeout(5000));

        // 1. Находим чекбокс с атрибутом type="checkbox"
        Locator checkbox = page.locator("input[type='checkbox']");
        assertTrue(checkbox.isVisible(), "Checkbox should be visible initially");
        assertTrue(checkbox.isEnabled(), "Checkbox should be enabled initially");

        // Сохраняем начальное состояние (для демонстрации)
        System.out.println("Initial checkbox state: visible");

        // 2. Кликаем на кнопку "Remove"
        Locator removeButton = page.locator("button:has-text('Remove')");
        assertTrue(removeButton.isVisible(), "Remove button should be visible");
        removeButton.click();

        // 3. Ожидаем исчезновения чекбокса
        checkbox.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
        // Проверяем, что чекбокс действительно исчез
        assertFalse(checkbox.isVisible(), "Checkbox should be removed");

        // 4. Проверяем, что появляется текст "It's gone!"
        Locator goneMessage = page.locator("#message");
        goneMessage.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        assertTrue(goneMessage.isVisible(), "Message should be visible");

        String messageText = goneMessage.textContent();
        assertEquals("It's gone!", messageText.trim(),
                "Message text should be 'It's gone!'");

        System.out.println("After Remove: " + messageText);

        // 5. Кликаем на кнопку "Add"
        Locator addButton = page.locator("button:has-text('Add')");
        assertTrue(addButton.isVisible(), "Add button should be visible");
        addButton.click();

        // 6. Проверяем, что чекбокс снова отображается
        // Ожидаем появления чекбокса
        page.waitForSelector("input[type='checkbox']", new Page.WaitForSelectorOptions()
                .setTimeout(5000));

        // Пересоздаем локатор чекбокса
        checkbox = page.locator("input[type='checkbox']");
        assertTrue(checkbox.isVisible(), "Checkbox should be visible again");
        assertTrue(checkbox.isEnabled(), "Checkbox should be enabled again");

        // Дополнительно проверяем сообщение о добавлении
        Locator addMessage = page.locator("#message");
        assertTrue(addMessage.isVisible(), "Message should be visible after add");
        assertEquals("It's back!", addMessage.textContent().trim(),
                "Message text should be 'It's back!'");

        System.out.println("After Add: " + addMessage.textContent());

        // Финальная проверка - чекбокс должен быть интерактивным
        checkbox.check();
        assertTrue(checkbox.isChecked(), "Checkbox should be checkable");

        System.out.println("Test completed successfully!");
    }

    @AfterEach
    void tearDown() {
        if (page != null) {
            page.close();
        }
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }
}
package auto;

import base.TestContext;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class DynamicControlsTest {
    private TestContext context;
    private DynamicControlsPage controlsPage;

    @BeforeEach
    public void setup() {
        context = new TestContext();
        controlsPage = new DynamicControlsPage(context.getPage());
        context.getPage().navigate("https://the-internet.herokuapp.com/dynamic_controls");
    }

    @AfterEach
    public void teardown() {
        context.getPage().close();
    }

    @Test
    void testDynamicCheckbox() {
        // Ожидаем загрузки страницы
        controlsPage.getPage().waitForSelector("#checkbox", new Page.WaitForSelectorOptions()
                .setTimeout(5000));

        // 1. Находим чекбокс с атрибутом type="checkbox"
        Locator checkbox = controlsPage.getPage().locator("input[type='checkbox']");
        assertTrue(checkbox.isVisible(), "Checkbox should be visible initially");
        assertTrue(checkbox.isEnabled(), "Checkbox should be enabled initially");

        // Сохраняем начальное состояние (для демонстрации)
        System.out.println("Initial checkbox state: visible");

        // 2. Кликаем на кнопку "Remove"
        Locator removeButton = controlsPage.getPage().locator("button:has-text('Remove')");
        assertTrue(removeButton.isVisible(), "Remove button should be visible");
        removeButton.click();

        // 3. Ожидаем исчезновения чекбокса
        checkbox.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
        // Проверяем, что чекбокс действительно исчез
        assertFalse(checkbox.isVisible(), "Checkbox should be removed");

        // 4. Проверяем, что появляется текст "It's gone!"
        Locator goneMessage = controlsPage.getPage().locator("#message");
        goneMessage.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        assertTrue(goneMessage.isVisible(), "Message should be visible");

        String messageText = goneMessage.textContent();
        assertEquals("It's gone!", messageText.trim(),
                "Message text should be 'It's gone!'");

        System.out.println("After Remove: " + messageText);

        // 5. Кликаем на кнопку "Add"
        Locator addButton = controlsPage.getPage().locator("button:has-text('Add')");
        assertTrue(addButton.isVisible(), "Add button should be visible");
        addButton.click();

        // 6. Проверяем, что чекбокс снова отображается
        // Ожидаем появления чекбокса
        controlsPage.getPage().waitForSelector("input[type='checkbox']", new Page.WaitForSelectorOptions()
                .setTimeout(5000));

        // Пересоздаем локатор чекбокса
        checkbox = controlsPage.getPage().locator("input[type='checkbox']");
        assertTrue(checkbox.isVisible(), "Checkbox should be visible again");
        assertTrue(checkbox.isEnabled(), "Checkbox should be enabled again");

        // Дополнительно проверяем сообщение о добавлении
        Locator addMessage = controlsPage.getPage().locator("#message");
        assertTrue(addMessage.isVisible(), "Message should be visible after add");
        assertEquals("It's back!", addMessage.textContent().trim(),
                "Message text should be 'It's back!'");

        System.out.println("After Add: " + addMessage.textContent());

        // Финальная проверка - чекбокс должен быть интерактивным
        checkbox.check();
        assertTrue(checkbox.isChecked(), "Checkbox should be checkable");

        System.out.println("Test completed successfully!");

        context.getPage().close();
    }
}
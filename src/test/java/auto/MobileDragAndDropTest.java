package auto;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MobileDragAndDropTest {
    Playwright playwright;
    Browser browser;
    //Код...
    BrowserContext context;
    Page page;

    @BeforeEach
    void setup() {
        playwright = Playwright.create();

        // Ручная настройка параметров Samsung Galaxy S22 Ultra
        Browser.NewContextOptions deviceOptions = new Browser.NewContextOptions()
                .setUserAgent("Mozilla/5.0 (Linux; Android 12; SM-S908B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.0.0 Mobile Safari/537.36")
                .setViewportSize(384, 873)  // Разрешение экрана
                .setDeviceScaleFactor(3.5)
                .setIsMobile(true)
                .setHasTouch(true);

        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        context = browser.newContext(deviceOptions);
        page = context.newPage();
    }

    @Test
    void testDragAndDropMobile() throws InterruptedException {
        page.navigate("https://the-internet.herokuapp.com/drag_and_drop");

        Locator columnA = page.locator("#column-a");
        Locator columnB = page.locator("#column-b");

        // 1. Проверка начального состояния
        //Ваш код..
        System.out.println("Начальные проверки.");
        String initialTextColumnB = columnB.locator("header").textContent().trim();
        assertEquals("B", initialTextColumnB, "Начальный текст в колонке B должен быть 'B'");

        String initialTextColumnA = columnA.locator("header").textContent().trim();
        assertEquals("A", initialTextColumnA, "Начальный текст в колонке A должен быть 'A'");

        // 2. Перетаскивание через JS
        System.out.println("Перетаскивание.");
//        page.evaluate("() => {\n" +
//                        "  const dataTransfer = new DataTransfer();\n" +
//                        //"  const event = new DragEvent('drop', { dataTransfer });\n" +
//                "  const event = new DragEvent('drop', { dataTransfer, bubbles: true });\n" +
//                //Ваш код...
//                "  document.querySelector('#column-b').dispatchEvent(event);\n" +
//                "}");

        // Подготавливаем JavaScript-код для переноса элемента A поверх элемента B
        String script = """
        (() => {
            const source = document.querySelector('#column-a');
            const target = document.querySelector('#column-b');

            const dataTransfer = new DataTransfer();

            // Имитируем начало перетаскивания
            source.dispatchEvent(new MouseEvent('mousedown', { bubbles: true }));
            source.dispatchEvent(new DragEvent('dragstart', { dataTransfer, bubbles: true }));

            // Имитируем завершение перетаскивания над целью
            target.dispatchEvent(new DragEvent('dragenter', { dataTransfer, bubbles: true }));
            target.dispatchEvent(new DragEvent('dragover', { dataTransfer, bubbles: true }));
            target.dispatchEvent(new DragEvent('drop', { dataTransfer, bubbles: true }));

            // Завершаем операцию перетаскивания
            source.dispatchEvent(new DragEvent('dragend', { dataTransfer, bubbles: true }));
        })();
        """;

        page.evaluate(script);

        Thread.sleep(1000); // Дождёмся окончания анимации перед проверкой результата

                // 3. Ожидание и проверка
                //Ваш код...
        // Ждем изменения текста
        System.out.println("Ждём изменений.");
        columnB.locator("header").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        // Проверяем, что текст в зоне B изменился на "A"
        String finalTextColumnB = columnB.locator("header").textContent().trim();
        assertEquals("A", finalTextColumnB, "Текст в колонке B должен быть 'A' после перетаскивания");

        // Дополнительная проверка, что текст в колонке A изменился на "B"
        String finalTextColumnA = columnA.locator("header").textContent().trim();
        assertEquals("B", finalTextColumnA, "Текст в колонке A должен быть 'B' после перетаскивания");

        System.out.println("Тест успешно пройден: элемент A перетащен в зону B");
    }

    @AfterEach
    void tearDown() {
        //код...
        // Закрываем ресурсы
        if (page != null) page.close();
        if (context != null) context.close();
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
}

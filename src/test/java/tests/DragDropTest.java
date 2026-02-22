package tests;

import base.BaseTest;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Test;
import pages.DragDropPage;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Тест
public class DragDropTest extends BaseTest {

    @Test
    public void testDragAndDrop() {
        DragDropPage dragDropPage = new DragDropPage(page);
        dragDropPage.navigateTo("/drag_and_drop");
        dragDropPage.dragDropArea().dragAToB();
        assertEquals("A", dragDropPage.dragDropArea().getTextB());
    }
}
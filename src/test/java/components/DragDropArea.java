package components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

public class DragDropArea {
    private final Page page;

    public DragDropArea(Page page) {
        this.page = page;
    }

    public void dragAToB() {
        Locator elementA = page.locator("#column-a");
        Locator elementB = page.locator("#column-b");
        elementA.dragTo(elementB);
    }

    public String getTextB() {
        return page.locator("#column-b").textContent();
    }
}
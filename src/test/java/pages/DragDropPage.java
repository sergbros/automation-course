package pages;

import com.microsoft.playwright.Page;
import components.DragDropArea;

public class DragDropPage extends BasePage {
    private DragDropArea dragDropArea;

    public DragDropPage(Page page) {
        super(page);
    }

    public DragDropArea dragDropArea() {
        if (dragDropArea == null) {
            dragDropArea = new DragDropArea(page);
        }
        return dragDropArea;
    }
}
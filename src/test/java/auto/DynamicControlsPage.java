package auto;

import com.microsoft.playwright.Page;
import lombok.Getter;

@Getter
public class DynamicControlsPage {
    private final Page page;

    public DynamicControlsPage(Page page) {
        this.page = page;
    }
}

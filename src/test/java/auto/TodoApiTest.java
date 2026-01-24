package auto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TodoApiTest {
    Playwright playwright;
    APIRequestContext requestContext;
    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        playwright = Playwright.create();
        requestContext = playwright.request().newContext(
                new APIRequest.NewContextOptions()
                        .setBaseURL("https://jsonplaceholder.typicode.com")
        );
    }

    @Test
    void testTodoApi() throws Exception {
        APIResponse response = requestContext.get("/todos/1");

        assertEquals(200, response.status());

        String responseBody = response.text();
        Map<String, Object> todo = objectMapper.readValue(responseBody, Map.class);

        assertTrue(todo.containsKey("userId"), "Response should contain 'userId' field");
        assertTrue(todo.containsKey("id"), "Response should contain 'id' field");
        assertTrue(todo.containsKey("title"), "Response should contain 'title' field");
        assertTrue(todo.containsKey("completed"), "Response should contain 'completed' field");
    }

    @AfterEach
    void tearDown() {
        requestContext.dispose();
        playwright.close();
    }
}

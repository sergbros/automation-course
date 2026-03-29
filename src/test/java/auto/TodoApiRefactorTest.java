package auto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TodoApiRefactorTest {
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
    void testTodoApi1() throws Exception {
        mainTestMethod("userId");
    }

    @Test
    void testTodoApi2() throws Exception {
        mainTestMethod("id");
    }

    @Test
    void testTodoApi3() throws Exception {
        mainTestMethod("title");
    }

    @Test
    void testTodoApi4() throws Exception {
        mainTestMethod("completed");
    }

    void mainTestMethod(String testContsinsKey) throws JsonProcessingException {
        APIResponse response = requestContext.get("/todos/1");

        assertEquals(200, response.status());

        String responseBody = response.text();
        Map<String, Object> todo = objectMapper.readValue(responseBody, Map.class);

        assertTrue(todo.containsKey(testContsinsKey), "Response should contain '" + testContsinsKey + "' field");
    }

    @AfterEach
    void tearDown() {
        requestContext.dispose();
        playwright.close();
    }
}

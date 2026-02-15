package auto;

import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.FormData;
import com.microsoft.playwright.options.RequestOptions;
import org.junit.jupiter.api.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

public class FileUploadTest {
    private static Playwright playwright;
    private static APIRequestContext request;
    private Path tempFile;
    private byte[] originalImageBytes;

    @BeforeAll
    static void setUp() {
        playwright = Playwright.create();
        APIRequest.NewContextOptions options = new APIRequest.NewContextOptions()
                .setBaseURL("https://httpbin.org");
        request = playwright.request().newContext(options);
    }

    @AfterAll
    static void tearDown() {
        if (playwright != null) {
            playwright.close();
        }
    }

    @BeforeEach
    void init() throws IOException {
        // Генерация тестового PNG-файла в памяти
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.RED);
        graphics.fillRect(0, 0, 100, 100);
        graphics.setColor(Color.BLUE);
        graphics.drawString("Test", 30, 50);
        graphics.dispose();

        // Сохраняем во временный файл
        tempFile = Files.createTempFile("test-image", ".png");
        ImageIO.write(image, "png", tempFile.toFile());
        originalImageBytes = Files.readAllBytes(tempFile);
    }

    @AfterEach
    void cleanup() throws IOException {
        if (tempFile != null) {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testFileUploadAndDownload() {
        // Загрузка файла с обработкой исключений
        APIResponse uploadResponse = null;
        try {
            uploadResponse = request.post(
                    "https://httpbin.org/post",
                    RequestOptions.create().setMultipart(
                            FormData.create().set("file", tempFile.toFile().toPath())
                    )
            );

            assertEquals(200, uploadResponse.status(), "Upload failed");

            // Проверка получения файла
            String responseBody = uploadResponse.text();
            assertTrue(responseBody.contains("data:image/png;base64"),
                    "Response doesn't contain base64 data");

            // Извлечение и верификация содержимого
            String base64Data = extractBase64FromResponse(responseBody);
            byte[] receivedBytes = Base64.getDecoder().decode(base64Data);

            // Сравнение массивов байтов для проверки целостности
            assertArrayEquals(originalImageBytes, receivedBytes,
                    "Uploaded file content doesn't match original");

        } catch (Exception e) {
            fail("Upload failed with exception: " + e.getMessage());
        } finally {
            if (uploadResponse != null) {
                uploadResponse.dispose();
            }
        }

        // Скачивание и проверка эталона
        APIResponse downloadResponse = null;
        try {
            downloadResponse = request.get("https://httpbin.org/image/png");
            assertEquals(200, downloadResponse.status(), "Download failed");

            // Проверка MIME-типа
            String contentType = downloadResponse.headers().get("content-type");
            assertNotNull(contentType, "Content-Type header is missing");
            assertTrue(contentType.contains("image/png"),
                    "Invalid MIME type: " + contentType);

            // Проверка сигнатуры PNG
            byte[] content = downloadResponse.body();
            validatePngSignature(content);

            // Дополнительная проверка - возможность чтения как изображения
            validatePngImage(content);

        } catch (Exception e) {
            fail("Download validation failed: " + e.getMessage());
        } finally {
            if (downloadResponse != null) {
                downloadResponse.dispose();
            }
        }
    }

    private String extractBase64FromResponse(String responseBody) {
        try {
            // Парсинг JSON ответа для извлечения base64 данных
            // Формат ответа httpbin.org: {"files": {"file": "data:image/png;base64,..."}}
            String[] parts = responseBody.split("\"file\": \"");
            if (parts.length < 2) {
                fail("File data not found in response");
            }
            String base64Part = parts[1].split("\"")[0];

            // Удаляем префикс data:image/png;base64, если есть
            if (base64Part.contains(",")) {
                return base64Part.split(",")[1];
            }
            return base64Part;
        } catch (Exception e) {
            fail("Failed to extract base64 data: " + e.getMessage());
            return null;
        }
    }

    private void validatePngSignature(byte[] content) {
        assertTrue(content.length >= 8, "File too small to be valid PNG");

        // Проверка PNG сигнатуры (первые 8 байт)
        byte[] pngSignature = new byte[]{
                (byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
        };

        for (int i = 0; i < 8; i++) {
            assertEquals(pngSignature[i] & 0xFF, content[i] & 0xFF,
                    "Invalid PNG signature at byte " + i);
        }

        // Дополнительная проверка IHDR chunk
        if (content.length > 16) {
            // Проверка, что после сигнатуры идет IHDR chunk (первые 4 байта данных - длина, следующие 4 - "IHDR")
            assertEquals('I', content[12]);
            assertEquals('H', content[13]);
            assertEquals('D', content[14]);
            assertEquals('R', content[15]);
        }
    }

    private void validatePngImage(byte[] content) throws IOException {
        // Проверка, что данные можно прочитать как PNG изображение
        try (ByteArrayInputStream bais = new ByteArrayInputStream(content)) {
            BufferedImage image = ImageIO.read(bais);
            assertNotNull(image, "Content is not a valid PNG image");
            assertTrue(image.getWidth() > 0, "Image has invalid width");
            assertTrue(image.getHeight() > 0, "Image has invalid height");
        }
    }
}

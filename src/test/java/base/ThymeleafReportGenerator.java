package base;

import junit.framework.TestResult;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.FileWriter;
import java.util.List;

public class ThymeleafReportGenerator {
    public static void generate(List<TestResult> results, String outputPath) {
        // Настройка Thymeleaf
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("/templates/");
        resolver.setSuffix(".html");

        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);

        // Заполнение данных
        Context context = new Context();
        context.setVariable("results", results);

        // Генерация HTML
        try (FileWriter writer = new FileWriter(outputPath)) {
            engine.process("report", context, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

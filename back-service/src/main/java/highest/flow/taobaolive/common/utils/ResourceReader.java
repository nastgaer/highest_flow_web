package highest.flow.taobaolive.common.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.stream.Collectors;

@Configuration
public class ResourceReader {

    public static String getProductCategoryList() throws IOException {
        final InputStream resource = new ClassPathResource("static/ProductCategoryList.json").getInputStream();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource))) {
            final String text = reader.lines()
                    .collect(Collectors.joining("\n"));
            return text;
        }
    }

    public static String getLiveColumnList() throws IOException {
        final InputStream resource = new ClassPathResource("static/LiveColumnList.json").getInputStream();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource))) {
            final String text = reader.lines()
                    .collect(Collectors.joining("\n"));
            return text;
        }
    }
}

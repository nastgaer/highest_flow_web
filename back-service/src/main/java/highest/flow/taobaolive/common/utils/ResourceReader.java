package highest.flow.taobaolive.common.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;

@Configuration
public class ResourceReader {

    @Value("classpath:static/ProductCategoryList.json")
    private static Resource productCategoryResource;

    @Value("classpath:static/LiveColumnList.json")
    private static Resource liveColumnResource;

    public static String getResourceAsString(Resource resource) throws IOException {
        String list = new String(Files.readAllBytes(productCategoryResource.getFile().toPath()));
        return list;
    }

    public static String getProductCategoryList() throws IOException {
        return getResourceAsString(productCategoryResource);
    }

    public static String getLiveColumnList() throws IOException {
        return getResourceAsString(liveColumnResource);
    }
}

package utils;

import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringValueResolver;

@Component
public class PropertiesReader implements EmbeddedValueResolverAware {

    private static StringValueResolver stringValueResolver;

    @Override
    public void setEmbeddedValueResolver(StringValueResolver stringValueResolver) {
        //上下文注入的是EmbeddedValueResolver
        PropertiesReader.stringValueResolver = stringValueResolver;
    }

    public static String getProperValue(String name) {
        return stringValueResolver.resolveStringValue("${" + name + "}");
    }
}

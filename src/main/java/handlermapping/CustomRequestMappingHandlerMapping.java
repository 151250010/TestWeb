package handlermapping;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import utils.RequestPathUtil;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CustomRequestMappingHandlerMapping implements HandlerMapping,InitializingBean,ServletContextAware,ApplicationContextAware,Ordered{

    private final Log log = LogFactory.getLog(getClass());
    private static final String SCOPED_TARGET_PREFIX = "scopedTarget.";
    private WebApplicationContext webApplicationContext;
    private ServletContext servletContext;
    private int order = 0; //default having the highest priority

    private final Map<CustomRequestMappingInfo, HandlerMethod> mappingLookup = new LinkedHashMap<>(); //用于根据mappingInfo 找到handlerMethod
    private final MultiValueMap<String, CustomRequestMappingInfo> urlLookup = new LinkedMultiValueMap<>(); //用于根据url找到 handlerMethod ，简单的使用第一个value

    @Override
    public void afterPropertiesSet() throws Exception {
        initHandlerMethods();
    }

    private void initHandlerMethods() {
        if (webApplicationContext != null) {
            String[] beanNames = getWebApplicationContext().getBeanNamesForType(Object.class); // 从当前的web上下文获取所有的bean names

            for (String beanName : beanNames) {
                if (!beanName.startsWith(SCOPED_TARGET_PREFIX)) {
                    Class<?> beanType = null;
                    try {
                        beanType = getWebApplicationContext().getType(beanName);
                    } catch (Throwable throwable) {
                        log.debug("Could not resolve target class for bean with name '" + beanName + "'");
                    }

                    if (beanType != null && isHandler(beanType)) {
                        log.debug("Begin to detect all the matched methods of bean with name '" + beanName + "'");
                        detectHandlerMethods(beanName);
                    }
                }
            }
        }
    }

    private void detectHandlerMethods(String beanName) {
        Class<?> handlerType = getWebApplicationContext().getType(beanName);
        if (handlerType != null) {
            Map<Method, CustomRequestMappingInfo> methodCustomRequestMappingInfoMap = selectMethods(handlerType, method -> getMappingForMethod(method, handlerType));
            methodCustomRequestMappingInfoMap.forEach(((method, customRequestMappingInfo) -> registerMappings(beanName, method, customRequestMappingInfo)));
        }
    }

    private void registerMappings(String beanName, Method method, CustomRequestMappingInfo customRequestMappingInfo) {
        HandlerMethod handlerMethod = new HandlerMethod(beanName, getWebApplicationContext().getAutowireCapableBeanFactory(), method);
        this.mappingLookup.put(customRequestMappingInfo, handlerMethod);
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        List<String> directPath = new ArrayList<>(customRequestMappingInfo.getPatternRequestCondition().getContent())
                .stream()
                .filter(pattern -> !antPathMatcher.isPattern(pattern))
                .collect(Collectors.toList());

        directPath.forEach(path -> this.urlLookup.add(path, customRequestMappingInfo));
        log.info("Register a handler method [" + handlerMethod.toString() + "]" + " to request mapping info [" + customRequestMappingInfo.toString() + "]");
    }

    /**
     * create all mapped CustomRequestMappingInfo of certain target type
     * ignore proxy class
     * and assume targetType is a concrete controller without super class
     * @param targetType
     * @param inspect
     * @return
     */
    private Map<Method, CustomRequestMappingInfo> selectMethods(Class<?> targetType, Function<Method, CustomRequestMappingInfo> inspect) {

        final Map<Method, CustomRequestMappingInfo> methodCustomRequestMappingInfoMap = new LinkedHashMap<>();
        Method[] declaredMethods = targetType.getDeclaredMethods(); // 不讨论aop代理的
        for (Method declaredMethod : declaredMethods) {
            if (declaredMethod.isAnnotationPresent(RequestMapping.class)) {
                // 有RequestMapping 注解
                methodCustomRequestMappingInfoMap.put(declaredMethod, inspect.apply(declaredMethod));
            }
        }
        return methodCustomRequestMappingInfoMap;
    }

    private boolean isHandler(Class<?> beanType){
        return beanType.isAnnotationPresent(RequestMapping.class) && beanType.isAnnotationPresent(Controller.class);
    }

    /**
     * 使用RequestMapping的属性 创建CustomRequestMappingInfo
     * @param method
     * @param handlerType
     * @return
     */
    private CustomRequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
        CustomRequestMappingInfo methodInfo = createMappingInfo(method);
        if (methodInfo != null) {
            CustomRequestMappingInfo classInfo = createMappingInfo(handlerType);
            if (classInfo != null) {
                methodInfo = classInfo.combine(methodInfo);
            }
        }
        return methodInfo;
    }

    private CustomRequestMappingInfo createMappingInfo(AnnotatedElement element) {
        RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(element, RequestMapping.class); //获取RequestMapping实例
        if (requestMapping == null) {

        }
        return CustomRequestMappingInfo
                .paths(requestMapping.path())
                .methods(requestMapping.method())
                .mappingName(requestMapping.name())
                .build();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.webApplicationContext = applicationContext instanceof WebApplicationContext ? (WebApplicationContext) applicationContext : null;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
        String lookupPath = new RequestPathUtil().lookupRequestPath(request);
        HandlerMethod handlerMethod = lookupHandlerMethod(lookupPath, request);
        if (handlerMethod != null) {
            handlerMethod = handlerMethod.createWithResolvedBean();
            return new HandlerExecutionChain(handlerMethod); //没有拦截器
        }
        return null;
    }

    private HandlerMethod lookupHandlerMethod(String lookupPath, HttpServletRequest request) throws Exception {
        List<CustomRequestMappingInfo> directMatches = urlLookup.get(lookupPath); //获取所有匹配的直接路径
        List<HandlerMethod> matchMethod = new ArrayList<>();

        if (directMatches != null) {
            addMatchMethods(directMatches, matchMethod, request);
        }
        if (matchMethod.isEmpty()) {
            addMatchMethods(this.mappingLookup.keySet(), matchMethod, request);
        }

        if (!matchMethod.isEmpty()) {
            request.setAttribute("HandlerMapping.pathWithinHandlerMapping", lookupPath);
            return matchMethod.get(0);
        }

        return null;
    }

    private void addMatchMethods(Collection<CustomRequestMappingInfo> mappingInfos, List<HandlerMethod> matchMethod, HttpServletRequest request) {
        mappingInfos.forEach(customRequestMappingInfo -> {
            CustomRequestMappingInfo mappingInfo = customRequestMappingInfo.getMatchingCondition(request);
            if (mappingInfo != null) {
                matchMethod.add(this.mappingLookup.get(customRequestMappingInfo));
            }
        });
    }

    protected void setOrder(int order) {
        this.order = order;
    }

    public WebApplicationContext getWebApplicationContext() {
        return webApplicationContext;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }
}

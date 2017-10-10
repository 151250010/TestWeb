package dispatcherservlet;

import handlermapping.CustomRequestMappingHandlerMapping;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.*;
import org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import utils.RequestPathUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

public class CustomDispatcherServlet extends FrameworkServlet{

    private List<HandlerMapping> handlerMappings;
    private List<HandlerAdapter> handlerAdapters;
    private ViewResolver viewResolver;

    private static final Log LOGGER = LogFactory.getLog(CustomDispatcherServlet.class);

    @Override
    protected void doService(HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOGGER.debug("[CustomDispatcherServlet]: I got a request --> " + request.toString());
        //这里必须设置为DispatcherServlet.CONTEXT view才可以进行渲染？具体的实现有空再看了
        request.setAttribute(DispatcherServlet.class.getName() + ".CONTEXT", getWebApplicationContext()); //设置上下文
        request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, new RequestPathUtil().lookupRequestPath(request));
        doDispatch(request, response);
    }

    /**
     * 进行请求的分发
     * @param request
     * @param response
     */
    private void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOGGER.debug("begin to dispatch request --> " + request.getRequestURL());

        //获取handlerExecutionChain
        HandlerExecutionChain executionChain = getHandlerExecutionChain(request);
        if (executionChain == null) {
            LOGGER.info("could not find an execution chain for the request -- > " + request.getRequestURL());
            return;
        }

        Object handler = executionChain.getHandler();
        if(handler == null){
            LOGGER.info("handler of execution is null!");
            return;
        }

        HandlerAdapter handlerAdapter = getHandlerAdapter(handler);
        if (handlerAdapter == null) {
            LOGGER.info("could not find a handler adapter for the request");
            return;
        }

        ModelAndView modelAndView = handlerAdapter.handle(request, response, handler);

        //进行视图的渲染
        render(modelAndView, request, response);
    }

    private void render(ModelAndView modelAndView, HttpServletRequest request, HttpServletResponse response) throws Exception {

        Locale locale = request.getLocale(); //直接从request中获取语言
        response.setLocale(locale);

        Optional<HttpStatus> status = Optional.ofNullable(modelAndView.getStatus());
        status.ifPresent(httpStatus -> response.setStatus(httpStatus.value()));

        String viewName = modelAndView.getViewName();
        View view;

        if (viewName != null) {
            //视图名字不为空的话，需要进行解析，直接用xml中配置的InternalViewResolver
            view = this.viewResolver.resolveViewName(viewName, locale);
        }else {
            view = modelAndView.getView();
        }

        try {
            view.render(modelAndView.getModel(), request, response); //进行视图的渲染
        } catch (Exception ex) {
            LOGGER.info("Error rendering view [" + view + "] in CustomDispatcherServlet with name '" + getServletName() + "'", ex);
            throw ex;
        }

        LOGGER.info("render successfully!");

    }

    private HandlerAdapter getHandlerAdapter(Object handler) {

        for (HandlerAdapter adapter : handlerAdapters) {
            if (adapter.supports(handler)) {
                return adapter;
            }
        }
        return null;
    }


    private HandlerExecutionChain getHandlerExecutionChain(HttpServletRequest request) {

        HandlerExecutionChain executionChain = null;
        for (HandlerMapping handlerMapping : handlerMappings) {
            try {
                executionChain = handlerMapping.getHandler(request);
            } catch (Exception e) {
                LOGGER.info("catch an exception when getting handler execution!");
            }

            if (executionChain != null) {
                return executionChain;
            }
        }

        return null;
    }

    @Override
    protected void onRefresh(ApplicationContext applicationContext) {
        LOGGER.info("[onRefresh] --> init method called?");
        LOGGER.info("refresh begins ---> init handlerMappings");

        initHandlerMappings(applicationContext);
        //init LocaleResolver,init MultipartResolver等一些其他的初始化工作先不初始化
        initHandlerAdapter(applicationContext);
        initViewResolver(applicationContext);
    }

    private void initViewResolver(ApplicationContext applicationContext) {
        //直接设置已知的解析器了
        this.viewResolver = applicationContext.getBean(InternalResourceViewResolver.class);
    }

    private void initHandlerAdapter(ApplicationContext applicationContext) {

        this.handlerAdapters = new ArrayList<>();
        //从根上下文获取handlerAdapters
        try{
            HandlerAdapter adapterOfRoot = applicationContext.getParent().getBean(HandlerAdapter.class);
            if (adapterOfRoot != null) {
                this.handlerAdapters.add(adapterOfRoot);
            }
        }catch (NullPointerException e){
            LOGGER.info("could not find a handler adapter from root application context!");
        }

        Map<String, HandlerAdapter> stringHandlerAdapterMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, HandlerAdapter.class, true, false);
        if (!stringHandlerAdapterMap.isEmpty()) {
            this.handlerAdapters.addAll(stringHandlerAdapterMap.values());
        }

        if (this.handlerAdapters.size() == 0) {
            handlerAdapters.add(new HttpRequestHandlerAdapter()); //支持静态资源处理
            handlerAdapters.add(new RequestMappingHandlerAdapter()); //支持@RequestMapping解析
            LOGGER.info("register a default request mapping handler adapter");
        }

        LOGGER.info("init handler adapter successfully!");
    }

    private void initHandlerMappings(ApplicationContext applicationContext) {

        //先从webapplicationContext中获取bean
        Map<String, HandlerMapping> stringHandlerMappingMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, HandlerMapping.class, true, false);
        this.handlerMappings = new ArrayList<>(stringHandlerMappingMap.values());

        if (handlerMappings.isEmpty()) {
            LOGGER.info("could not find a handler mapping instance from current application context!");
        }

        //我们可以试下从根上下文中获取HanlderMapping
        try {
            HandlerMapping handlerMappingOfRoot = applicationContext.getParent().getBean(HandlerMapping.class);
            if (handlerMappingOfRoot != null) {
                this.handlerMappings.add(handlerMappingOfRoot);
                LOGGER.info("find a handler mapping from root application context!");
            }
        } catch (NullPointerException handlerMappingOfRoot) {
            LOGGER.info("could not find a handler mapping instance from root application context!");
        }

    }

}

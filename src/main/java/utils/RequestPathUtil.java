package utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public class RequestPathUtil {

    private Log log = LogFactory.getLog(getClass());

    /**
     * get the path within servlet mapping or within the web application
     * @param request
     * @return
     */
    public String lookupRequestPath(HttpServletRequest request) {

        String pathWithinApplication = getPathWithinApplication(request);
        String servletPath = (String) Optional.ofNullable(request.getAttribute("javax.servlet.include.servlet_path"))
                .orElse(request.getServletPath()); // try to get attribute if null use request.getServletPath
        log.info("info of request [" + request.toString() + "] showed here: [servletPath] --> " + servletPath);

        if (pathWithinApplication.equals(servletPath)) {
            return pathWithinApplication;
        }
        return pathWithinApplication.substring(servletPath.length());
    }

    /**
     * http://localhost:8080/news/man/list.jsp
     * contextPath: /news
     * servletPath: /man/list.jsp
     * requestUri: /news/man/list.jsp
     *
     * 所以我们想返回的其实是servletPath
     */

    /**
     * get the path within application
     * @param request
     * @return
     */
    private String getPathWithinApplication(HttpServletRequest request) {
        //contextPath 是站点的根路径，也就是项目的名字
        String contextPath = request.getContextPath();
        // another choice? ----> contextPath = (String) request.getAttribute("javax.servlet.include.context_path");
        String requestUri = request.getRequestURI(); // It seems the result differs from the web container, but in tomcat is ok.

        String pathWithApp;
        int indexOfContext = requestUri.indexOf(contextPath);
        if (indexOfContext == -1) {
            pathWithApp = "/";
        }else {
            pathWithApp = requestUri.substring(contextPath.length());
        }
        log.info("info of request [" + request.toString() + "] showed here: [contextPath] --> " + contextPath + ", [requestUri] --> " + requestUri + ", [pathWithinApp] --> " + pathWithApp);
        return pathWithApp;
    }

}

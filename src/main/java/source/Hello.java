package source;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

@Controller
@RequestMapping("/test")
public class Hello {

    @RequestMapping(path = {"/sayhello"},name = "customTest",method = RequestMethod.GET)
    public String sayHello(HttpServletRequest request) {

        /**
         * 尝试一下获取tempdir
         *  测试修改
         */
//        getCurrentWebTempDir(request);

//        getPropertiesOfName("test.name");

//        urlPathHelperTest(request);

        System.out.println("Hello!This is my request content!");
        return "Hello";
    }

    /**
     * 看下UrlPathHelper 的处理结果
     * @param request
     */
    private void urlPathHelperTest(HttpServletRequest request) {

        /*UrlPathHelper urlPathHelper = new UrlPathHelper();
        System.out.println("urlPathHelper looks up path ----> " + urlPathHelper.getLookupPathForRequest(request));
        // result: urlPathHelper looks up path ----> /sayhello
        //直接输出请求路径*/

//        RequestPathUtil requestPathUtil = new RequestPathUtil();
//        System.out.println(requestPathUtil.lookupRequestPath(request));

    }

    private void getPropertiesOfName(String s) {
        //get the value of properties
//        System.out.println(PropertiesReader.getProperValue(s));
    }

    private void getCurrentWebTempDir(HttpServletRequest request) {
        //get the dir of current web application in the servlet container
        File file = (File) request.getServletContext().getAttribute("javax.servlet.context.tempdir");
        System.out.println(file.getAbsolutePath());
    }

//    @PostMapping("/sayok")
//    public String sayOk(){
//        //
//        System.out.println("OK");
//        return "Hello";
//    }
//
   /* @DeleteMapping("/username")
    public String getUserName(){
        System.out.println("Try to get user name");
        return "get user name successfully!";
    }*/

//   @GetMapping("/userinfo")
//    public String getUserInfo(@RequestParam String userId) {
//        return "Hello";
//    }
}

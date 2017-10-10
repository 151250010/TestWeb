package source.controllerAdvice;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = {"source"})
public class GlobalControllerAdvice {

    // Model Attribute 的其实主要的作用是添加模型属性 add Model Attribute
    // 在每次请求的时候都会被设置进去
   /* @ModelAttribute(value = "global controller advice")
    public String preHandle() {
        System.out.println("Request is getting pre handled!");
        return "This is the GLOBALCONTROLLERADVICE !";
    }*/
}

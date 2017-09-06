package source.dateFormat;

import dto.AnnotationDateFormatUser;
import dto.BindingInitializerUser;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
@RequestMapping("/date")
public class UserControllerForDateFormat {

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @ResponseBody
    @PostMapping("/dateTimeFormatAnnotation/login")
    public AnnotationDateFormatUser login(@RequestBody AnnotationDateFormatUser user) {
        System.out.println(user.getBirthday().toString());
        return user;
    }

    @ResponseBody
    @PostMapping("/bindingInitializers/login")
    public BindingInitializerUser login(@RequestBody BindingInitializerUser user) {
        System.out.println(user.getBirthday().toString());
        return user;
    }

    /**
     * 使用局部的转化器
     * @param webDataBinder
     */
   /* @InitBinder
    public void allowDateBinding(WebDataBinder webDataBinder) {
        webDataBinder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
    }*/
}

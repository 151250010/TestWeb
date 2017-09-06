package bindinginitializers;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.context.request.WebRequest;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MyWebBindingInitalizer implements WebBindingInitializer {

    @Override
    public void initBinder(WebDataBinder webDataBinder, WebRequest webRequest) {
        webDataBinder.registerCustomEditor(Date.class,new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd"),false));
    }
}

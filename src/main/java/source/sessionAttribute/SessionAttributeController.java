package source.sessionAttribute;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;
import java.util.Map;

@Controller
@RequestMapping("/session")
@SessionAttributes(names = {"userName","password"})
public class SessionAttributeController {

    @GetMapping("/alertMessage")
    @ResponseBody
    public String alertMessage(@SessionAttribute("userName") String userName, @SessionAttribute("password") String password, Model model, HttpSession session, HttpServletRequest request) {

        System.out.println("Successfully Alert Message of [userName] + {" + userName +
                "} , [password] + {" + password +
                "}");

        System.out.println("Model Values --> ");
        Map<String, ?> allAttributes = model.asMap();
        allAttributes.forEach((name, value) -> System.out.println(name + "=============" + value));

        System.out.println("Request Values --> ");
        Enumeration<String> requestNames = request.getAttributeNames();
        while (requestNames.hasMoreElements()) {
            String requestName = requestNames.nextElement();
            System.out.println(requestName + "=============" + request.getAttribute(requestName));
        }

        System.out.println("Request Scoped Session Values --> ");
        HttpSession requestSession = request.getSession();
        Enumeration<String> sessionAttributeNames = requestSession.getAttributeNames();
        while (sessionAttributeNames.hasMoreElements()) {
            String sessionAttributeName = sessionAttributeNames.nextElement();
            System.out.println(sessionAttributeName + "=============" + requestSession.getAttribute(sessionAttributeName));
        }

        printSessionAttributes(session);

        return "alert";
    }

    @GetMapping("/init")
    @ResponseBody
    public String initSessionMessage(ModelMap modelMap) {
        modelMap.addAttribute("userName", "Xihao");
        modelMap.addAttribute("password", "asd");
        return "init";
    }

    @GetMapping("/clear")
    @ResponseBody
    public String clearSessionMessage(SessionStatus sessionStatus, HttpSession session) {
        sessionStatus.setComplete();
        // 执行方法之后，Session 数据还没有进行清除
        printSessionAttributes(session);
        return "Clear Session Message Successfully!";
    }

    private void printSessionAttributes(HttpSession session) {
        System.out.println("Session Status --------------------------------------------------");
        Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String attribuetName = attributeNames.nextElement();
            System.out.println(attribuetName + "========" + session.getAttribute(attribuetName));
        }
    }

    @ModelAttribute("modelAttributeWarning")
    public String preHandleAndSetModelAttribute() {
        System.out.println("Inside of preHandleAndSetModelAttribute!");
        return "I am the model attribute !";
    }


}

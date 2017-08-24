package handlermapping;

import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.condition.AbstractRequestCondition;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

public class RequestMethodRequestCondition extends AbstractRequestCondition<RequestMethodRequestCondition>{

    private Set<RequestMethod> methods; // all the matching methods on handler method

    public RequestMethodRequestCondition(Set<RequestMethod> methods) {
        this.methods = methods;
    }

    public RequestMethodRequestCondition(List<RequestMethod> methods) {
        this.methods = Collections.unmodifiableSet(new LinkedHashSet<>(methods));
    }

    public RequestMethodRequestCondition(RequestMethod... methods) {
        this(Arrays.asList(methods));
    }

    public RequestMethodRequestCondition(){
        this.methods = new LinkedHashSet<>();
    }

    @Override
    protected Collection<?> getContent() {
        return this.methods;
    }

    @Override
    protected String getToStringInfix() {
        return "||";
    }

    @Override
    public RequestMethodRequestCondition combine(RequestMethodRequestCondition other) {
        Set<RequestMethod> methods = new LinkedHashSet<>(this.methods);
        methods.addAll(other.methods);
        return new RequestMethodRequestCondition(methods);
    }

    @Override
    public RequestMethodRequestCondition getMatchingCondition(HttpServletRequest request) {

        if (this.methods.isEmpty()) {
            return this;
        }

        String methodValue = request.getMethod();
        HttpMethod httpMethod = HttpMethod.resolve(methodValue);
        if (httpMethod != null) {
            for (RequestMethod method : this.methods) {
                if (httpMethod.matches(method.name())) {
                    return new RequestMethodRequestCondition(method);
                }
            }
            //default return get
            if (httpMethod == HttpMethod.HEAD && this.methods.contains(RequestMethod.GET)) {
                return new RequestMethodRequestCondition(RequestMethod.GET);
            }
        }

        return null;
    }

    @Override
    public int compareTo(RequestMethodRequestCondition other, HttpServletRequest request) {

        if (other.methods.size() != this.methods.size()) {
            return other.methods.size() - this.methods.size();
        } else if (this.methods.size() == 1) {
            if (this.methods.contains(RequestMethod.HEAD) && other.methods.contains(RequestMethod.GET)) {
                return -1;
            }else if (this.methods.contains(RequestMethod.GET) && other.methods.contains(RequestMethod.HEAD)){
                return 1;
            }
        }

        return 0;
    }
}

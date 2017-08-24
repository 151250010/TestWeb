package handlermapping;

import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.condition.RequestCondition;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

public class CustomRequestMappingInfo implements RequestCondition<CustomRequestMappingInfo> {

    private String name;
    private PatternRequestCondition patternRequestCondition;
    private RequestMethodRequestCondition methodRequestCondition;

    public CustomRequestMappingInfo(String name, PatternRequestCondition patternRequestCondition, RequestMethodRequestCondition methodRequestCondition) {
        this.name = name;
        this.patternRequestCondition = patternRequestCondition;
        this.methodRequestCondition = methodRequestCondition;
    }

    public CustomRequestMappingInfo(PatternRequestCondition patternRequestCondition, RequestMethodRequestCondition methodRequestCondition) {
        this.patternRequestCondition = patternRequestCondition;
        this.methodRequestCondition = methodRequestCondition;
    }

    @Override
    public CustomRequestMappingInfo combine(CustomRequestMappingInfo other) {
        String mappingName = combineName(other);
        PatternRequestCondition patternRequestCondition = this.patternRequestCondition.combine(other.patternRequestCondition);
        RequestMethodRequestCondition requestMethodRequestCondition = this.methodRequestCondition.combine(other.methodRequestCondition);
        return new CustomRequestMappingInfo(mappingName, patternRequestCondition, requestMethodRequestCondition);
    }

    private String combineName(CustomRequestMappingInfo other) {
        //use '#' to combine names
        if (this.name != null && other.name != null) {
            return this.name + '#' + other.name;
        } else if (this.name != null) {
            return this.name;
        }else {
            return other.name;
        }
    }


    @Override
    public CustomRequestMappingInfo getMatchingCondition(HttpServletRequest request) {
        RequestMethodRequestCondition methodRequestCondition = this.methodRequestCondition.getMatchingCondition(request);
        if (methodRequestCondition == null) {
            return null;
        }
        PatternRequestCondition patternRequestCondition = this.patternRequestCondition.getMatchingCondition(request);
        if (patternRequestCondition == null) {
            return null;
        }

        return new CustomRequestMappingInfo(this.name, patternRequestCondition, methodRequestCondition);
    }

    @Override
    public int compareTo(CustomRequestMappingInfo other, HttpServletRequest request) {

        int result;
        if (HttpMethod.HEAD.matches(request.getMethod())) {
            result = this.methodRequestCondition.compareTo(other.methodRequestCondition, request);
            if (result != 0) {
                return result;
            }
        }

        result = this.patternRequestCondition.compareTo(other.patternRequestCondition, request);
        if (result != 0) {
            return result;
        }

        result = this.methodRequestCondition.compareTo(other.methodRequestCondition, request);
        if (result != 0) {
            return result;
        }

        return 0;
    }

    public String getName() {
        return name;
    }

    public PatternRequestCondition getPatternRequestCondition() {
        return patternRequestCondition;
    }

    public RequestMethodRequestCondition getMethodRequestCondition() {
        return methodRequestCondition;
    }

    public static RequestMappingInfoBuilder paths(String... paths) {
        return new RequestMappingInfoBuilder(paths);
    }

    /**
     * a custom handler mapping info builder
     */
    static class RequestMappingInfoBuilder{
        private String[] paths = new String[0];
        private RequestMethod[] methods = new RequestMethod[0];
        private String mappingName;

        public RequestMappingInfoBuilder(String... paths) {
            this.paths = paths;
        }

        public RequestMappingInfoBuilder mappingName(String mappingName) {
            this.mappingName = mappingName;
            return this;
        }

        public RequestMappingInfoBuilder paths(String... paths) {
            this.paths = paths;
            return this;
        }

        public RequestMappingInfoBuilder methods(RequestMethod... methods) {
            this.methods = methods;
            return this;
        }

        public CustomRequestMappingInfo build(){
            return new CustomRequestMappingInfo(this.mappingName, new PatternRequestCondition(this.paths), new RequestMethodRequestCondition(this.methods));
        }
    }

    @Override
    public String toString() {
        return "CustomRequestMappingInfo{" +
                "name='" + name + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return (this.patternRequestCondition.hashCode() * 31 + this.methodRequestCondition.hashCode());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CustomRequestMappingInfo)) {
            return false;
        }

        CustomRequestMappingInfo customRequestMappingInfo = (CustomRequestMappingInfo) other;
        return (this.patternRequestCondition.equals(customRequestMappingInfo.patternRequestCondition))
                && (this.methodRequestCondition.equals(customRequestMappingInfo.methodRequestCondition));
    }

}

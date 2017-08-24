package handlermapping;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.mvc.condition.AbstractRequestCondition;
import utils.RequestPathUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 实现路径匹配的条件
 */
public class PatternRequestCondition extends AbstractRequestCondition<PatternRequestCondition>{

    private static final Log log = LogFactory.getLog(PatternRequestCondition.class);
    // ant 适配有点复杂，直接用spring自带的
    private AntPathMatcher antPathMatcher = new AntPathMatcher();
    private Set<String> patterns;
    private RequestPathUtil requestPathUtil = new RequestPathUtil(); //请求路径处理类

    public PatternRequestCondition(Set<String> patterns) {
        this.patterns = patterns;
    }

    public PatternRequestCondition(String... patterns) {
        List<String> patternList = Arrays.asList(patterns);
        this.patterns = Collections.unmodifiableSet(checkLeadingSlash(patternList));
    }

    public PatternRequestCondition(){
        this.patterns = new LinkedHashSet<>();
    }

    /**
     * 所有的pattern都加上"/"
     * @param patternList
     * @return
     */
    private Set<String> checkLeadingSlash(List<String> patternList) {
        Set<String> result = new LinkedHashSet<>(patternList.size());
        for (String pattern : patternList) {
            if (!"".equals(pattern) && pattern != null && !pattern.startsWith("/")) {
                pattern = "/" + pattern;
            }
            result.add(pattern);
        }
        return result;
    }

    @Override
    protected Collection<String> getContent() {
        return this.patterns;
    }

    @Override
    protected String getToStringInfix() {
        return "||";
    }

    @Override
    public PatternRequestCondition combine(PatternRequestCondition other) {

        Set<String> result = new LinkedHashSet<>();
        if (!this.patterns.isEmpty() && !other.patterns.isEmpty()) {
            for (String pattern1 : this.patterns) {
                for (String pattern2 : other.patterns) {
                    result.add(this.antPathMatcher.combine(pattern1, pattern2));
                }
            }
        } else if (!this.patterns.isEmpty()) {
            result.addAll(this.patterns);
        } else if (!other.patterns.isEmpty()) {
            result.addAll(other.patterns);
        }else {
            result.add("");
        }
        return new PatternRequestCondition(result);
    }

    @Override
    public PatternRequestCondition getMatchingCondition(HttpServletRequest request) {

        log.info("Try to get the matching condition for [HttpServletRequest] [" + request.toString() + "]");
        // if patterns are null
        if (this.patterns == null) {
            return this;
        }

        String lookupPath = this.requestPathUtil.lookupRequestPath(request); //获得请求路径(相对于contextPath的路径)
        List<String> matches = getMatchingPatterns(lookupPath);

        return matches.isEmpty() ? null : new PatternRequestCondition(matches.toArray(new String[matches.size()]));
    }

    private List<String> getMatchingPatterns(String lookupPath) {
        List<String> matches = new ArrayList<>();
        for (String pattern : this.patterns) {
            String match = getMatchingPatterns(pattern, lookupPath);
            if (match != null) {
                matches.add(match);
            }
        }
        matches.sort(this.antPathMatcher.getPatternComparator(lookupPath));
        return matches;
    }

    private String getMatchingPatterns(String pattern, String lookupPath) {
        if (pattern.equals(lookupPath)) {
            return pattern;
        }
        if (this.antPathMatcher.match(pattern, lookupPath)) {
            return pattern;
        }
        if (!pattern.endsWith("/") && this.antPathMatcher.match(pattern + "/", lookupPath)) {
            return pattern + "/";
        }
        return null;
    }

    @Override
    public int compareTo(PatternRequestCondition other, HttpServletRequest request) {
        return 0;
    }
}

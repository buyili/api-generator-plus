package site.forgus.plugins.apigeneratorplus.model;

import org.apache.commons.lang.StringUtils;
import org.eclipse.xtend.lib.annotations.Data;

import java.util.*;

/**
 * @author lmx 2020/11/19 14:32
 */
@Data
public class FilterFieldInfo {

    public String canonicalClassName = "";

    public String includeFiled = "";

    public String excludeField = "";

    public boolean excludeChildren = false;

    public List<String> getCanonicalClassNameList() {
        return split(canonicalClassName);
    }

    public List<String> getIncludeFiledList() {
        return split(includeFiled);
    }

    public List<String> getExcludeFiledList() {
        return split(excludeField);
    }

    private List<String> split(String string) {
        if (!StringUtils.isEmpty(string)) {
            String[] split = string.split(";");
            return Arrays.asList(split);
        }
        return Collections.emptyList();
    }


}

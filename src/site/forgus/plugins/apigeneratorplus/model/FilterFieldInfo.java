package site.forgus.plugins.apigeneratorplus.model;

import org.apache.commons.lang.StringUtils;
import org.eclipse.xtend.lib.annotations.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * @author lmx 2020/11/19 14:32
 */
@Data
public class FilterFieldInfo {

    public String canonicalClassName = "";

    public String includeFiled = "";

    public String excludeField = "";

    public boolean excludeChildren = false;

    public Set<String> getIncludeFiledSet(){
        HashSet<String> strings = new HashSet<>();
        if (!StringUtils.isEmpty(includeFiled)) {
            String[] split = includeFiled.split(",");
            for (String str : split) {
                strings.add(str);
            }
        }
        return strings;
    }

    public Set<String> getExcludeFiledSet(){
        HashSet<String> strings = new HashSet<>();
        if (!StringUtils.isEmpty(excludeField)) {
            String[] split = excludeField.split(",");
            for (String str : split) {
                strings.add(str);
            }
        }
        return strings;
    }

}

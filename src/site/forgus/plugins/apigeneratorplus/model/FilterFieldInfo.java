package site.forgus.plugins.apigeneratorplus.model;

import com.intellij.util.containers.ContainerUtil;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @author lmx 2020/11/19 14:32
 */
@Data
public class FilterFieldInfo {

    public String canonicalClassName = "";

    public String includeFiled = "";

    public String excludeField = "";

    public boolean excludeChildren = true;

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
        string = string.replace("\n", "");
        if (!StringUtils.isEmpty(string)) {
            String[] split = string.split(";");
            return Arrays.asList(split);
        }
        return ContainerUtil.newArrayList();
    }


}

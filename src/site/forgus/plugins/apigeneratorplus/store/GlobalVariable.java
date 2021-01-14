package site.forgus.plugins.apigeneratorplus.store;

import com.intellij.openapi.project.Project;
import lombok.Data;

/**
 * @author lmx 2021/1/13 22:03
 **/
@Data
public class GlobalVariable {

    private Project project;

    private static GlobalVariable globalVariable = new GlobalVariable();

    public static GlobalVariable getInstance() {
        return globalVariable;
    }

}

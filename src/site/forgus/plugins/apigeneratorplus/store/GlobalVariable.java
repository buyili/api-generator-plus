package site.forgus.plugins.apigeneratorplus.store;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import lombok.Data;
import site.forgus.plugins.apigeneratorplus.config.ApiGeneratorConfig;

/**
 * @author lmx 2021/1/13 22:03
 **/
@Data
public class GlobalVariable {

    private Project project;

    private ApiGeneratorConfig apiGeneratorConfig;

    private static GlobalVariable globalVariable = new GlobalVariable();

    public static GlobalVariable getInstance() {
        return globalVariable;
    }

    public static ApiGeneratorConfig getApiConfig() {
        if (globalVariable.getApiGeneratorConfig() != null) {
            return globalVariable.getApiGeneratorConfig();
        }
        return ServiceManager.getService(globalVariable.getProject(), ApiGeneratorConfig.class);
    }

}

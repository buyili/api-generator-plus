package site.forgus.plugins.apigeneratorplus.config;


import lombok.Data;
import site.forgus.plugins.apigeneratorplus.yapi.model.YApiProject;

import java.io.Serializable;

@Data
public class YApiProjectConfigInfo implements Cloneable, Serializable {

    private static final long serialVersionUID = -3988083750232797887L;

    private String id = String.valueOf(System.currentTimeMillis());

    private String name = "";

    private String moduleName = "";

    private String packageName = "";

    private String token = "";

    private String projectId = "";

    private String basePath = "";

    private YApiProject project;

    @Override
    public YApiProjectConfigInfo clone() {
        try {
            return (YApiProjectConfigInfo) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

}

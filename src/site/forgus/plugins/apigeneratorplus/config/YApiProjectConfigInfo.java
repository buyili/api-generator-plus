package site.forgus.plugins.apigeneratorplus.config;


import lombok.Data;
import site.forgus.plugins.apigeneratorplus.yapi.model.YApiProject;

@Data
public class YApiProjectConfigInfo implements Cloneable {

    private String packageName;

    private String token;

    private String projectId;

    private String basePath;

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

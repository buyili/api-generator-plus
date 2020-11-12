package site.forgus.plugins.apigenerator.curl.model;


import lombok.Data;
import site.forgus.plugins.apigenerator.config.YApiProjectConfigInfo;

/**
 * @author lmx 2020/11/11 22:53
 **/
@Data
public class CURLModelInfo implements Cloneable{

    private String moduleName = "";

    private String port = "";

    @Override
    public CURLModelInfo clone() {
        try {
            return (CURLModelInfo) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

}

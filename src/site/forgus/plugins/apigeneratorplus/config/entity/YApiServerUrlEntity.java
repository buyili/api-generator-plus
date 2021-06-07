package site.forgus.plugins.apigeneratorplus.config.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lmx 2021/6/6 11:33
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class YApiServerUrlEntity implements Cloneable{

    private String id;

    private String serverUrl;

    @Override
    public YApiServerUrlEntity clone() {
        try {
            return (YApiServerUrlEntity) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }
}

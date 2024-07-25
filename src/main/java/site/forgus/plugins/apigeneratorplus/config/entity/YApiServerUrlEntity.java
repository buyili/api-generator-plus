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

    public String id;

    public String serverUrl;

    @Override
    public YApiServerUrlEntity clone() {
        try {
            return (YApiServerUrlEntity) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString(){
        return serverUrl;
    }
}

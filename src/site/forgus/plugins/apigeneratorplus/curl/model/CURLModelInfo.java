package site.forgus.plugins.apigeneratorplus.curl.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lmx 2020/11/11 22:53
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
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

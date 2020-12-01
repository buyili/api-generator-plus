package site.forgus.plugins.apigeneratorplus.curl.model;

import lombok.Data;

/**
 * @author lmx 2020/12/1 22:47
 **/
@Data
public class Header implements Cloneable{

    private String key = "";

    private String value = "";

    @Override
    public Header clone() {
        try {
            return (Header) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}

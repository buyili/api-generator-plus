package site.forgus.plugins.apigeneratorplus.yapi.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class YApiParam implements Serializable {
    private static final long serialVersionUID = 1022289922468567639L;

    private String desc;
    private String example;
    private String name;
    private String required;

}

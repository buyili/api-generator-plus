package site.forgus.plugins.apigeneratorplus.yapi.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class YApiHeader implements Serializable {
    private static final long serialVersionUID = -6583156150132193662L;

    private String name;
    private String value;
    // 参数示例
    private String example;
    // 备注
    private String desc;
    // 是否必需 1、必需 0、非必需
    private String required;

    public YApiHeader(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public YApiHeader(String name, String value, boolean required, String desc) {
        this.name = name;
        this.value = value;
        this.required = required ? "1" : "0";
        this.desc = desc;
    }

    public static YApiHeader json() {
        return new YApiHeader("Content-Type", "application/json");
    }

    public static YApiHeader form() {
        return new YApiHeader("Content-Type", "application/x-www-form-urlencoded");
    }

    public static YApiHeader multipartFormData() {
        return new YApiHeader("Content-Type", "multipart/form-data");
    }

}

package site.forgus.plugins.apigeneratorplus.yapi.enums;

public enum ResponseBodyTypeEnum {

    JSON("json"),
    RAW("raw");

    private String value;

    ResponseBodyTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

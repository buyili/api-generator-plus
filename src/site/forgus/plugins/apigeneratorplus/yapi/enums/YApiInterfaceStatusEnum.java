package site.forgus.plugins.apigeneratorplus.yapi.enums;

public enum YApiInterfaceStatusEnum {
    DONE("done"),
    UNDONE("undone");
    private String value;

    public String getValue() {
        return value;
    }

    YApiInterfaceStatusEnum(String value) {
        this.value = value;
    }
}

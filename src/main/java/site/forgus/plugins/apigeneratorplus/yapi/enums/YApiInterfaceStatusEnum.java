package site.forgus.plugins.apigeneratorplus.yapi.enums;

public enum YApiInterfaceStatusEnum {
    /**
     * 已完成
     */
    DONE("done"),

    /**
     * 未完成
     */
    UNDONE("undone");

    private String value;

    public String getValue() {
        return value;
    }

    YApiInterfaceStatusEnum(String value) {
        this.value = value;
    }
}

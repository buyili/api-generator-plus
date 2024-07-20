package site.forgus.plugins.apigeneratorplus.yapi.enums;

public enum RequestMethodEnum {
    GET, POST, PUT, DELETE, PATCH;

    public String lowerCaseName(){
        return this.name().toLowerCase();
    }
}

package site.forgus.plugins.apigeneratorplus.constant;

public enum CUrlClientType {

    CMD, BASH;

    public String getSymbolAnd() {
        switch (this) {
            case CMD:
                return "^&";
            case BASH:
                return "&";
        }
        return "";
    }

}

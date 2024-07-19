package site.forgus.plugins.apigeneratorplus.exception;

/**
 * 抛出此异常时插件不处理，抛出给IDE
 * @author limaoxu
 * @date 2024-7-17 17:12:47
 */

public class ReportException extends RuntimeException {

    private static final long serialVersionUID = -5110242073384702378L;

    public ReportException(String message) {
        super(message);
    }
}

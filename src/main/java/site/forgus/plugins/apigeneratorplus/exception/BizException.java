package site.forgus.plugins.apigeneratorplus.exception;

/**
 * @author lmx 2020/11/23 14:29
 */

public class BizException extends RuntimeException {

    private static final long serialVersionUID = 8404678015377060499L;

    public BizException(String message) {
        super(message);
    }

}

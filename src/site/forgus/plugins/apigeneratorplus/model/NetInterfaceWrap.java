package site.forgus.plugins.apigeneratorplus.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author limaoxu
 */
@Setter
@Getter
@ToString
public class NetInterfaceWrap {
    private String displayName;
    private String ipV4;
    private boolean checked = false;

    public NetInterfaceWrap() {
    }

    public NetInterfaceWrap(String displayName, String ipV4) {
        this.displayName = displayName;
        this.ipV4 = ipV4;
    }

    public NetInterfaceWrap(String displayName, String ipV4, boolean checked) {
        this.displayName = displayName;
        this.ipV4 = ipV4;
        this.checked = checked;
    }
}

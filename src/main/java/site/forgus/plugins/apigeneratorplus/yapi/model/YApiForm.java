package site.forgus.plugins.apigeneratorplus.yapi.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class YApiForm extends YApiParam {
    private static final long serialVersionUID = 259883183902353577L;

    private String type = "text";

}

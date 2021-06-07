package site.forgus.plugins.apigeneratorplus.config.entity;

import lombok.Data;
import site.forgus.plugins.apigeneratorplus.yapi.model.YApiProject;

/**
 * @author lmx 2021/6/6 11:30
 **/
@Data
public class YApiProjectEntity {

    private String serverUrlId;

    private String token;

    private YApiProject project;

}

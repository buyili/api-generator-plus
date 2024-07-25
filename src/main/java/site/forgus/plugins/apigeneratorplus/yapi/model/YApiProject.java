package site.forgus.plugins.apigeneratorplus.yapi.model;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class YApiProject implements Serializable {

    private static final long serialVersionUID = -4425604264358577061L;

    public Boolean switch_notice;
    public Boolean is_mock_open;
    public Boolean strice;
    public Boolean is_json5;
    public Integer _id;
    public String name;
    public String basepath;
    public String desc;
    public String project_type;
    public Integer uid;
    public Integer group_id;
    public String icon;
    public String color;
    public Date add_time;
    public Date up_time;
    public Boolean role;
    public String after_script;
    public String pre_script;
    public String project_mock_script;
    // 使用ArrayList代替List,或者设定默认值new ArrayList<>(); 避免IDE读取配置时实例化为com.intellij.util.SmartList, 避免深拷贝时报错
    public List<YApiEnv> env = new ArrayList<>();
    public List<YApiTag> tag = new ArrayList<>();

}

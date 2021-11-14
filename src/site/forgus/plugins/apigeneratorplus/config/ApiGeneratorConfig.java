package site.forgus.plugins.apigeneratorplus.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import site.forgus.plugins.apigeneratorplus.model.FilterFieldInfo;
import site.forgus.plugins.apigeneratorplus.yapi.model.YApiProject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@State(name = "ApiGeneratorConfig2",
        storages = {@Storage("ApiGeneratorPlusPlugin.xml")}
)
public class ApiGeneratorConfig implements PersistentStateComponent<ApiGeneratorConfig> {

//    public Set<String> excludeFieldNames = new HashSet<>();
    public String excludeFields = "serialVersionUID";
    public String excludeAnnotations = "@CurrentId,@CurrentUserId;@CurrentMerchantId,";
//    public Set<String> excludeAnnotationNames = new HashSet<>();
    public String dirPath = "";
    public String prefix = "└";
    public Boolean cnFileName = false;
    public Boolean overwrite = true;

    public String yApiServerUrl = "";
    public String projectToken = "";
    public YApiProject yApiProject;
    public String projectId = "";
    public Boolean autoCat = false;
    public Boolean apiDone = true;
    public String defaultCat = "api_generator_plus";
    public Boolean ignoreResponse = false;
    public String tag = "";
//    public Set<String> tags = new HashSet<>();

    public Boolean isMultiModule = false;
    public Boolean isUseDefaultToken = false;
    public Boolean matchWithModuleName = false;
    public List<YApiProjectConfigInfo> yApiProjectConfigInfoList = new ArrayList<>();
    public FilterFieldInfo filterFieldInfo = new FilterFieldInfo();

    /**
     * 等于空字符串时代表毫秒数，不为空时根据该值内容格式化
     */
    public String dateFormat = "";
    public String localDateFormat = "yyyy-MM-dd";
    public String localDateTimeFormat = "yyyy-MM-dd HH:mm:ss";
    public String localTimeFormat = "HH:mm:ss";


    @Nullable
    @Override
    public ApiGeneratorConfig getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ApiGeneratorConfig state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}

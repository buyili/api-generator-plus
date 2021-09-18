package site.forgus.plugins.apigeneratorplus.setting;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import site.forgus.plugins.apigeneratorplus.curl.enums.ArrayFormatEnum;
import site.forgus.plugins.apigeneratorplus.curl.model.CURLModuleInfo;
import site.forgus.plugins.apigeneratorplus.curl.model.FetchConfig;
import site.forgus.plugins.apigeneratorplus.model.FilterFieldInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lmx 2020/11/11 18:01
 */
@State(name = "site.forgus.plugins.apigenerator.setting.CURLSettingState",
        storages = {@Storage("ApiGeneratorPlusPlugin.xml")}
)
public class CURLSettingState implements PersistentStateComponent<CURLSettingState> {

    public String baseApi = "";

    public List<CURLModuleInfo> moduleInfoList = new ArrayList<>();

    public List<Module> modules;

    /**
     * qs.stringify({ a: ['b', 'c'] }, { arrayFormat: 'indices' })
     * // 'a[0]=b&a[1]=c'
     * qs.stringify({ a: ['b', 'c'] }, { arrayFormat: 'brackets' })
     * // 'a[]=b&a[]=c'
     * qs.stringify({ a: ['b', 'c'] }, { arrayFormat: 'repeat' })
     * // 'a=b&a=c'
     * qs.stringify({ a: ['b', 'c'] }, { arrayFormat: 'comma' })
     */
    public String arrayFormat = ArrayFormatEnum.repeat.name();

    public FilterFieldInfo filterFieldInfo = new FilterFieldInfo();

    public FetchConfig fetchConfig = new FetchConfig();

    public String axiosAppend = "";

    public static CURLSettingState getInstance(Project project){
        return ServiceManager.getService(project, CURLSettingState.class);
    }

    @Nullable
    @Override
    public CURLSettingState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull CURLSettingState state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}

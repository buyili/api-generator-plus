package site.forgus.plugins.apigenerator.setting;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.module.Module;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import site.forgus.plugins.apigenerator.curl.model.CURLModelInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lmx 2020/11/11 18:01
 */
@State(name = "site.forgus.plugins.apigenerator.setting.CURLSettingState")
public class CURLSettingState implements PersistentStateComponent<CURLSettingState> {

    public String ip = "";

    public List<CURLModelInfo> modelInfoList = new ArrayList<>();

    public List<Module> modules;

    public List<String> moduleNames;

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

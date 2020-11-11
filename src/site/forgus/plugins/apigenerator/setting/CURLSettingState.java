package site.forgus.plugins.apigenerator.setting;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author lmx 2020/11/11 18:01
 */
@State(name = "site.forgus.plugins.apigenerator.setting.CURLSettingState")
public class CURLSettingState implements PersistentStateComponent<CURLSettingState> {

    public String ip = "";

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

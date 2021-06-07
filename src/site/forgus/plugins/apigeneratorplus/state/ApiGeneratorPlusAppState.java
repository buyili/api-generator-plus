package site.forgus.plugins.apigeneratorplus.state;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import site.forgus.plugins.apigeneratorplus.config.entity.YApiServerUrlEntity;

import java.util.Collections;
import java.util.List;

/**
 * @author lmx 2021/6/7 11:56
 */
@State(name = "site.forgus.plugins.apigeneratorplus.state.ApiGeneratorPlusAppState",
        storages = @Storage("ApiGeneratorPlusApp.xml"))
public class ApiGeneratorPlusAppState implements PersistentStateComponent<ApiGeneratorPlusAppState> {

    public List<YApiServerUrlEntity> urls = Collections.emptyList();

    public static ApiGeneratorPlusAppState getInstance() {
        return ServiceManager.getService(ApiGeneratorPlusAppState.class);
    }

    public void removeUrl(YApiServerUrlEntity urlEntity) {
        if (CollectionUtils.isNotEmpty(urls)) {
            urls.removeIf(urlItem -> {
                return urlEntity.getId().equals(urlItem.getId());
            });
        }
    }

    @Nullable
    @Override
    public ApiGeneratorPlusAppState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ApiGeneratorPlusAppState state) {

    }
}

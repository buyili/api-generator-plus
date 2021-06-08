package site.forgus.plugins.apigeneratorplus.state;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import site.forgus.plugins.apigeneratorplus.config.entity.YApiProjectEntity;
import site.forgus.plugins.apigeneratorplus.config.entity.YApiServerUrlEntity;
import site.forgus.plugins.apigeneratorplus.yapi.model.YApiProject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lmx 2021/6/7 11:56
 */
@State(name = "site.forgus.plugins.apigeneratorplus.state.ApiGeneratorPlusAppState",
        storages = @Storage("ApiGeneratorPlusPluginApp.xml"))
public class ApiGeneratorPlusAppState implements PersistentStateComponent<ApiGeneratorPlusAppState> {

    public List<YApiServerUrlEntity> urls = new ArrayList<>();
    public List<YApiProjectEntity> projects = new ArrayList<>();

    public static ApiGeneratorPlusAppState getInstance() {
        return ServiceManager.getService(ApiGeneratorPlusAppState.class);
    }

    @Nullable
    @Override
    public ApiGeneratorPlusAppState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ApiGeneratorPlusAppState state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public void removeUrl(YApiServerUrlEntity urlEntity) {
        if (CollectionUtils.isNotEmpty(urls)) {
            urls.removeIf(urlItem -> {
                return urlEntity.getId().equals(urlItem.getId());
            });
        }
    }

    public void addUrl(String url) {
        String id = String.valueOf(System.currentTimeMillis());
        YApiServerUrlEntity urlEntity = new YApiServerUrlEntity(id, url);
        if (urls != null) {
            urls.add(urlEntity);
        }
    }

    public void changeUrl(@Nullable String id, @NotNull String newUrl) {
        for (YApiServerUrlEntity url : urls) {
            if (url.getId().equals(id)) {
                url.setServerUrl(newUrl);
            }
        }
    }

    public void addProject(String serverUrlId, String token, YApiProject yApiProject) {
        YApiProjectEntity entity = new YApiProjectEntity();
        entity.project = yApiProject;
        entity.serverUrlId = serverUrlId;
        entity.token = token;
        if (projects != null) {
            projects.add(entity);
        }
    }
}

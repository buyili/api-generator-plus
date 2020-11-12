package site.forgus.plugins.apigeneratorplus;

import com.google.gson.Gson;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.SystemIndependent;
import org.yaml.snakeyaml.Yaml;
import site.forgus.plugins.apigeneratorplus.curl.CurlUtils;
import site.forgus.plugins.apigeneratorplus.curl.model.CURLModelInfo;
import site.forgus.plugins.apigeneratorplus.setting.CURLSettingState;
import site.forgus.plugins.apigeneratorplus.util.NotificationUtil;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author lmx 2020/11/11 14:19
 */

public class GenerateModuleNamesAction extends AnAction {
    @SneakyThrows
    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        Editor editor = actionEvent.getDataContext().getData(CommonDataKeys.EDITOR);
//        PsiFile psiFile = actionEvent.getData(CommonDataKeys.PSI_FILE);
        Project project = actionEvent.getProject();
//        PsiElement referenceAt = psiFile.findElementAt(editor.getCaretModel().getOffset());
        CURLSettingState state = ServiceManager.getService(project, CURLSettingState.class);
        Module[] modules = ModuleManager.getInstance(project).getModules();


        ArrayList<String> tempModuleNames = new ArrayList<>();
        List<CURLModelInfo> list = new ArrayList<>();
        for (Module module : modules) {
            tempModuleNames.add(module.getName());
            list.add(new CURLModelInfo(module.getName(), findPort(module)));
        }
        state.moduleNames = tempModuleNames;
        state.modelInfoList = list;
        Gson gson = new Gson();
        String message = MessageFormat.format("Generate project modules success! modules: [{0}]", gson.toJson(list));
        NotificationUtil.infoNotify(message, project);
    }

    private String findPort(Module module) {
        VirtualFile moduleFile = module.getModuleFile();
        VirtualFile[] children = moduleFile.getChildren();
        String path = moduleFile.getPath();

        String configFilePath = "";
        try {
            configFilePath = path.substring(0, path.lastIndexOf("/"))
                    .concat("/src/main/resources/application.properties");
            VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(configFilePath);
            Properties properties = new Properties();
            properties.load(virtualFile.getInputStream());
            String port = properties.getProperty("server.port");
            if (StringUtils.isNotBlank(port)) {
                return port;
            }
        } catch (Exception e) {
            System.out.println(configFilePath);
//            e.printStackTrace();
        }
        try {
            configFilePath = path.substring(0, path.lastIndexOf("/"))
                    .concat("/src/main/resources/application.yml");
            VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(configFilePath);
            Yaml yaml = new Yaml();
            Map<String, String> yamlMap = yaml.load(virtualFile.getInputStream());
            Object valueByKey = getValueByKey("server.port", "", yamlMap);
            String port = String.valueOf(valueByKey);
            if (StringUtils.isNotBlank(port)) {
                return port;
            }
        } catch (Exception e) {
            System.out.println(configFilePath);
//            e.printStackTrace();
        }


        String name = moduleFile.getName();
        VirtualFile canonicalFile = moduleFile.getCanonicalFile();
        String canonicalPath = moduleFile.getCanonicalPath();
        @SystemIndependent String moduleFilePath = module.getModuleFilePath();
        return "";
    }

    /**
     * 从Map中获取配置的值
     * 传的key支持两种形式, 一种是单独的,如user.path.key
     * 一种是获取数组中的某一个,如 user.path.key[0]
     * @param key
     * @return
     */
    public Object getValueByKey(String key, Object defaultValue, Map properties) {
        String separator = ".";
        String[] separatorKeys = null;
        if (key.contains(separator)) {
            // 取下面配置项的情况, user.path.keys 这种
            separatorKeys = key.split("\\.");
        } else {
            // 直接取一个配置项的情况, user
            Object res = properties.get(key);
            return res == null ? defaultValue :  res;
        }
        // 下面肯定是取多个的情况
        String finalValue = null;
        Object tempObject = properties;
        for (int i = 0; i < separatorKeys.length; i++) {
            //如果是user[0].path这种情况,则按list处理
            String innerKey = separatorKeys[i];
            Integer index = null;
//            if (innerKey.contains("[")) {
//                // 如果是user[0]的形式,则index = 0 , innerKey=user
//                index = Integer.valueOf(StringTools.getSubstringBetweenFF(innerKey, "[", "]")[0]);
//                innerKey = innerKey.substring(0, innerKey.indexOf("["));
//            }
            Map<String, Object> mapTempObj = (Map) tempObject;
            Object object = mapTempObj.get(innerKey);
            // 如果没有对应的配置项,则返回设置的默认值
            if (object == null) {
                return defaultValue;
            }
            Object targetObj = object;
            if (index != null) {
                // 如果是取的数组中的值,在这里取值
                targetObj = ((ArrayList) object).get(index);
            }
            // 一次获取结束,继续获取后面的
            tempObject = targetObj;
            if (i == separatorKeys.length - 1) {
                //循环结束
                return targetObj;
            }

        }
        return null;
    }

}

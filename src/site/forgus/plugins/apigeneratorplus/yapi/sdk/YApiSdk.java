package site.forgus.plugins.apigeneratorplus.yapi.sdk;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.extern.log4j.Log4j;
import org.jetbrains.annotations.Nullable;
import site.forgus.plugins.apigeneratorplus.exception.BizException;
import site.forgus.plugins.apigeneratorplus.util.HttpUtil;
import site.forgus.plugins.apigeneratorplus.util.JsonUtil;
import site.forgus.plugins.apigeneratorplus.yapi.model.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YApiSdk {

    private static final String PROJECT_INFO_URI = "/api/project/get";
    private static final String LIST_CATEGORY_URI = "/api/interface/getCatMenu";
    private static final String ADD_CATEGORY_URI = "/api/interface/add_cat";
    private static final String SAVE_INTERFACE_URI = "/api/interface/save";

    private static Gson gson;

    static {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Date.class, new DateSerializer()).setDateFormat(DateFormat.LONG);
        builder.registerTypeAdapter(Date.class, new DateDeserializer()).setDateFormat(DateFormat.LONG);
        gson = builder.create();
    }

    /**
     * 获取项目信息
     *
     * @param token
     * @return
     * @throws IOException
     */
    @Nullable
    public static YApiProject getProjectInfo(String serverUrl, String token) {
        Map<String, String> params = new HashMap<>();
        params.put("token", token);
        String responseStr = HttpUtil.doGet(serverUrl + PROJECT_INFO_URI, params);
        Type type = new TypeToken<YApiResponse<YApiProject>>() {
        }.getType();
        YApiResponse<YApiProject> yApiResponse = gson.fromJson(responseStr, type);
        System.out.println(responseStr);
        if (yApiResponse != null && yApiResponse.getErrcode() != 0) {
            String message = MessageFormat.format("Server Url 和 Token 不匹配; server url: [{0}] token: [{1}]",
                    serverUrl, token);
            throw new BizException(message);
//            Messages.showErrorDialog(message, "多模块项目配置保存失败");
//            return null;
        }
        return yApiResponse.getData();
    }

    /**
     * 获取分类列表
     *
     * @param token
     * @return
     * @throws IOException
     */
    public static List<YApiCat> listCategories(String serverUrl, String token) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("token", token);
        String responseStr = HttpUtil.doGet(serverUrl + LIST_CATEGORY_URI, params);
        Type type = new TypeToken<YApiResponse<List<YApiCat>>>() {
        }.getType();
        YApiResponse<List<YApiCat>> yApiResponse = gson.fromJson(responseStr, type);
        if (yApiResponse != null && yApiResponse.getErrcode() != 0) {
            String message = MessageFormat.format("Server Url 和 Token 不匹配; server url: [{0}] token: [{1}]",
                    serverUrl, token);
            throw new BizException(message);
        }
        return yApiResponse.getData();
    }

    /**
     * 添加分类
     *
     * @param token
     * @param projectId
     * @param name      分类名称
     * @return
     * @throws IOException
     */
    public static YApiResponse<YApiCat> addCategory(String serverUrl, String token, String projectId, String name) throws IOException {
        return addCategory(serverUrl, token, projectId, name, "");
    }

    /**
     * 保存接口（新增或更新）
     *
     * @param yApiInterface
     * @return
     * @throws IOException
     */
    public static YApiResponse saveInterface(String serverUrl, YApiInterface yApiInterface) throws IOException {
        System.out.println(JsonUtil.prettyJson.toJson(yApiInterface));
        String string = HttpUtil.doPost(serverUrl + SAVE_INTERFACE_URI, gson.toJson(yApiInterface));
        System.out.println(MessageFormat.format("保存接口返回值: \n{0}", string));
        return gson.fromJson(string, YApiResponse.class);
    }

    private static YApiResponse<YApiCat> addCategory(String serverUrl, String token, String projectId, String name, String desc) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("desc", desc);
        params.put("name", name);
        params.put("project_id", projectId);
        params.put("token", token);
        String string = HttpUtil.doPost(serverUrl + ADD_CATEGORY_URI, gson.toJson(params));
        Type type = new TypeToken<YApiResponse<YApiCat>>() {
        }.getType();
        return gson.fromJson(string, type);
    }

}

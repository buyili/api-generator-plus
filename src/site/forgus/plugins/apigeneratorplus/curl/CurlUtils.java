package site.forgus.plugins.apigeneratorplus.curl;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.TextTransferable;
import lombok.extern.log4j.Log4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.SystemIndependent;
import org.yaml.snakeyaml.Yaml;
import site.forgus.plugins.apigeneratorplus.constant.CUrlClientType;
import site.forgus.plugins.apigeneratorplus.constant.WebAnnotation;
import site.forgus.plugins.apigeneratorplus.curl.model.CURLModuleInfo;
import site.forgus.plugins.apigeneratorplus.curl.model.FetchRequestInfo;
import site.forgus.plugins.apigeneratorplus.curl.model.Header;
import site.forgus.plugins.apigeneratorplus.model.FilterFieldInfo;
import site.forgus.plugins.apigeneratorplus.normal.FieldInfo;
import site.forgus.plugins.apigeneratorplus.normal.MethodInfo;
import site.forgus.plugins.apigeneratorplus.setting.CURLSettingState;
import site.forgus.plugins.apigeneratorplus.util.FieldUtil;
import site.forgus.plugins.apigeneratorplus.util.JsonUtil;
import site.forgus.plugins.apigeneratorplus.util.NotificationUtil;
import site.forgus.plugins.apigeneratorplus.util.StringUtil;
import site.forgus.plugins.apigeneratorplus.yapi.enums.RequestMethodEnum;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.MessageFormat;
import java.util.*;

/**
 * @author lmx 2020/11/11 15:49
 */
@Log4j
public class CurlUtils {

    private CURLSettingState curlSettingState;

    private static final String SLASH = "/";

    public void copyAsFetch(@NotNull AnActionEvent actionEvent) {
        Editor editor = actionEvent.getData(CommonDataKeys.EDITOR);
        Assert.notNull(editor);

        PsiFile psiFile = actionEvent.getData(CommonDataKeys.PSI_FILE);
        Assert.notNull(psiFile);

        Project project = actionEvent.getProject();
        Assert.notNull(project);

        PsiElement referenceAt = psiFile.findElementAt(editor.getCaretModel().getOffset());
        Assert.notNull(referenceAt);

        PsiClass selectedClass = PsiTreeUtil.getContextOfType(referenceAt, PsiClass.class);
        curlSettingState = ServiceManager.getService(project, CURLSettingState.class);

        PsiMethod selectedMethod = PsiTreeUtil.getContextOfType(referenceAt, PsiMethod.class);
        if (selectedMethod != null) {
            //            PsiAnnotation[] annotations = selectedMethod.getAnnotations();
            //            boolean postMethod = isPostMethod(selectedMethod);
            MethodInfo methodInfo = new MethodInfo(selectedMethod);
            String moduleName = getModuleName(editor, project);
            checkHasModuleConfig(project, moduleName);
            CURLModuleInfo curlModuleInfo = getCurlModelInfo(moduleName);
            Assert.notNull(curlModuleInfo, "no matching module configuration");

            String port = StringUtils.isEmpty(curlModuleInfo.getPort()) ? getChooseOrInputPort() : curlModuleInfo.getPort();

            FetchRequestInfo fetchRequestInfo = new FetchRequestInfo();
            FetchRequestInfo.InitOptions initOptions = new FetchRequestInfo.InitOptions();


            fetchRequestInfo.setInput(getBaseApi(port) + buildPath(selectedMethod, curlModuleInfo) + getRequestParams(selectedMethod, methodInfo));
            // 访问接口
            if (!isGetMethod(selectedMethod.getAnnotations())) {
                // 非Get请求参数
                //fetchRequestInfo.setInput(getBaseApi(port) + buildPath(selectedMethod, curlModuleInfo));
                initOptions.setBody(getRequestBody(selectedMethod, methodInfo));
            }

            PsiAnnotation methodMapping = getMethodMapping(selectedMethod);
            Assert.notNull(methodMapping, "not specific annotation for mapping web requests ");
            initOptions.setMethod(getMethodFromAnnotation(methodMapping).name());
            // 添加header
            initOptions.setHeaders(curlModuleInfo.getHeadersAsMap());

            if (StringUtils.isNotEmpty(curlSettingState.fetchConfig.credentials)) {
                initOptions.setCredentials(curlSettingState.fetchConfig.credentials);
            }
            if (StringUtils.isNotEmpty(curlSettingState.fetchConfig.cache)) {
                initOptions.setCache(curlSettingState.fetchConfig.cache);
            }
            if (StringUtils.isNotEmpty(curlSettingState.fetchConfig.redirect)) {
                initOptions.setRedirect(curlSettingState.fetchConfig.redirect);
            }
            if (StringUtils.isNotEmpty(curlSettingState.fetchConfig.referrer)) {
                initOptions.setReferrer(curlSettingState.fetchConfig.referrer);
            }
            if (StringUtils.isNotEmpty(curlSettingState.fetchConfig.referrerPolicy)) {
                initOptions.setReferrerPolicy(curlSettingState.fetchConfig.referrerPolicy);
            }
            if (StringUtils.isNotEmpty(curlSettingState.fetchConfig.integrity)) {
                initOptions.setIntegrity(curlSettingState.fetchConfig.integrity);
            }

            fetchRequestInfo.setInitOptions(initOptions);
            String rawStr = fetchRequestInfo.toPrettyString();
            System.out.println(rawStr);
            CopyPasteManager.getInstance().setContents(new TextTransferable(rawStr));
            NotificationUtil.infoNotify("已复制到剪切板", rawStr, project);
        }
    }

    public void copyAsCUrl(@NotNull AnActionEvent actionEvent, CUrlClientType cUrlClientType) {
        Editor editor = actionEvent.getData(CommonDataKeys.EDITOR);
        Assert.notNull(editor);

        PsiFile psiFile = actionEvent.getData(CommonDataKeys.PSI_FILE);
        Assert.notNull(psiFile);

        Project project = actionEvent.getProject();
        Assert.notNull(project);

        PsiElement referenceAt = psiFile.findElementAt(editor.getCaretModel().getOffset());
        Assert.notNull(referenceAt);

        PsiClass selectedClass = PsiTreeUtil.getContextOfType(referenceAt, PsiClass.class);
        curlSettingState = ServiceManager.getService(project, CURLSettingState.class);

        PsiMethod selectedMethod = PsiTreeUtil.getContextOfType(referenceAt, PsiMethod.class);
        if (selectedMethod != null) {
            //            PsiAnnotation[] annotations = selectedMethod.getAnnotations();
            //            boolean postMethod = isPostMethod(selectedMethod);
            MethodInfo methodInfo = new MethodInfo(selectedMethod);
            String moduleName = getModuleName(editor, project);
            checkHasModuleConfig(project, moduleName);
            CURLModuleInfo curlModuleInfo = getCurlModelInfo(moduleName);
            Assert.notNull(curlModuleInfo);

            String port = StringUtils.isEmpty(curlModuleInfo.getPort()) ? getChooseOrInputPort() : curlModuleInfo.getPort();
            StringBuilder stringBuilder = new StringBuilder("curl");

            // 访问接口
            stringBuilder.append(" '")
                    .append(getBaseApi(port))
                    .append(buildPath(selectedMethod, curlModuleInfo));
            stringBuilder.append(getRequestParams(selectedMethod, methodInfo, cUrlClientType));
            stringBuilder.append("'");
            if (!isGetMethod(selectedMethod.getAnnotations())) {
                // 非Get请求参数
                stringBuilder.append(getRequestBody(selectedMethod, methodInfo, cUrlClientType));
            }

            if (CollectionUtils.isNotEmpty(curlModuleInfo.getRequestHeaders())) {
                for (Header requestHeader : curlModuleInfo.getRequestHeaders()) {
                    stringBuilder.append(" -H '").append(requestHeader.getKey()).append(": ")
                            .append(requestHeader.getValue()).append("'");
                }
            }

            String curlStr = stringBuilder.toString();
            if (CUrlClientType.CMD.equals(cUrlClientType)) {
                curlStr = curlStr.replaceAll("'", "\"");
            }
            System.out.println(curlStr);
            CopyPasteManager.getInstance().setContents(new TextTransferable(curlStr));
            NotificationUtil.infoNotify("已复制到剪切板", curlStr, project);
        }
    }

    public static void findModuleInfoAndSave(AnActionEvent actionEvent) {
        Project project = actionEvent.getProject();
        Assert.notNull(project);
        CURLSettingState state = ServiceManager.getService(project, CURLSettingState.class);
        findModuleInfoAndSave(project, state);
    }

    public static void findModuleInfoAndSave(Project project, CURLSettingState state) {
        List<CURLModuleInfo> foundModuleInfoList = findModuleInfo(project);
        String oldJson = JsonUtil.gson.toJson(state.moduleInfoList);
        for (CURLModuleInfo info : state.moduleInfoList) {
            foundModuleInfoList.removeIf(curlModuleInfo -> info.getModuleName().equals(curlModuleInfo.getModuleName()));
        }
        state.moduleInfoList.addAll(foundModuleInfoList);
        String message = MessageFormat.format("Generate project modules success!\n old modules: {0} \nadd modules: {1}",
                oldJson, JsonUtil.gson.toJson(foundModuleInfoList));
        NotificationUtil.infoNotify(message, project);
    }

    public static List<CURLModuleInfo> findModuleInfo(Project project) {
        Module[] modules = ModuleManager.getInstance(project).getModules();
        if (modules.length > 0) {
            List<CURLModuleInfo> list = new ArrayList<>();
            for (Module module : modules) {
                CURLModuleInfo curlModuleInfo = new CURLModuleInfo();
                curlModuleInfo.setModuleName(module.getName());
                curlModuleInfo.setPort(findPort(module));
                curlModuleInfo.setContextPath(findContextPath(module));
                list.add(curlModuleInfo);
            }
            return list;
        }
        return ContainerUtil.newArrayList();
    }

    private String buildPath(PsiMethod psiMethod, CURLModuleInfo info) {
        String classPath = "";
        String methodPath = "";
        for (PsiAnnotation annotation : Objects.requireNonNull(psiMethod.getContainingClass()).getAnnotations()) {
            if (annotation.getText().contains("Mapping")) {
                classPath = getPathFromAnnotation(annotation);
                break;
            }
        }
        for (PsiAnnotation annotation : psiMethod.getAnnotations()) {
            if (annotation.getText().contains("Mapping")) {
                methodPath = getPathFromAnnotation(annotation);
                break;
            }
        }
        return pathResolve(info.getContextPath(), classPath, methodPath);
    }

    public String pathResolve(String... args) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String arg : args) {
            if (StringUtils.isNotBlank(arg)) {
                if (!arg.startsWith("/")) {
                    stringBuilder.append('/');
                }
                if (arg.endsWith("/")) {
                    stringBuilder.append(arg.substring(0, arg.length() - 1));
                } else {
                    stringBuilder.append(arg);
                }
            }
        }
        return stringBuilder.toString();
    }

    private String getPathFromAnnotation(PsiAnnotation annotation) {
        if (annotation == null) {
            return "";
        }
        PsiNameValuePair[] psiNameValuePairs = annotation.getParameterList().getAttributes();
        if (psiNameValuePairs.length == 1 && psiNameValuePairs[0].getName() == null) {
            return appendSlash(psiNameValuePairs[0].getLiteralValue());
        }
        if (psiNameValuePairs.length >= 1) {
            for (PsiNameValuePair psiNameValuePair : psiNameValuePairs) {
                if (psiNameValuePair.getName().equals("value") || psiNameValuePair.getName().equals("path")) {
                    return appendSlash(psiNameValuePair.getLiteralValue());
                }
            }
        }
        return "";
    }

    private String appendSlash(String path) {
        if (StringUtils.isEmpty(path)) {
            return "";
        }
        String p = path;
        if (!path.startsWith(SLASH)) {
            p = SLASH + path;
        }
        if (path.endsWith(SLASH)) {
            p = p.substring(0, p.length() - 1);
        }
        return p;
    }

    private PsiAnnotation getMethodMapping(PsiMethod psiMethod) {
        for (PsiAnnotation annotation : psiMethod.getAnnotations()) {
            String text = annotation.getText();
            if (text.contains("Mapping")) {
                return annotation;
            }
        }
        return null;
    }

    private RequestMethodEnum getMethodFromAnnotation(PsiAnnotation methodMapping) {
        String text = methodMapping.getText();
        if (text.contains(WebAnnotation.RequestMapping)) {
            return extractMethodFromAttribute(methodMapping);
        }
        return extractMethodFromMappingText(text);
    }

    private RequestMethodEnum extractMethodFromMappingText(String text) {
        if (text.contains(WebAnnotation.GetMapping)) {
            return RequestMethodEnum.GET;
        }
        if (text.contains(WebAnnotation.PutMapping)) {
            return RequestMethodEnum.PUT;
        }
        if (text.contains(WebAnnotation.DeleteMapping)) {
            return RequestMethodEnum.DELETE;
        }
        if (text.contains(WebAnnotation.PatchMapping)) {
            return RequestMethodEnum.PATCH;
        }
        return RequestMethodEnum.POST;
    }

    private RequestMethodEnum extractMethodFromAttribute(PsiAnnotation annotation) {
        PsiNameValuePair[] psiNameValuePairs = annotation.getParameterList().getAttributes();
        for (PsiNameValuePair psiNameValuePair : psiNameValuePairs) {
            if ("method".equals(psiNameValuePair.getName())) {
                return RequestMethodEnum.valueOf(psiNameValuePair.getValue().getReference().resolve().getText());
            }
        }
        return RequestMethodEnum.POST;
    }

    public static Module getModule(Editor editor, Project project) {
        Document document = editor.getDocument();
        FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
        VirtualFile virtualFile = fileDocumentManager.getFile(document);
        ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(project).getFileIndex();

        if (virtualFile != null) {
            return projectFileIndex.getModuleForFile(virtualFile);
        }
        return null;
    }

    private void checkHasModuleConfig(Project project, String moduleName) {
        if (CollectionUtils.isEmpty(curlSettingState.moduleInfoList)
                || getCurlModelInfo(moduleName) == null) {
            findModuleInfoAndSave(project, curlSettingState);
        }
    }

    private String getModuleName(Editor editor, Project project) {
        Document document = editor.getDocument();
        FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
        VirtualFile virtualFile = fileDocumentManager.getFile(document);
        ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(project).getFileIndex();

        String moduleName = null;
        if (virtualFile != null) {
            Module module = projectFileIndex.getModuleForFile(virtualFile);
            moduleName = module != null ? module.getName() : "";
        }
        return moduleName;
    }

    private String getPort(String moduleName) {
        if (StringUtils.isEmpty(moduleName)) {
            return "";
        }
        for (CURLModuleInfo curlModuleInfo : curlSettingState.moduleInfoList) {
            if (curlModuleInfo.getModuleName().equals(moduleName)) {
                return curlModuleInfo.getPort();
            }
        }
        return "";
    }

    private CURLModuleInfo getCurlModelInfo(String moduleName) {
        if (StringUtils.isNotEmpty(moduleName)) {
            for (CURLModuleInfo curlModuleInfo : curlSettingState.moduleInfoList) {
                if (curlModuleInfo.getModuleName().equals(moduleName)) {
                    return curlModuleInfo;
                }
            }
        }
//        NotificationUtil.errorNotify("no matching module configuration\n Please right click 'Copy as CURL-> Generate Project Modules' to scan the module information");
        return null;
    }

    private String getChooseOrInputPort() {
        List<String> strings = new ArrayList<>();
        for (CURLModuleInfo info : curlSettingState.moduleInfoList) {
            if (StringUtils.isNotEmpty(info.getPort())) {
                strings.add(info.getPort() + ":" + info.getModuleName());
            }
        }
        String[] modelWithPort = strings.toArray(new String[0]);
        String s = Messages.showEditableChooseDialog("请选择或输入端口", "提示", null, modelWithPort, "8080", null);
        Assert.notNull(s, "cancel copy as curl");
        String[] split = s.split(":");
        if (split.length > 0) {
            return split[0];
        }
        return "";
    }

    private boolean annotationContain(PsiAnnotation[] annotations, String str) {
        for (PsiAnnotation annotation : annotations) {
            if (annotation.getText().contains(str)) {
                return true;
            }
        }
        return false;
    }

    private boolean isGetMethod(PsiAnnotation[] annotations) {
        for (PsiAnnotation annotation : annotations) {
            if (annotation.getText().contains("GetMapping") || annotation.getText().contains("GET")) {
                return true;
            }
        }
        return false;
    }

    private boolean haveControllerAnnotation(PsiClass psiClass) {
        PsiAnnotation[] annotations = psiClass.getAnnotations();
        for (PsiAnnotation annotation : annotations) {
            if (annotation.getText().contains(WebAnnotation.Controller)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取本地真正的IP地址，即获得有线或者 无线WiFi 地址。
     * 过滤虚拟机、蓝牙等地址
     *
     * @return IPv4
     */
    public static String getRealIP() {
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = allNetInterfaces.nextElement();
                // 去除回环接口，子接口，未运行和接口
                if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
                    continue;
                }

                if (!netInterface.getDisplayName().contains("Intel")
                        && !netInterface.getDisplayName().contains("Realtek")
                        && !netInterface.getDisplayName().contains("Ethernet")) {
                    continue;
                }

                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress ip = addresses.nextElement();
                    if (ip != null) {
                        // ipv4
                        if (ip instanceof Inet4Address) {
                            return ip.getHostAddress();
                        }
                    }
                }
                break;
            }
        } catch (SocketException e) {
            //捕获异常
        }
        return null;
    }

    public boolean isPostMethod(PsiMethod psiMethod) {
        PsiAnnotation[] annotations = psiMethod.getAnnotations();
        for (PsiAnnotation annotation : annotations) {
            if (annotation.getText().contains(WebAnnotation.PostMapping)) {
                return true;
            } else if (annotation.getText().contains(WebAnnotation.RequestMapping)) {
                PsiAnnotationParameterList parameterList = annotation.getParameterList();
                PsiNameValuePair[] attributes = parameterList.getAttributes();
                for (PsiNameValuePair psiNameValuePair : attributes) {
                    StringUtil.showPsiNameValuePair(psiNameValuePair);
                    return "method".equals(psiNameValuePair.getName())
                            && psiNameValuePair.getValue().getText().contains(RequestMethodEnum.POST.name());
                }
            }
        }
        return false;
    }

    public String getBaseApi(String port) {
        if (StringUtils.isNotEmpty(curlSettingState.baseApi)) {
            return curlSettingState.baseApi;
        }
        String localIP = getRealIP();
        return "http://" + localIP + ":" + port;
    }


    public String getRequestBody(PsiMethod psiMethod, MethodInfo methodInfo, CUrlClientType cUrlClientType) {
        StringUtil.showPsiMethod(psiMethod);
        List<FieldInfo> requestFields = methodInfo.getRequestFields();
        if (containRequestBodyAnnotation(psiMethod)) {
            for (FieldInfo requestField : requestFields) {
                if (containRequestBodyAnnotation(requestField.getAnnotations().toArray(new PsiAnnotation[0]))) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(" -H 'Content-Type: application/json;charset=UTF-8'");
                    stringBuilder.append(" --data-binary '");
                    String s = JsonUtil.buildRawJson(requestField);
                    if (cUrlClientType.equals(CUrlClientType.CMD)) {
                        s = s.replace("\"", "\\\"");
                    }
                    stringBuilder.append(s)
                            .append("'");
                    return stringBuilder.toString();
                }
            }
        } else {
            List<String> strings = generateKeyValue(requestFields);
            StringBuilder stringBuilder = new StringBuilder("");
            stringBuilder.append(" -H 'Content-Type: application/x-www-form-urlencoded'");
            stringBuilder.append(" --data-raw '");
            for (String string : strings) {
                //                stringBuilder.append(string).append(cUrlClientType.getSymbolAnd());
                stringBuilder.append(string).append("&");
            }
            stringBuilder.append("'");
            return stringBuilder.toString();
        }
        return "";
    }

    public String getRequestBody(PsiMethod psiMethod, MethodInfo methodInfo) {
        StringUtil.showPsiMethod(psiMethod);
        List<FieldInfo> requestFields = methodInfo.getRequestFields();
        if (containRequestBodyAnnotation(psiMethod)) {
            for (FieldInfo requestField : requestFields) {
                if (containRequestBodyAnnotation(requestField.getAnnotations().toArray(new PsiAnnotation[0]))) {
                    return JsonUtil.buildRawJson(requestField);
                }
            }
        } else {
            List<String> strings = generateKeyValue(requestFields);
            StringBuilder stringBuilder = new StringBuilder();
            for (String string : strings) {
                stringBuilder.append(string).append("&");
            }
            return stringBuilder.toString();
        }
        return "";
    }

    public String getRequestParams(PsiMethod psiMethod, MethodInfo methodInfo, CUrlClientType cUrlClientType) {
        StringUtil.showPsiMethod(psiMethod);
        boolean containRequestBodyAnnotation = containRequestBodyAnnotation(psiMethod);
        if (!isGetMethod(psiMethod.getAnnotations()) && !containRequestBodyAnnotation) {
            return "";
        }
        List<FieldInfo> requestFields = methodInfo.getRequestFields();
        List<FieldInfo> filteredFields = new ArrayList<>(requestFields);
        if (containRequestBodyAnnotation) {
            for (FieldInfo requestField : requestFields) {
                if (containRequestBodyAnnotation(requestField.getAnnotations().toArray(new PsiAnnotation[0]))) {
                    filteredFields.remove(requestField);
                }
            }
            if (CollectionUtils.isEmpty(filteredFields)) {
                return "";
            }
        }
        List<String> strings = generateKeyValue(filteredFields);
        StringBuilder stringBuilder = new StringBuilder("?");
        for (String string : strings) {
            //            stringBuilder.append(string).append(cUrlClientType.getSymbolAnd());
            stringBuilder.append(string).append("&");
        }
        String str = stringBuilder.toString();
        str = str.substring(0, str.length() - 1);
        //        str = str.replaceAll("%", "^%");
        return str;
    }

    public String getRequestParams(PsiMethod psiMethod, MethodInfo methodInfo) {
        StringUtil.showPsiMethod(psiMethod);
        List<FieldInfo> requestFields = methodInfo.getRequestFields();
        List<String> strings = generateKeyValue(requestFields);
        StringBuilder stringBuilder = new StringBuilder("?");
        for (String string : strings) {
//            stringBuilder.append(string).append(cUrlClientType.getSymbolAnd());
            stringBuilder.append(string).append("&");
        }
        String str = stringBuilder.toString();
        str = str.substring(0, str.length() - 1);
//        str = str.replaceAll("%", "^%");
        return str;
    }

    private List<String> generateKeyValue(List<FieldInfo> fieldInfoList) {
        if (CollectionUtils.isEmpty(fieldInfoList)) {
            return ContainerUtil.newArrayList();
        }
        ArrayList<String> strings = new ArrayList<>();
        for (FieldInfo requestField : fieldInfoList) {
            if (requestField.hasChildren()) {
                strings.addAll(generateKeyValue(filterChildrenFiled(requestField)));
            } else {
                String value = FieldUtil.getValueForCurl(requestField.getName(), requestField.getPsiType(), curlSettingState);
                strings.add(value);
//                String strVal = "";
//                if (null != value && !"".equals(String.valueOf(value))) {
//                    strVal = URLEncoder.encode(value.toString());
//                    strings.add(requestField.getName() + "=" + strVal);
//                }
            }
        }
        return strings;
    }

    public List<FieldInfo> filterChildrenFiled(FieldInfo fieldInfo) {
        FilterFieldInfo filterFieldInfo = curlSettingState.filterFieldInfo;
        List<String> canonicalClassNameList = filterFieldInfo.getCanonicalClassNameList();
        List<String> includeFiledList = filterFieldInfo.getIncludeFiledList();
        List<FieldInfo> children = fieldInfo.getChildren();
        List<String> excludeFiledList = filterFieldInfo.getExcludeFiledList();
        int index = getIndexOnCanonicalClassNameList(fieldInfo.getPsiType().getCanonicalText(), canonicalClassNameList);
        if (CollectionUtils.isNotEmpty(canonicalClassNameList) && index != -1) {

            if (includeFiledList.size() > index && StringUtils.isNotEmpty(includeFiledList.get(index))) {
                children.removeIf(child -> !includeFiledList.get(index).contains(child.getName() + ","));
            } else if (excludeFiledList.size() > index && StringUtils.isNotEmpty(excludeFiledList.get(index))) {
                children.removeIf(child -> excludeFiledList.get(index).contains(child.getName()));
            }
            if (filterFieldInfo.excludeChildren) {
                for (FieldInfo child : children) {
                    child.setChildren(ContainerUtil.newArrayList());
                }
            }
        }
        return children;
    }

    public int getIndexOnCanonicalClassNameList(String canonicalClassName, List<String> set) {
        for (String s : set) {
            if (canonicalClassName.startsWith(s)) {
                return set.indexOf(s);
            }
        }
        return -1;
    }

    private boolean containRequestBodyAnnotation(PsiAnnotation[] annotations) {
        for (PsiAnnotation annotation : annotations) {
            if (annotation.getText().contains(WebAnnotation.RequestBody)) {
                return true;
            }
        }
        return false;
    }

    private boolean containRequestBodyAnnotation(PsiMethod psiMethod) {
        if (containRequestBodyAnnotation(psiMethod.getAnnotations())) {
            return true;
        }
        for (PsiParameter parameter : psiMethod.getParameterList().getParameters()) {
            if (parameter.getText().contains(WebAnnotation.RequestBody)) {
                return true;
            }
        }
        return false;
    }


    public static String findPort(Module module) {
        @SystemIndependent String path = module.getModuleFilePath();

        String configFilePath = "";
        String key = "server.port";
        String val = "";
        try {
            configFilePath = path.substring(0, path.lastIndexOf("/"))
                    .concat("/src/main/resources/application.yml");
            log.info(configFilePath);
            VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(configFilePath);
            Yaml yaml = new Yaml();
            Map<String, String> yamlMap = yaml.load(virtualFile.getInputStream());
            Object valueByKey = getValueByKey(key, "", yamlMap);
            String tempVal = String.valueOf(valueByKey);
            if (StringUtils.isNotBlank(tempVal)) {
                val = tempVal;
            }
        } catch (Exception e) {
//            System.out.println(configFilePath);
        }
        try {
            configFilePath = path.substring(0, path.lastIndexOf("/"))
                    .concat("/src/main/resources/application.yaml");
            log.info(configFilePath);
            VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(configFilePath);
            Yaml yaml = new Yaml();
            Map<String, String> yamlMap = yaml.load(virtualFile.getInputStream());
            Object valueByKey = getValueByKey(key, "", yamlMap);
            String tempVal = String.valueOf(valueByKey);
            if (StringUtils.isNotBlank(tempVal)) {
                val = tempVal;
            }
        } catch (Exception e) {
            System.out.println(configFilePath);
        }
        try {
            configFilePath = path.substring(0, path.lastIndexOf("/"))
                    .concat("/src/main/resources/application.properties");
            log.info(configFilePath);
            VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(configFilePath);
            Properties properties = new Properties();
            properties.load(virtualFile.getInputStream());
            String tempVal = properties.getProperty(key);
            if (StringUtils.isNotBlank(tempVal)) {
                val = tempVal;
            }
        } catch (Exception e) {
            System.out.println(configFilePath);
        }
        return val;
    }


    public static String findContextPath(Module module) {
        @SystemIndependent String path = module.getModuleFilePath();

        String configFilePath = "";
        String key = "server.servlet.context-path";
        String val = "";
        try {
            configFilePath = path.substring(0, path.lastIndexOf("/"))
                    .concat("/src/main/resources/application.yml");
            log.info(configFilePath);
            VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(configFilePath);
            Yaml yaml = new Yaml();
            Map<String, String> yamlMap = yaml.load(virtualFile.getInputStream());
            Object valueByKey = getValueByKey(key, "", yamlMap);
            String tempVal = String.valueOf(valueByKey);
            if (StringUtils.isNotBlank(tempVal)) {
                val = tempVal;
            }
        } catch (Exception e) {
//            System.out.println(configFilePath);
        }
        try {
            configFilePath = path.substring(0, path.lastIndexOf("/"))
                    .concat("/src/main/resources/application.yaml");
            log.info(configFilePath);
            VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(configFilePath);
            Yaml yaml = new Yaml();
            Map<String, String> yamlMap = yaml.load(virtualFile.getInputStream());
            Object valueByKey = getValueByKey(key, "", yamlMap);
            String tempVal = String.valueOf(valueByKey);
            if (StringUtils.isNotBlank(tempVal)) {
                val = tempVal;
            }
        } catch (Exception e) {
//            System.out.println(configFilePath);
        }
        try {
            configFilePath = path.substring(0, path.lastIndexOf("/"))
                    .concat("/src/main/resources/application.properties");
            log.info(configFilePath);
            VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(configFilePath);
            Properties properties = new Properties();
            properties.load(virtualFile.getInputStream());
            String tempVal = properties.getProperty(key);
            if (StringUtils.isNotBlank(tempVal)) {
                val = tempVal;
            }
        } catch (Exception e) {
//            System.out.println(configFilePath);
        }
        return val;
    }

    /**
     * 从Map中获取配置的值
     * 传的key支持两种形式, 一种是单独的,如user.path.key
     * 一种是获取数组中的某一个,如 user.path.key[0]
     *
     * @param key
     * @return
     */
    public static Object getValueByKey(String key, Object defaultValue, Map properties) {
        String separator = ".";
        String[] separatorKeys = null;
        if (key.contains(separator)) {
            // 取下面配置项的情况, user.path.keys 这种
            separatorKeys = key.split("\\.");
        } else {
            // 直接取一个配置项的情况, user
            Object res = properties.get(key);
            return res == null ? defaultValue : res;
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

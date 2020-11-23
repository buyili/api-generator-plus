package site.forgus.plugins.apigeneratorplus.curl;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ui.TextTransferable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import site.forgus.plugins.apigeneratorplus.constant.CUrlClientType;
import site.forgus.plugins.apigeneratorplus.constant.WebAnnotation;
import site.forgus.plugins.apigeneratorplus.curl.model.CURLModelInfo;
import site.forgus.plugins.apigeneratorplus.model.FilterFieldInfo;
import site.forgus.plugins.apigeneratorplus.normal.FieldInfo;
import site.forgus.plugins.apigeneratorplus.normal.MethodInfo;
import site.forgus.plugins.apigeneratorplus.setting.CURLSettingState;
import site.forgus.plugins.apigeneratorplus.util.FieldUtil;
import site.forgus.plugins.apigeneratorplus.util.JsonUtil;
import site.forgus.plugins.apigeneratorplus.util.NotificationUtil;
import site.forgus.plugins.apigeneratorplus.util.StringUtil;
import site.forgus.plugins.apigeneratorplus.yapi.enums.RequestMethodEnum;

import java.net.*;
import java.util.*;

/**
 * @author lmx 2020/11/11 15:49
 */

public class CurlUtils {

    private CURLSettingState curlSettingState;

    private static final String SLASH = "/";

    public void copyAsCUrl(@NotNull AnActionEvent actionEvent, CUrlClientType cUrlClientType) {
        Editor editor = actionEvent.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = actionEvent.getData(CommonDataKeys.PSI_FILE);
        Project project = actionEvent.getProject();
        PsiElement referenceAt = psiFile.findElementAt(editor.getCaretModel().getOffset());
        PsiClass selectedClass = PsiTreeUtil.getContextOfType(referenceAt, PsiClass.class);

        curlSettingState = ServiceManager.getService(project, CURLSettingState.class);

        PsiMethod selectedMethod = PsiTreeUtil.getContextOfType(referenceAt, PsiMethod.class);
        if (selectedMethod != null) {
//            PsiAnnotation[] annotations = selectedMethod.getAnnotations();
//            boolean postMethod = isPostMethod(selectedMethod);
            MethodInfo methodInfo = new MethodInfo(selectedMethod);
            String moduleName = getModuleName(editor, project);
            CURLModelInfo curlModelInfo = getCurlModelInfo(moduleName);

            assert curlModelInfo != null;
            String port = StringUtils.isEmpty(curlModelInfo.getPort()) ? getChooseOrInputPort() : curlModelInfo.getPort();
            StringBuilder stringBuilder = new StringBuilder("curl");

            // 访问接口
            stringBuilder.append(" '")
                    .append(getBaseApi(port))
                    .append(buildPath(selectedMethod));
            if (isGetMethod(selectedMethod.getAnnotations())) {
                // Get 请求参数
                stringBuilder.append(getRequestParams(selectedMethod, methodInfo, cUrlClientType));
                stringBuilder.append("'");
            } else {
                // 非Get请求参数
                stringBuilder.append("'");
                stringBuilder.append(getRequestBody(selectedMethod, methodInfo, cUrlClientType));
            }

            // 添加header
            List<String[]> headers = curlModelInfo.getHeaders();
            if (CollectionUtils.isNotEmpty(headers)) {
                for (String[] header : headers) {
                    stringBuilder.append(" -H '").append(header[0]).append(": ").append(header[1]).append("'");

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

    private String buildPath(PsiMethod psiMethod) {
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
        return classPath + methodPath;
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
        for (CURLModelInfo curlModelInfo : curlSettingState.modelInfoList) {
            if (curlModelInfo.getModuleName().equals(moduleName)) {
                return curlModelInfo.getPort();
            }
        }
        return "";
    }

    private CURLModelInfo getCurlModelInfo(String moduleName) {
        if (StringUtils.isNotEmpty(moduleName)) {
            for (CURLModelInfo curlModelInfo : curlSettingState.modelInfoList) {
                if (curlModelInfo.getModuleName().equals(moduleName)) {
                    return curlModelInfo;
                }
            }
        }
        return null;
    }

    private String getChooseOrInputPort() {
        List<String> strings = new ArrayList<>();
        for (CURLModelInfo info : curlSettingState.modelInfoList) {
            if (StringUtils.isNotEmpty(info.getPort())) {
                strings.add(info.getPort() + ":" + info.getModuleName());
            }
        }
        String[] modelWithPort = strings.toArray(new String[0]);
        String s = Messages.showEditableChooseDialog("请选择或输入端口", "提示", null, modelWithPort, "8080", null);
        assert s != null;
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

    public String getRequestParams(PsiMethod psiMethod, MethodInfo methodInfo, CUrlClientType cUrlClientType) {
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
            return Collections.emptyList();
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
                children.removeIf(child -> !includeFiledList.get(index).contains(child.getName()));
            } else if (excludeFiledList.size() > index && StringUtils.isNotEmpty(excludeFiledList.get(index))) {
                children.removeIf(child -> excludeFiledList.get(index).contains(child.getName()));
            }
            if (filterFieldInfo.excludeChildren) {
                for (FieldInfo child : children) {
                    child.setChildren(Collections.emptyList());
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

}

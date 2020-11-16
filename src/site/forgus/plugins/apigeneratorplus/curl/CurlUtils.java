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
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ui.TextTransferable;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import site.forgus.plugins.apigeneratorplus.constant.CUrlClientType;
import site.forgus.plugins.apigeneratorplus.constant.WebAnnotation;
import site.forgus.plugins.apigeneratorplus.curl.model.CURLModelInfo;
import site.forgus.plugins.apigeneratorplus.normal.FieldInfo;
import site.forgus.plugins.apigeneratorplus.normal.MethodInfo;
import site.forgus.plugins.apigeneratorplus.setting.CURLSettingState;
import site.forgus.plugins.apigeneratorplus.util.FieldUtil;
import site.forgus.plugins.apigeneratorplus.util.NotificationUtil;
import site.forgus.plugins.apigeneratorplus.util.StringUtil;
import site.forgus.plugins.apigeneratorplus.yapi.enums.RequestMethodEnum;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

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

        String moduleName = getModuleName(editor, project);
        String port = getPort(moduleName);
        PsiMethod selectedMethod = PsiTreeUtil.getContextOfType(referenceAt, PsiMethod.class);
        if (selectedMethod != null) {
            PsiAnnotation[] annotations = selectedMethod.getAnnotations();
            boolean postMethod = isPostMethod(selectedMethod);
            MethodInfo methodInfo = new MethodInfo(selectedMethod);
            StringBuilder stringBuilder = new StringBuilder("curl");

            // 访问接口
            stringBuilder.append(" '")
                    .append(getBaseApi(port))
                    .append(buildPath(selectedMethod));
            if (isGetMethod(selectedMethod.getAnnotations())) {
                // Get 请求参数
                stringBuilder.append(methodInfo.getCurlRequestParams(selectedMethod, cUrlClientType));
                stringBuilder.append("'");
            } else {
                // 非Get请求参数
                stringBuilder.append("'");
                stringBuilder.append(methodInfo.getCurlRequestBody(selectedMethod, cUrlClientType));
            }

            String curlStr = stringBuilder.toString();
            if (CUrlClientType.CMD.equals(cUrlClientType)) {
                curlStr = curlStr.replaceAll("'", "\"");
            }
            System.out.println(curlStr);
            NotificationUtil.infoNotify("已复制到剪切板", curlStr, project);
            CopyPasteManager.getInstance().setContents(new TextTransferable(curlStr));
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

//            VirtualFile moduleContentRoot = projectFileIndex.getContentRootForFile(virtualFile);
//            boolean isLibraryFile = projectFileIndex.isLibraryClassFile(virtualFile);
//            boolean isInLibraryClasses = projectFileIndex.isInLibraryClasses(virtualFile);
//            boolean isInLibrarySource = projectFileIndex.isInLibrarySource(virtualFile);
//            Messages.showInfoMessage("Module: " + moduleName + "\n" +
//                            "Module content root: " + moduleContentRoot + "\n" +
//                            "Is library file: " + isLibraryFile + "\n" +
//                            "Is in library classes: " + isInLibraryClasses +
//                            ", Is in library source: " + isInLibrarySource,
//                    "Main File Info for" + virtualFile.getName());
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
        String localIP = getRealIP();
        return "http://" + localIP + ":" + port;
    }

    public String getCurlRequestBody(CUrlClientType cUrlClientType, MethodInfo methodInfo) {
        List<FieldInfo> requestFields = methodInfo.getRequestFields();
        List<String> strings = generateKeyValue(requestFields);
        StringBuffer stringBuffer = new StringBuffer(" -d '");
        for (String string : strings) {
            stringBuffer.append(string).append(cUrlClientType.getSymbolAnd());
        }
        stringBuffer.append("'");
        return stringBuffer.toString();
    }

    private List<String> generateKeyValue(List<FieldInfo> fieldInfoList) {
        ArrayList<String> strings = new ArrayList<>();
        for (FieldInfo requestField : fieldInfoList) {
            if (requestField.hasChildren()) {
                strings.addAll(generateKeyValue(requestField.getChildren()));
            } else {
                strings.add(requestField.getName() + "=" + FieldUtil.getValue(requestField.getPsiType()));
            }
        }
        return strings;
    }
}
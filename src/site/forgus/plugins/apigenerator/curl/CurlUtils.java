package site.forgus.plugins.apigenerator.curl;

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
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ui.TextTransferable;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import site.forgus.plugins.apigenerator.constant.CUrlClientType;
import site.forgus.plugins.apigenerator.constant.WebAnnotation;
import site.forgus.plugins.apigenerator.curl.model.CURLModelInfo;
import site.forgus.plugins.apigenerator.normal.MethodInfo;
import site.forgus.plugins.apigenerator.setting.CURLSettingState;
import site.forgus.plugins.apigenerator.util.NotificationUtil;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

/**
 * @author lmx 2020/11/11 15:49
 */

public class CurlUtils {

    private CURLSettingState curlSettingState;

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
            MethodInfo methodInfo = new MethodInfo(selectedMethod);
            StringBuffer stringBuffer = new StringBuffer("curl ");
            stringBuffer.append(methodInfo.getCurlRequestBody(cUrlClientType));
            stringBuffer.append(" " + getBaseApi(port));
//            stringBuffer.append(" -H \"X-Auth-Token: 7d2661b6-4258-4e80-9ea2-761f9d5cd3c2\"");
            System.out.println(stringBuffer.toString());
            NotificationUtil.infoNotify("已复制到剪切板", stringBuffer.toString(), project);
            CopyPasteManager.getInstance().setContents(new TextTransferable(stringBuffer.toString()));
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

    public String getBaseApi(String port) {
        String localIP = getRealIP();
        return "http://" + localIP + ":" + port;
    }
}

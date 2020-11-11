package site.forgus.plugins.apigenerator.curl;

import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ui.TextTransferable;
import lombok.SneakyThrows;
import site.forgus.plugins.apigenerator.constant.CUrlClientType;
import site.forgus.plugins.apigenerator.constant.WebAnnotation;
import site.forgus.plugins.apigenerator.normal.MethodInfo;
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

    public void copyAsCUrl(Project project, PsiElement referenceAt, CUrlClientType cUrlClientType) {
        PsiMethod selectedMethod = PsiTreeUtil.getContextOfType(referenceAt, PsiMethod.class);
        if (selectedMethod != null) {
//            try {
            MethodInfo methodInfo = new MethodInfo(selectedMethod);
            StringBuffer stringBuffer = new StringBuffer("curl ");
            stringBuffer.append(methodInfo.getCurlRequestBody(cUrlClientType));
            stringBuffer.append(" " + getBaseApi());
            stringBuffer.append(" -H \"X-Auth-Token: 7d2661b6-4258-4e80-9ea2-761f9d5cd3c2\"");
            System.out.println(stringBuffer.toString());
            NotificationUtil.infoNotify("已复制到剪切板", stringBuffer.toString(), project);
            CopyPasteManager.getInstance().setContents(new TextTransferable(stringBuffer.toString()));
//            } catch (IOException e) {
//                NotificationUtil.errorNotify(e.getMessage(), project);
//            }
        }
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

    @SneakyThrows
    public String getLocalIP(){
        InetAddress inet = InetAddress.getLocalHost();
        System.out.println("本机的ip=" + inet.getHostAddress());
        return inet.getHostAddress();
    }

    public String getBaseApi(){
        String localIP = getRealIP();
        return "http://" + localIP;
    }
}

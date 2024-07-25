package site.forgus.plugins.apigeneratorplus.util;

import org.jetbrains.annotations.Nullable;
import site.forgus.plugins.apigeneratorplus.model.NetInterfaceWrap;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @author limaoxu
 * @date 2024-7-17 01:40:03
 */

public class NetworkUtil {

    @Nullable
    public static List<NetInterfaceWrap> getAll() {
        try {
            boolean checkedInterface = false;
            List<NetInterfaceWrap> netInterfaceWraps = new ArrayList<>();
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = allNetInterfaces.nextElement();
                // 去除回环接口，子接口，未运行的接口
                if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
                    continue;
                }

                NetInterfaceWrap wrap = new NetInterfaceWrap();
                String displayName = netInterface.getDisplayName();
                wrap.setDisplayName(displayName);

                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress ip = addresses.nextElement();
                    if (ip != null) {
                        // ipv4
                        if (ip instanceof Inet4Address) {
                            wrap.setIpV4(ip.getHostAddress());
                        }
                    }
                }
                // 过滤掉没有ip地址的网络接口
                if (AssertUtils.isEmpty(wrap.getIpV4())) {
                    continue;
                }

                if (!checkedInterface && (!displayName.contains("Virtual") && !displayName.contains("Adapter"))) {
                    checkedInterface = true;
                    wrap.setChecked(true);
                }


                netInterfaceWraps.add(wrap);
            }
            return netInterfaceWraps;
        } catch (SocketException e) {
            //捕获异常
        }
        return null;
    }

}

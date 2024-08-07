package site.forgus.plugins.apigeneratorplus.util;

import org.jetbrains.annotations.Contract;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lmx 2021/1/13 18:14
 */

public class DeepCloneUtil {

    @Contract("null -> null")
    public static <T extends Serializable> List<T> deepCloneList(List<T> list) {
        if (null == list) {
            return null;
        }
        if (list.isEmpty()) {
            return new ArrayList<>();
        }
        List<T> cloneList = new ArrayList<>();
        for (T item : list) {
            cloneList.add(deepClone(item));
        }
        return cloneList;
    }

    /**
     * 深层复制序列化 vo
     *
     * @param src
     * @return dest
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static <T> T deepClone(T src) {
        ByteArrayOutputStream bo = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        T dest = null;
        try {
            try {
                //对象写入内存
                bo = new ByteArrayOutputStream();
                out = new ObjectOutputStream(bo);
                out.writeObject(src);
                //从内存中读回来
                in = new ObjectInputStream(new ByteArrayInputStream(bo.toByteArray()));
                dest = (T) in.readObject();
            } finally {
                //使用 finally 关闭资源
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
                if (bo != null) {
                    bo.close();
                }
            }
            //使用 catch 块统一捕捉资源
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }

        return dest;
    }

}

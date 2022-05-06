package io.choerodon.kb.infra.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import org.apache.commons.lang3.StringUtils;

/**
 * @author lei.cao01@hand-china.com
 * @version 1.0.0
 * @ClassName FileUtil.java
 * @Description TODO
 * @createTime 2021年06月30日 16:45:00
 */
public class CommonUtil {

    public static String utf8Code(String str) {
        try {
            return URLEncoder.encode(str, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String getFileId(String fileKey) {
        return fileKey.substring(fileKey.lastIndexOf("/") + 1, fileKey.indexOf("@"));
    }

    public static String getFileType(String fileKey) {
        int index = fileKey.lastIndexOf(".");
        if (index > -1) {
            return fileKey.substring(index + 1);
        } else {
          return "";
        }
    }

    public static String decoderFileKey(String fileKey) {
        //测试wps新版本是否需要base64
        try {
            String encode = URLEncoder.encode(fileKey, "UTF-8");
            return URLDecoder.decode(encode, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getFileName(String fileKey) {
        int index = fileKey.indexOf("@");
        if (index > -1) {
            return fileKey.substring(index + 1);
        } else {
            String[] s = fileKey.split("/");
            return s[s.length - 1];
        }
    }

}
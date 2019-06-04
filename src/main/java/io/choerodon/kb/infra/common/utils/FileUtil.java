package io.choerodon.kb.infra.common.utils;

import java.io.*;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.infra.common.BaseStage;

/**
 * Created by Zenger on 2019/6/3.
 */
public class FileUtil {

    private static ResourceLoader resourceLoader = new DefaultResourceLoader();
    private static String classPath;
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);


    private FileUtil() {

    }

    static {
        try {
            classPath = resourceLoader.getResource("/").getURI().getPath();
            String repositoryPath = classPath == null ? "" : classPath + BaseStage.BACKETNAME;
            File repo = new File(repositoryPath);
            if (!repo.exists()) {
                repo.mkdirs();
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * 通过inputStream流 替换文件的参数
     *
     * @param inputStream 流
     * @param params      参数
     * @return String
     */
    public static String replaceReturnString(InputStream inputStream, Map<String, String> params) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            byte[] b = new byte[31 * 1024 * 1024];
            for (int n; (n = inputStream.read(b)) != -1; ) {
                String content = new String(b, 0, n);
                if (params != null) {
                    for (Object o : params.entrySet()) {
                        Map.Entry entry = (Map.Entry) o;
                        content = content.replace(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
                    }
                }
                stringBuilder.append(content);
                stringBuilder.append(System.getProperty("line.separator"));
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            throw new CommonException("error.param.render");
        }
    }

    public static FileItem createFileItem(File file, String fieldName, String mimeType) {
        FileItemFactory factory = new DiskFileItemFactory(16, null);
        FileItem item = factory.createItem(BaseStage.FILENAME, mimeType, true, fieldName);
        int bytesRead = 0;
        byte[] buffer = new byte[31 * 1024 * 1024];
        try {
            FileInputStream fis = new FileInputStream(file);
            OutputStream os = item.getOutputStream();
            while ((bytesRead = fis.read(buffer, 0, 31 * 1024 * 1024)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            fis.close();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return item;
    }

    // 创建临时文件
    public static File getFileFromBytes(byte[] byt, String name) {
        File file = new File(getWorkingDirectory(name));
        OutputStream output = null;
        try {
            output = new FileOutputStream(file);
            BufferedOutputStream bufferedOutput = new BufferedOutputStream(output);
            bufferedOutput.write(byt);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        return file;
    }

    private static String getWorkingDirectory(String name) {
        String path = classPath == null ? BaseStage.BACKETNAME + "/" + name : classPath + BaseStage.BACKETNAME + "/" + name;
        return path.replace("/", File.separator);
    }

    private static void deleteWorkingDirectory(String name) {
        String path = getWorkingDirectory(name);
        File file = new File(path);
        if (file.exists()) {
            deleteDir(file);
        }
    }

    private static Boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                Boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }
}

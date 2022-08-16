package io.choerodon.kb.infra.utils;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.util.Charsets;

import io.choerodon.core.exception.CommonException;

/**
 * @author shinan.chen
 * @since 2019/6/13
 */
public class HtmlUtil {

    private HtmlUtil() {
        throw new UnsupportedOperationException();
    }

    public static final String FONT_PATH = "/font/Alibaba-PuHuiTi-Regular.ttf";

    /**
     * 加载html模板
     * @param path
     * @return
     * @throws IOException
     */
    public static String loadHtmlTemplate(String path) throws IOException {
        InputStream inputStream = HtmlUtil.class.getResourceAsStream(path);
        String html;
        try {
            html = IOUtils.toString(inputStream, String.valueOf(Charsets.UTF_8));
        } catch (IOException e) {
            throw new CommonException(e);
        } finally {
            inputStream.close();
        }
        return html;
    }
}

package io.choerodon.kb.infra.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import javax.servlet.http.HttpServletResponse;

import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.pdf.converter.PdfConverterExtension;
import com.vladsch.flexmark.profiles.pegdown.Extensions;
import com.vladsch.flexmark.profiles.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.options.MutableDataHolder;
import org.apache.commons.io.IOUtils;

import io.choerodon.core.exception.CommonException;

/**
 * @author shinan.chen
 * @since 2019/6/5
 */
public class PdfUtil {
    static final MutableDataHolder OPTIONS = PegdownOptionsAdapter.flexmarkOptions(
            Extensions.ALL & ~(Extensions.ANCHORLINKS | Extensions.EXTANCHORLINKS_WRAP)
            , TocExtension.create()).toMutable()
            .set(TocExtension.LIST_CLASS, PdfConverterExtension.DEFAULT_TOC_LIST_CLASS)
//            .set(HtmlRenderer.GENERATE_HEADER_ID, true)
            //.set(HtmlRenderer.RENDER_HEADER_ID, true)
            ;

    private static void getResourceFileContent(final StringWriter writer, final String resourcePath) {
        InputStream inputStream = PdfUtil.class.getResourceAsStream(resourcePath);
        try {
            IOUtils.copy(inputStream, writer, "UTF-8");
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void markdown2Pdf(String title, String markdownString, HttpServletResponse response) {
        final Parser PARSER = Parser.builder(OPTIONS).build();
        final HtmlRenderer RENDERER = HtmlRenderer.builder(OPTIONS).build();

        Node document = PARSER.parse(markdownString);
        String htmlBody = RENDERER.render(document);
        String fontUrl = "'" + PdfUtil.class.getResource("/font/PingFang-SC-Regular.ttf") + "'";
        String html;
        try {
            html = HtmlUtil.loadHtmlTemplate("/htmlTemplate/pdfHtml.html");
        } catch (IOException e) {
            throw new CommonException(e.getMessage());
        }
        html = html.replace("{pdf:fontUrl}", fontUrl);
        html = html.replace("{pdf:title}", title);
        html = html.replace("{pdf:body}", htmlBody);
        try {
            String disposition = "attachment;filename=\"title.pdf\"";
            response.setContentType("application/pdf");
            response.setCharacterEncoding("utf-8");
            response.addHeader("Content-Disposition", disposition);
            PdfConverterExtension.exportToPdf(response.getOutputStream(), html, "", OPTIONS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

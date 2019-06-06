package io.choerodon.kb.infra.common.utils;

import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.pdf.converter.PdfConverterExtension;
import com.vladsch.flexmark.profiles.pegdown.Extensions;
import com.vladsch.flexmark.profiles.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.options.MutableDataHolder;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * @author shinan.chen
 * @since 2019/6/5
 */
public class PdfUtil {
    static final MutableDataHolder OPTIONS = PegdownOptionsAdapter.flexmarkOptions(
            Extensions.ALL & ~(Extensions.ANCHORLINKS | Extensions.EXTANCHORLINKS_WRAP)
            , TocExtension.create()).toMutable()
            .set(TocExtension.LIST_CLASS, PdfConverterExtension.DEFAULT_TOC_LIST_CLASS)
            //.set(HtmlRenderer.GENERATE_HEADER_ID, true)
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
        String html = RENDERER.render(document);
        String url = "'" + PdfUtil.class.getResource("/font/arialuni.ttf") + "'";
        String style = "<style>\n" +
                "@font-face {\n" +
                "  font-family: 'font';\n" +
                "  src: url(" + url + ");\n" +
                "}\n" +
                "* {\n" +
                "    font-family: 'font';\n" +
                "}\n" +
                "var,\n" +
                "code,\n" +
                "kbd,\n" +
                "pre {\n" +
                "    font: 0.9em 'font';\n" +
                "}\n" +
                "code {\n" +
                "    color:#c1788b;\n" +
                "}\n" +
                "pre {\n" +
                "    background-color:#f5f7f8; padding:18px\n" +
                "}\n" +
                "pre code{\n" +
                "    color:#000000;\n" +
                "}\n" +
                "img {\n" +
                "    max-width: 100%\n" +
                "}\n" +
                "</style>";
        html = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">\n" + style +
                "</head><body>" + html + "\n" +
                "</body></html>";

        try {
            String disposition = String.format("attachment;filename=\"%s.pdf\"", title);
            response.setContentType("application/pdf");
            response.setCharacterEncoding("utf-8");
            response.addHeader("Content-Disposition", disposition);
            PdfConverterExtension.exportToPdf(response.getOutputStream(), html, "", OPTIONS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

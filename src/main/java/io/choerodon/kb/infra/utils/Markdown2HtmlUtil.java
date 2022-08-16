package io.choerodon.kb.infra.utils;

import java.io.IOException;
import java.net.URL;

import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.pdf.converter.PdfConverterExtension;
import com.vladsch.flexmark.profiles.pegdown.Extensions;
import com.vladsch.flexmark.profiles.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.options.MutableDataHolder;

import io.choerodon.core.exception.CommonException;

/**
 * Created by Zenger on 2019/5/8.
 */
public class Markdown2HtmlUtil {

    private Markdown2HtmlUtil() {
        throw new UnsupportedOperationException();
    }

    public static final MutableDataHolder OPTIONS = PegdownOptionsAdapter.flexmarkOptions(
                    Extensions.ALL & ~(Extensions.ANCHORLINKS | Extensions.EXTANCHORLINKS_WRAP),
                    TocExtension.create()
            )
            .toMutable()
            .set(TocExtension.LIST_CLASS, PdfConverterExtension.DEFAULT_TOC_LIST_CLASS);

    public static String markdown2html(String title, String markdownString) {
        final Parser PARSER = Parser.builder(OPTIONS).build();
        final HtmlRenderer RENDERER = HtmlRenderer.builder(OPTIONS).build();

        Node document = PARSER.parse(markdownString);
        String htmlBody = RENDERER.render(document);
        URL url = Markdown2HtmlUtil.class.getResource(HtmlUtil.FONT_PATH);
        if (url == null) {
            throw new CommonException("error.pdf.font.file.not.found");
        }
        String fontUrl = "'" + url + "'";
        String html;
        try {
            html = HtmlUtil.loadHtmlTemplate("/htmlTemplate/pdfHtml.html");
        } catch (IOException e) {
            throw new CommonException(e.getMessage());
        }
        html = html.replace("{pdf:fontUrl}", fontUrl)
                .replace("{pdf:title}", title)
                .replace("{pdf:body}", htmlBody);
        return html;
    }

}

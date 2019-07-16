package io.choerodon.kb.infra.utils;

import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.ins.InsExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.options.MutableDataSet;

import java.util.Arrays;

/**
 * Created by Zenger on 2019/5/8.
 */
public class Markdown2HtmlUtil {

    private Markdown2HtmlUtil() {
    }

    private static Parser parser;
    private static HtmlRenderer renderer;

    static {
        MutableDataSet options = new MutableDataSet().set(Parser.EXTENSIONS, Arrays.asList(
                StrikethroughExtension.create(),
                TablesExtension.create(),
                TaskListExtension.create(),
                TocExtension.create(),
                InsExtension.create(),
                YamlFrontMatterExtension.create()
        )).set(HtmlRenderer.SOFT_BREAK, "<br>\n")
                .set(TocExtension.LEVELS, 255)
                .set(TocExtension.DIV_CLASS, "toc")
                .set(TocExtension.TITLE, "目录")
                .set(TocExtension.IS_NUMBERED, true)
                .set(TocExtension.IS_HTML, true);
        parser = Parser.builder(options).build();
        renderer = HtmlRenderer.builder(options).build();
    }

    public static String markdown2Html(String mdSyntax) {
        Node document = parser.parse(mdSyntax);
        return renderer.render(document);
    }
}

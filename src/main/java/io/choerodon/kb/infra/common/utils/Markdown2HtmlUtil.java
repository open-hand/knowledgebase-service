package io.choerodon.kb.infra.common.utils;

import java.util.Arrays;
import java.util.List;

import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.front.matter.YamlFrontMatterExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.ins.InsExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

/**
 * Created by Zenger on 2019/5/8.
 */
public class Markdown2HtmlUtil {

    public static String markdown2Html(String mdSyntax) {
        List<Extension> extensions = Arrays.asList(TablesExtension.create(),
                StrikethroughExtension.create(),
                YamlFrontMatterExtension.create(),
                InsExtension.create(),
                AutolinkExtension.create());
        Parser parser = Parser.builder()
                .extensions(extensions)
                .build();
        Node document = parser.parse(mdSyntax);
        HtmlRenderer renderer = HtmlRenderer.builder()
                .extensions(extensions)
                .build();
        return renderer.render(document);
    }
}

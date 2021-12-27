package io.choerodon.kb.infra.utils;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.pdf.converter.PdfConverterExtension;
import com.vladsch.flexmark.util.ast.Node;
import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.vo.WatermarkVO;

import javax.servlet.http.HttpServletResponse;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.math.BigInteger;
import java.net.URL;

/**
 * @author shinan.chen
 * @since 2019/6/5
 */
public class PdfProUtil extends PdfUtil {
    /**
     * 透明度
     */
    private static final Float FILL_OPACITY = 0.1F;
    /**
     * 字体颜色
     */
    private static final String COLOR = "#0f1358";
    /**
     * 字体大小
     */
    private static final Float FONT_SIZE = 16F;
    /**
     * 横坐标
     */
    private static final Long X_AXIS = 100L;
    /**
     * 纵坐标
     */
    private static final Long Y_AXIS = 100L;
    /**
     * 对齐方式，0左对齐1居中2右对齐
     */
    private static final Integer ALIGN = 1;
    /**
     * 倾斜角度
     */
    private static final Float ROTATION = -16F;

    private static final Long INTERVAL = -40L;

    private static final String FONT_PATH = "/font/Alibaba-PuHuiTi-Regular.ttf";


    public static void markdown2Pdf(String title,
                                    String markdownString,
                                    HttpServletResponse response,
                                    WatermarkVO waterMark) {
        final Parser PARSER = Parser.builder(OPTIONS).build();
        final HtmlRenderer RENDERER = HtmlRenderer.builder(OPTIONS).build();

        Node document = PARSER.parse(markdownString);
        String htmlBody = RENDERER.render(document);
        URL url = PdfProUtil.class.getResource(FONT_PATH);
        if (url == null) {
            throw new CommonException("error.pdf.font.file.not.found");
        }
        StringBuilder fontUrlBuilder = new StringBuilder();
        fontUrlBuilder
                .append("'")
                .append(url)
                .append("'");
        String fontUrl = fontUrlBuilder.toString();
        String html;
        try {
            html = HtmlUtil.loadHtmlTemplate("/htmlTemplate/pdfHtml.html");
        } catch (IOException e) {
            throw new CommonException(e.getMessage());
        }
        html = html.replace("{pdf:fontUrl}", fontUrl);
        html = html.replace("{pdf:title}", title);
        html = html.replace("{pdf:body}", htmlBody);

        String fileName = "title.pdf";
        if (waterMark != null && Boolean.TRUE.equals(waterMark.getDoWaterMark())) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            PdfConverterExtension.exportToPdf(bos, html, "", OPTIONS);
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            watermarkText(response, bis, fileName, waterMark.getWaterMarkString());
        } else {
            seHttpServletResponseHeader(response, fileName);
            try {
                PdfConverterExtension.exportToPdf(response.getOutputStream(), html, "", OPTIONS);
            } catch (IOException e) {
                throw new CommonException("error.io.exception.to.response", e);
            }
        }
    }

    private static void watermarkText(HttpServletResponse response,
                                      InputStream inputStream,
                                      String fileName,
                                      String waterMarkString) {
        seHttpServletResponseHeader(response, fileName);
        PdfStamper stamper = null;
        try {
            OutputStream outputStream = response.getOutputStream();
            PdfReader pdfReader = new PdfReader(inputStream);
            stamper = new PdfStamper(pdfReader, outputStream);
            int total = pdfReader.getNumberOfPages() + 1;
            URL url = PdfProUtil.class.getResource(FONT_PATH);
            if (url != null) {
                BaseFont font = BaseFont.createFont(FONT_PATH, "Identity-H", false);
                //水印平铺
                JLabel label = new JLabel();
                label.setText(waterMarkString);
                FontMetrics metrics = label.getFontMetrics(label.getFont());
                int textH = metrics.getHeight();
                int textW = metrics.stringWidth(label.getText());
                PdfGState gs = new PdfGState();
                gs.setFillOpacity(FILL_OPACITY);
                for (int i = 1; i < total; ++i) {
                    Rectangle pageRect = pdfReader.getPageSizeWithRotation(i);
                    PdfContentByte content = stamper.getOverContent(i);
                    content.beginText();
                    content.setColorFill(getColor(COLOR));
                    content.setGState(gs);
                    content.setFontAndSize(font, FONT_SIZE);
                    content.setTextMatrix((float) X_AXIS, (float) Y_AXIS);
                    for (long height = INTERVAL + (long) textH; (float) height < pageRect.getHeight() + (float) textH; height = height + (long) textH + Y_AXIS) {
                        for (long width = INTERVAL + (long) textW; (float) width < pageRect.getWidth() + (float) textW; width = width + (long) textW + X_AXIS) {
                            content.showTextAligned(ALIGN, waterMarkString, (float) width, (float) height, ROTATION);
                        }
                    }
                    content.endText();
                }
            } else {
                throw new CommonException("error.pdf.font.file.not.found");
            }
        } catch (IOException | DocumentException e) {
            throw new CommonException("error.pdf.write.watermark", e);
        } finally {
            if (stamper != null) {
                try {
                    stamper.close();
                } catch (DocumentException | IOException e) {
                    throw new CommonException("error.close.stamper", e);
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new CommonException("error.close.inputStream", e);
                }
            }
        }
    }

    private static void seHttpServletResponseHeader(HttpServletResponse response, String fileName) {
        response.setHeader("Content-Disposition", "inline;filename=" + fileName);
        response.setContentType("application/pdf");
        response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
        response.setHeader("Pragma", "public");
        response.setDateHeader("Expires", System.currentTimeMillis() + 1000L);
        response.setCharacterEncoding("utf-8");
    }

    private static BaseColor getColor(String color) {
        String red = color.substring(1, 3);
        String green = color.substring(3, 5);
        String blue = color.substring(5, 7);
        return new BaseColor(decodeHex(red), decodeHex(green), decodeHex(blue));
    }

    public static int decodeHex(String hex) {
        BigInteger bigint = new BigInteger(hex, 16);
        return bigint.intValue();
    }

}

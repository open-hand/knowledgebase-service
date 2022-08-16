package io.choerodon.kb.infra.utils;

import java.awt.*;
import java.io.*;
import java.net.URL;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.swing.*;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import com.vladsch.flexmark.pdf.converter.PdfConverterExtension;
import org.apache.commons.lang3.StringUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.vo.WatermarkVO;

/**
 * @author shinan.chen
 * @since 2019/6/5
 */
public class PdfUtil {

    /**
     * 透明度
     */
    private static final Float FILL_OPACITY = 0.1F;
    /**
     * 字体颜色: #0f1358
     */
    private static final BaseColor COLOR = new BaseColor(
            Integer.parseInt("0f",16),
            Integer.parseInt("13",16),
            Integer.parseInt("58",16)
    );
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

    /**
     * Markdown转化为PDF并推送下载
     * @param title 标题
     * @param markdownString markdown内容
     * @param response http输出流
     * @param waterMark 水印设置
     */
    public static void markdown2Pdf(String title,
                                    String markdownString,
                                    HttpServletResponse response,
                                    WatermarkVO waterMark) {
        String html = Markdown2HtmlUtil.markdown2html(title, markdownString);
        String fileName = "title.pdf";
        seHttpServletResponseHeaderForPdfDownload(response, fileName);
        try {
            final ServletOutputStream responseOutputStream = response.getOutputStream();
            if (waterMark != null && Boolean.TRUE.equals(waterMark.getDoWaterMark())) {
                // 如果有水印, 则先将pdf输出到临时内存
                ByteArrayOutputStream tempOutputStream = new ByteArrayOutputStream();
                PdfConverterExtension.exportToPdf(tempOutputStream, html, StringUtils.EMPTY, Markdown2HtmlUtil.OPTIONS);
                // 再重新绘制水印
                printWatermark(responseOutputStream, new ByteArrayInputStream(tempOutputStream.toByteArray()), waterMark.getWaterMarkString());
            } else {
                // 不需要水印, 直接输出
                PdfConverterExtension.exportToPdf(responseOutputStream, html, StringUtils.EMPTY, Markdown2HtmlUtil.OPTIONS);
            }
        } catch (IOException e) {
            throw new CommonException("error.io.exception.to.response", e);
        }
    }

    /**
     * 添加水印
     * @param outputStream 输出目标
     * @param inputStream 原文内容所在的输入流
     * @param waterMarkString 水印文本
     */
    private static void printWatermark(OutputStream outputStream,
                                       InputStream inputStream,
                                       String waterMarkString) {

        PdfStamper stamper = null;
        try {
            PdfReader pdfReader = new PdfReader(inputStream);
            stamper = new PdfStamper(pdfReader, outputStream);
            int total = pdfReader.getNumberOfPages() + 1;
            URL url = PdfUtil.class.getResource(HtmlUtil.FONT_PATH);
            if (url == null) {
                throw new CommonException("error.pdf.font.file.not.found");
            }
            BaseFont font = BaseFont.createFont(HtmlUtil.FONT_PATH, "Identity-H", false);
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
                content.setColorFill(COLOR);
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

    /**
     * 设置HTTP请求头(For Download)
     * @param response HTTP Response
     * @param fileName 下载文件名
     */
    private static void seHttpServletResponseHeaderForPdfDownload(HttpServletResponse response, String fileName) {
        response.setHeader("Content-Disposition", "inline;filename=" + fileName);
        response.setContentType("application/pdf");
        response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
        response.setHeader("Pragma", "public");
        response.setDateHeader("Expires", System.currentTimeMillis() + 1000L);
        response.setCharacterEncoding("utf-8");
    }

}

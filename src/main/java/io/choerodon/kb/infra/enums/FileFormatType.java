package io.choerodon.kb.infra.enums;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangxiang on 2022/5/19
 */
public class FileFormatType {
    private FileFormatType() {
    }

    public static List<String> ONLY_FILE_FORMATS = new ArrayList<>();

    public static List<String> WPS_FILE_FORMATS = new ArrayList<>();

    static {
        //wps支持的类型
        WPS_FILE_FORMATS.add("DOC");
        WPS_FILE_FORMATS.add("DOCX");
        WPS_FILE_FORMATS.add("XLSX");
        WPS_FILE_FORMATS.add("XLS");
        WPS_FILE_FORMATS.add("XLSM");
        WPS_FILE_FORMATS.add("CSV");
        WPS_FILE_FORMATS.add("PPT");
        WPS_FILE_FORMATS.add("PPTX");
        WPS_FILE_FORMATS.add("PPS");
        WPS_FILE_FORMATS.add("PPSX");
        WPS_FILE_FORMATS.add("PDF");
        WPS_FILE_FORMATS.add("MP4");

        //onlyOffice支持的类型
        ONLY_FILE_FORMATS.add("DOCX");
        ONLY_FILE_FORMATS.add("XLSX");
        ONLY_FILE_FORMATS.add("PPTX");
        ONLY_FILE_FORMATS.add("PDF");
        ONLY_FILE_FORMATS.add("MP4");
    }
}

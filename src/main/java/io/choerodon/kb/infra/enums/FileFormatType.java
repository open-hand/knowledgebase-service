package io.choerodon.kb.infra.enums;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangxiang on 2022/5/19
 */
public class FileFormatType {
    private FileFormatType() {
    }

    public static List<String> FILE_FORMATS = new ArrayList<>();

    static {
        FILE_FORMATS.add("DOC");
        FILE_FORMATS.add("DOCX");
        FILE_FORMATS.add("XLSX");
        FILE_FORMATS.add("XLS");
        FILE_FORMATS.add("XLSM");
        FILE_FORMATS.add("CSV");
        FILE_FORMATS.add("PPT");
        FILE_FORMATS.add("PPTX");
        FILE_FORMATS.add("PPS");
        FILE_FORMATS.add("PPSX");
    }
}

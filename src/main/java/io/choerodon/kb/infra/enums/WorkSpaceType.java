package io.choerodon.kb.infra.enums;

/**
 * Created by wangxiang on 2022/4/27
 */
public enum WorkSpaceType {
    /**
     * 文档类型
     */
    DOCUMENT("document"),
    /**
     * 文件类型
     */
    FILE("file"),
    /**
     * 文件夹类型
     */
    FOLDER("folder");
    private String value;

    WorkSpaceType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}

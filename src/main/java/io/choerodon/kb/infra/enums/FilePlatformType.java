package io.choerodon.kb.infra.enums;

/**
 * Created by wangxiang on 2022/6/7
 */
public enum FilePlatformType {
    WPS("WPS"),
    ONLY_OFFICE("OnlyOffice");

    private String type;

    FilePlatformType(String type) {
        this.type = type;
    }

    public String getPlatformType() {
        return type;
    }
}

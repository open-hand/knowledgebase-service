package io.choerodon.kb.infra.enums;

/**
 * Created by wangxiang on 2022/6/7
 */
public enum FileSourceType {
    UPLOAD("UPLOAD"),
    COPY("COPY");
    private String type;

    FileSourceType(String type) {
        this.type = type;
    }

    public String getFileSourceType() {
        return type;
    }
}

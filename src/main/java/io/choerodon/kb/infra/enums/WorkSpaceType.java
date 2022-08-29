package io.choerodon.kb.infra.enums;


import io.choerodon.core.exception.CommonException;
import org.springframework.lang.NonNull;

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

    public static WorkSpaceType of(@NonNull String type) {
        try {
            return valueOf(type.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new CommonException("error.work.space.type.transfer", type);
        }
    }
}

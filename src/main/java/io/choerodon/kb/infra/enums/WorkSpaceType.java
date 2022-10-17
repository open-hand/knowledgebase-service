package io.choerodon.kb.infra.enums;


import org.springframework.lang.NonNull;

import io.choerodon.core.exception.CommonException;

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

    public static PermissionConstants.PermissionTargetBaseType queryPermissionTargetBaseTypeByType(String type) {
        if (FOLDER.value.equals(type)) {
            return PermissionConstants.PermissionTargetBaseType.FOLDER;
        } else if (DOCUMENT.value.equals(type) || FILE.value.equals(type)) {
            return PermissionConstants.PermissionTargetBaseType.FILE;
        } else {
            return null;
        }
    }

}

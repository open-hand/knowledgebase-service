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

    /**
     * 转化为权限控制对象基本类型
     * @param type  work space类型
     * @return      权限控制对象基本类型
     */
    public static PermissionConstants.PermissionTargetBaseType toTargetBaseType(String type) {
        if (FOLDER.value.equals(type)) {
            return PermissionConstants.PermissionTargetBaseType.FOLDER;
        } else if (DOCUMENT.value.equals(type) || FILE.value.equals(type)) {
            return PermissionConstants.PermissionTargetBaseType.FILE;
        } else {
            return null;
        }
    }

    public static PermissionConstants.ActionPermission queryReadActionByType(String type) {
        if (FOLDER.value.equals(type)) {
            return PermissionConstants.ActionPermission.FOLDER_READ;
        } else if (DOCUMENT.value.equals(type)) {
            return PermissionConstants.ActionPermission.DOCUMENT_READ;
        } else if (FILE.value.equals(type)) {
            return PermissionConstants.ActionPermission.FILE_READ;
        } else {
            return null;
        }
    }

    /**
     * 获取合法的父级类型
     * @param type  当前类型
     * @return      合法的父级类型
     */
    public static String[] queryValidParentType(String type) {
        if (FOLDER.value.equals(type)) {
            return new String[]{FOLDER.value};
        } else if (DOCUMENT.value.equals(type)) {
            return new String[]{FOLDER.value, DOCUMENT.value};
        } else if (FILE.value.equals(type)) {
            return new String[]{FOLDER.value};
        } else {
            return new String[]{};
        }
    }

}

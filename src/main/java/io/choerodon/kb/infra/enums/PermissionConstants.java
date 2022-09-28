package io.choerodon.kb.infra.enums;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import org.hzero.core.base.BaseConstants;
import org.hzero.core.message.MessageAccessor;

/**
 * 知识库权限管理通用常量
 *
 * @author gaokuo.dai@zknow.com 2022-09-23
 */
public class PermissionConstants {

    private PermissionConstants() {
        throw new UnsupportedOperationException();
    }

    /**
     * 空ID占位符
     */
    public static final Long EMPTY_ID_PLACEHOLDER = BaseConstants.DEFAULT_TENANT_ID;

    /**
     * 操作权限
     *
     * @author gaokuo.dai@zknow.com 2022-09-23
     */
    public enum ActionPermission {

        // 知识库操作权限
        /**
         * 知识库-设置
         */
        KNOWLEDGE_BASE_SETTINGS("knowledge-base.settings"),
        /**
         * 知识库-删除
         */
        KNOWLEDGE_BASE_DELETE("knowledge-base.delete"),
        /**
         * 知识库-回收站-恢复
         */
        KNOWLEDGE_BASE_RECOVER("knowledge-base.recover"),
        /**
         * 知识库-回收站-永久删除
         */
        KNOWLEDGE_BASE_PERMANENTLY_DELETE("knowledge-base.permanently-delete"),
        /**
         * 知识库-管理协作者
         */
        KNOWLEDGE_BASE_COLLABORATORS("knowledge-base.collaborators"),
        /**
         * 知识库-安全设置
         */
        KNOWLEDGE_BASE_SECURITY_SETTINGS("knowledge-base.security-settings"),

        // 文件夹操作权限
        /**
         * 文件夹-创建
         */
        FOLDER_CREATE("folder.create"),
        /**
         * 文件夹-重命名
         */
        FOLDER_RENAME("folder.rename"),
        /**
         * 文件夹-移动
         */
        FOLDER_MOVE("folder.move"),
        /**
         * 文件夹-删除
         */
        FOLDER_DELETE("folder.delete"),
        /**
         * 文件夹-回收站-恢复
         */
        FOLDER_RECOVER("folder.recover"),
        /**
         * 文件夹-回收站-永久删除
         */
        FOLDER_PERMANENTLY_DELETE("folder.permanently-delete"),
        /**
         * 文件夹-管理协作者
         */
        FOLDER_COLLABORATORS("folder.collaborators"),
        /**
         * 文件夹-安全设置
         */
        FOLDER_SECURITY_SETTINGS("folder.security-settings"),

        // MD文档操作权限
        /**
         * MD文档-创建
         */
        DOCUMENT_CREATE("document.create"),
        /**
         * MD文档-编辑
         */
        DOCUMENT_EDIT("document.edit"),
        /**
         * MD文档-重命名
         */
        DOCUMENT_RENAME("document.rename"),
        /**
         * MD文档-移动至
         */
        DOCUMENT_MOVE("document.move"),
        /**
         * MD文档-复制
         */
        DOCUMENT_COPY("document.copy"),
        /**
         * MD文档-下载为PDF
         */
        DOCUMENT_DOWNLOAD_TO_PDF("document.download-to-pdf"),
        /**
         * MD文档-操作历史
         */
        DOCUMENT_OPERATING_HISTORY("document.operating-history"),
        /**
         * MD文档-版本对比-查看
         */
        DOCUMENT_VIEW_VERSION("document.view-version"),
        /**
         * MD文档-版本对比-回滚
         */
        DOCUMENT_ROLL_BACK("document.roll-back"),
        /**
         * MD文档-删除
         */
        DOCUMENT_DELETE("document.delete"),
        /**
         * MD文档-删除回收站-恢复
         */
        DOCUMENT_RECOVER("document.recover"),
        /**
         * MD文档-回收站-永久删除
         */
        DOCUMENT_PERMANENTLY_DELETE("document.permanently-delete"),
        /**
         * MD文档-分享
         */
        DOCUMENT_SHARE("document.share"),
        /**
         * MD文档-管理协作者
         */
        DOCUMENT_COLLABORATORS("document.collaborators"),
        /**
         * MD文档-安全设置
         */
        DOCUMENT_SECURITY_SETTINGS("document.security-settings"),

        // 其他文档操作权限
        /**
         * 其他文档-创建
         */
        FILE_CREATE("file.create"),
        /**
         * 其他文档-编辑
         */
        FILE_EDIT("file.edit"),
        /**
         * 其他文档-重命名
         */
        FILE_RENAME("file.rename"),
        /**
         * 其他文档-移动至
         */
        FILE_MOVE("file.move"),
        /**
         * 其他文档-复制
         */
        FILE_COPY("file.copy"),
        /**
         * 其他文档-下载
         */
        FILE_DOWNLOAD("file.download"),
        /**
         * 其他文档-删除
         */
        FILE_DELETE("file.delete"),
        /**
         * 其他文档-回收站-恢复
         */
        FILE_RECOVER("file.recover"),
        /**
         * 其他文档-回收站-永久删除
         */
        FILE_PERMANENTLY_DELETE("file.permanently-delete"),
        /**
         * 其他文档-分享
         */
        FILE_SHARE("file.share"),
        /**
         * 其他文档-管理协作者
         */
        FILE_COLLABORATORS("file.collaborators"),
        /**
         * 其他文档-安全设置
         */
        FILE_SECURITY_SETTINGS("file.security-settings");

        /**
         * 所有操作权限
         */
        public static final ActionPermission[] ALL_ACTION_PERMISSION = ActionPermission.values();
        /**
         * 操作权限Code查找Map
         */
        private static final Map<String, ActionPermission> CODE_TO_ACTION_PERMISSION = Stream.of(ALL_ACTION_PERMISSION)
                .collect(Collectors.toMap(ActionPermission::getCode, Function.identity()));

        ActionPermission(String code) {
            this.code = code;
        }

        /**
         * 操作权限编码
         */
        private final String code;
        /**
         * 查询多语言描述时的防冲突前缀
         */
        private static final String CODE_PREFIX = "knowledge-base.permission.";

        /**
         * 根据操作权限编码查询枚举值
         *
         * @param actionPermissionCode 据操作权限编码
         * @return 操作权限枚举值, 未找到返回空
         */
        public static ActionPermission ofCode(String actionPermissionCode) {
            if (StringUtils.isBlank(actionPermissionCode)) {
                return null;
            }
            return CODE_TO_ACTION_PERMISSION.get(actionPermissionCode);
        }

        /**
         * 是否为合法的操作权限编码
         *
         * @param actionPermissionCode 操作权限编码
         * @return 是否合法
         */
        public static boolean isValid(String actionPermissionCode) {
            final ActionPermission actionPermission = ofCode(actionPermissionCode);
            return actionPermission != null && ArrayUtils.contains(ALL_ACTION_PERMISSION, actionPermission);
        }

        /**
         * @return 多语言描述
         */
        public String getDescription() {
            return MessageAccessor.getMessage(CODE_PREFIX + this.code).getDesc();
        }

        /**
         * @return 操作权限编码
         */
        public String getCode() {
            return this.code;
        }

    }

    /**
     * 权限角色编码
     *
     * @author gaokuo.dai@zknow.com 2022-09-23
     */
    public static class PermissionRole {
        /**
         * 可管理
         */
        public static final String MANAGER = "MANAGER";
        /**
         * 可编辑
         */
        public static final String EDITOR = "EDITOR";
        /**
         * 可阅读
         */
        public static final String READER = "READER";
        /**
         * 空值占位符
         */
        public static final String NULL = "NULL";

        private PermissionRole() {
            throw new UnsupportedOperationException();
        }

        /**
         * 所有权限角色编码
         */
        public static final Set<String> ALL_CODES = SetUtils.hashSet(MANAGER, EDITOR, READER, NULL);
        /**
         * 知识库权限管理可接受的的权限角色编码
         */
        public static final Set<String> OBJECT_SETTING_ROLE_CODES = SetUtils.hashSet(MANAGER, EDITOR, READER);

        /**
         * 是否为合法的权限角色编码
         *
         * @param permissionRoleCode 权限角色编码
         * @return 是否合法
         */
        public static boolean isValid(String permissionRoleCode) {
            return permissionRoleCode != null && ALL_CODES.contains(permissionRoleCode);
        }

        /**
         * 是否为知识库权限管理可接受的的权限角色编码
         *
         * @param permissionRoleCode 权限角色编码
         * @return 是否合法
         */
        public static boolean isValidForPermissionRoleConfig(String permissionRoleCode) {
            return permissionRoleCode != null && OBJECT_SETTING_ROLE_CODES.contains(permissionRoleCode);
        }
    }

    /**
     * 授权对象类型
     *
     * @author zongqi.hao@zknow.com
     * @since 2022/9/23
     */
    public enum PermissionRangeType {

        MANAGER,
        MEMBER,
        USER,
        ROLE,
        WORK_GROUP,
        PUBLIC,

        /**
         * 非数据库值, 供前端显示
         */
        SPECIFY_RANGE;

        /**
         * 知识库对象授权时的授权对象类型
         */
        public static final Set<String> OBJECT_SETTING_RANGE_TYPES = SetUtils.hashSet(
                USER.toString(),
                ROLE.toString(),
                WORK_GROUP.toString(),
                PUBLIC.toString()
        );

        /**
         * 知识库创建和默认类型
         */
        public static final Set<String> KNOWLEDGE_BASE_SETTING_RANGE_TYPES = SetUtils.union(
                OBJECT_SETTING_RANGE_TYPES,
                SetUtils.hashSet(
                        MANAGER.toString(),
                        MEMBER.toString()
                )
        );
        
        /**
         * 组织设置界面前端渲染所使用的授权对象类型
         */
        public static final Set<String> RADIO_RANGES_TYPES_FOR_FRONT = SetUtils.hashSet(
                MANAGER.toString(),
                MEMBER.toString()
        );

        public static PermissionRangeType of(String value) {
            return PermissionRangeType.valueOf(value);
        }

    }

    /**
     * 权限范围基础对象类型
     */
    public enum PermissionTargetBaseType {
        /**
         * 知识库
         */
        KNOWLEDGE_BASE("knowledge-base"),
        /**
         * 文件夹
         */
        FOLDER("folder"),
        /**
         * 文件，对应 {@link WorkSpaceType} DOCUMENT和FILE
         */
        FILE("file");

        /**
         * 所有基础对象类型
         */
        public static final PermissionTargetBaseType[] ALL_PERMISSION_TARGET_BASE_TYPE = PermissionTargetBaseType.values();
        /**
         * 基础对象类型code查找Map
         */
        private static final Map<String, PermissionTargetBaseType> CODE_TO_PERMISSION_TARGET_BASE_TYPE = Stream.of(ALL_PERMISSION_TARGET_BASE_TYPE)
                .collect(Collectors.toMap(PermissionTargetBaseType::toString, Function.identity()));


        PermissionTargetBaseType(String kebabCaseName) {
            this.kebabCaseName = kebabCaseName;
        }

        /**
         * 中划线分隔消息命名方式
         */
        private final String kebabCaseName;

        /**
         * @return 中划线分隔消息命名方式
         */
        public String getKebabCaseName() {
            return kebabCaseName;
        }

        public static PermissionTargetBaseType of(String permissionTargetBaseTypeCode) {
            if (StringUtils.isBlank(permissionTargetBaseTypeCode)) {
                return null;
            }
            return CODE_TO_PERMISSION_TARGET_BASE_TYPE.get(permissionTargetBaseTypeCode);
        }

        /**
         * 是否为合法的础对象类型
         *
         * @param permissionTargetBaseTypeCode 操作权限编码
         * @return 是否合法
         */
        public static boolean isValid(String permissionTargetBaseTypeCode) {
            final PermissionTargetBaseType permissionTargetBaseType = of(permissionTargetBaseTypeCode);
            return permissionTargetBaseType != null && ArrayUtils.contains(ALL_PERMISSION_TARGET_BASE_TYPE, permissionTargetBaseType);
        }

    }

    /**
     * Copyright (c) 2022. ZKnow Enterprise Solution. All right reserved.
     *
     * @author zongqi.hao@zknow.com
     * @since 2022/9/23
     */
    public enum PermissionTargetType {

        /**
         * 组织层创建
         */
        KNOWLEDGE_BASE_CREATE_ORG(PermissionTargetBaseType.KNOWLEDGE_BASE, "_CREATE_ORG"),
        /**
         * 项目层创建
         */
        KNOWLEDGE_BASE_CREATE_PROJECT(PermissionTargetBaseType.KNOWLEDGE_BASE, "_CREATE_PROJECT"),
        /**
         * 组织层默认
         */
        KNOWLEDGE_BASE_DEFAULT_ORG(PermissionTargetBaseType.KNOWLEDGE_BASE, "_DEFAULT_ORG"),
        /**
         * 项目层默认
         */
        KNOWLEDGE_BASE_DEFAULT_PROJECT(PermissionTargetBaseType.KNOWLEDGE_BASE, "_DEFAULT_PROJECT"),
        /**
         * 组织层知识库
         */
        KNOWLEDGE_BASE_ORG(PermissionTargetBaseType.KNOWLEDGE_BASE, "_ORG"),
        /**
         * 项目层知识库
         */
        KNOWLEDGE_BASE_PROJECT(PermissionTargetBaseType.KNOWLEDGE_BASE, "_PROJECT"),
        /**
         * 组织层文件夹
         */
        FOLDER_ORG(PermissionTargetBaseType.FOLDER, "_ORG"),
        /**
         * 项目层文件夹
         */
        FOLDER_PROJECT(PermissionTargetBaseType.FOLDER, "_PROJECT"),
        /**
         * 组织层文件，包含document和file
         */
        FILE_ORG(PermissionTargetBaseType.FILE, "_ORG"),
        /**
         * 项目层文件，包含document和file
         */
        FILE_PROJECT(PermissionTargetBaseType.FILE, "_PROJECT"),
        ;

        /**
         * 权限目标基础类型
         */
        private final PermissionTargetBaseType baseType;
        /**
         * 存到数据库的Code
         */
        private final String code;

        PermissionTargetType(PermissionTargetBaseType baseType, String suffix) {
            Assert.notNull(baseType, BaseConstants.ErrorCode.NOT_NULL);
            Assert.isTrue(StringUtils.isNotBlank(suffix), BaseConstants.ErrorCode.NOT_NULL);
            this.baseType = baseType;
            this.code = baseType + suffix;
        }

        /**
         * @return 存到数据库的Code
         */
        public String getCode() {
            return this.code;
        }

        /**
         * @return 权限目标基础类型
         */
        public PermissionTargetBaseType getBaseType() {
            return this.baseType;
        }


        /**
         * 知识库创建和默认类型
         */
        public static final Set<String> KNOWLEDGE_BASE_SETTING_TARGET_TYPES = SetUtils.hashSet(
                KNOWLEDGE_BASE_CREATE_ORG.code,
                KNOWLEDGE_BASE_CREATE_PROJECT.code,
                KNOWLEDGE_BASE_DEFAULT_ORG.code,
                KNOWLEDGE_BASE_DEFAULT_PROJECT.code
        );
        /**
         * 知识库和知识库文档类型
         */
        public static final Set<String> OBJECT_SETTING_TARGET_TYPES = SetUtils.hashSet(
                KNOWLEDGE_BASE_ORG.code,
                KNOWLEDGE_BASE_PROJECT.code,
                FOLDER_ORG.code,
                FOLDER_PROJECT.code,
                FILE_ORG.code,
                FILE_PROJECT.code
        );

        public static PermissionTargetType of(String value) {
            return PermissionTargetType.valueOf(value);
        }

    }

    /**
     * 知识库安全设置选项
     *
     * @author superlee
     * @since 2022-09-26
     */
    public enum SecurityConfigAction {

        /**
         * 可复制
         */
        COPY,
        /**
         * 可分享
         */
        SHARE,
        /**
         * 可下载
         */
        DOWNLOAD;

        public static Set<String> buildPermissionCodeByType(PermissionTargetBaseType permissionTarget) {
            Set<String> permissionCodes = new HashSet<>();
            for (SecurityConfigAction securityConfigAction : SecurityConfigAction.values()) {
                StringBuilder builder = new StringBuilder();
                builder.append(permissionTarget.getKebabCaseName()).append(BaseConstants.Symbol.POINT).append(securityConfigAction.toString().toLowerCase());
                permissionCodes.add(builder.toString());
            }
            return permissionCodes;
        }
    }
}

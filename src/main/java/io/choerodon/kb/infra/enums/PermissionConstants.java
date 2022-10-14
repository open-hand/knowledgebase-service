package io.choerodon.kb.infra.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Sets;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.infra.common.PermissionErrorCode;

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
     * 权限通用缓存前缀
     */
    public static final String PERMISSION_CACHE_PREFIX = "knowledge:permission:";

    /**
     * 权限通用缓存超时时间(单位: 秒)
     */
    public static final long PERMISSION_CACHE_EXPIRE = 3600L;

    /**
     * 权限通用无效缓存占位符
     */
    public static final String PERMISSION_CACHE_INVALID_PLACEHOLDER = "INVALID";

    /**
     * 操作权限--知识库创建</br>
     * 知识库创建权限不在矩阵中配置, 但是又需要鉴权, 所以这里做一个单独的常量
     */
    public static final String ACTION_PERMISSION_CREATE_KNOWLEDGE_BASE = "knowledge-base.create";

    /**
     * 操作权限
     *
     * @author gaokuo.dai@zknow.com 2022-09-23
     */
    public enum ActionPermission {

        // 知识库操作权限
        /**
         * 知识库-查看
         */
        KNOWLEDGE_BASE_READ(ActionPermissionRange.ACTION_RANGE_KNOWLEDGE_BASE, "read"),
        /**
         * 知识库-设置
         */
        KNOWLEDGE_BASE_SETTINGS(ActionPermissionRange.ACTION_RANGE_KNOWLEDGE_BASE, "settings"),
        /**
         * 知识库-删除
         */
        KNOWLEDGE_BASE_DELETE(ActionPermissionRange.ACTION_RANGE_KNOWLEDGE_BASE, "delete"),
        /**
         * 知识库-回收站-恢复
         */
        KNOWLEDGE_BASE_RECOVER(ActionPermissionRange.ACTION_RANGE_KNOWLEDGE_BASE, "recover"),
        /**
         * 知识库-回收站-永久删除
         */
        KNOWLEDGE_BASE_PERMANENTLY_DELETE(ActionPermissionRange.ACTION_RANGE_KNOWLEDGE_BASE, "permanently-delete"),
        /**
         * 知识库-管理协作者
         */
        KNOWLEDGE_BASE_COLLABORATORS(ActionPermissionRange.ACTION_RANGE_KNOWLEDGE_BASE, "collaborators"),
        /**
         * 知识库-安全设置
         */
        KNOWLEDGE_BASE_SECURITY_SETTINGS(ActionPermissionRange.ACTION_RANGE_KNOWLEDGE_BASE, "security-settings"),

        // 文件夹操作权限
        /**
         * 文件夹-查看
         */
        FOLDER_READ(ActionPermissionRange.ACTION_RANGE_FOLDER, "read"),
        /**
         * 文件夹-创建
         */
        FOLDER_CREATE(ActionPermissionRange.ACTION_RANGE_FOLDER, "create"),
        /**
         * 文件夹-重命名
         */
        FOLDER_RENAME(ActionPermissionRange.ACTION_RANGE_FOLDER, "rename"),
        /**
         * 文件夹-移动
         */
        FOLDER_MOVE(ActionPermissionRange.ACTION_RANGE_FOLDER, "move"),
        /**
         * 文件夹-删除
         */
        FOLDER_DELETE(ActionPermissionRange.ACTION_RANGE_FOLDER, "delete"),
        /**
         * 文件夹-回收站-恢复
         */
        FOLDER_RECOVER(ActionPermissionRange.ACTION_RANGE_FOLDER, "recover"),
        /**
         * 文件夹-回收站-永久删除
         */
        FOLDER_PERMANENTLY_DELETE(ActionPermissionRange.ACTION_RANGE_FOLDER, "permanently-delete"),
        /**
         * 文件夹-管理协作者
         */
        FOLDER_COLLABORATORS(ActionPermissionRange.ACTION_RANGE_FOLDER, "collaborators"),
        /**
         * 文件夹-安全设置
         */
        FOLDER_SECURITY_SETTINGS(ActionPermissionRange.ACTION_RANGE_FOLDER, "security-settings"),

        // MD文档操作权限
        /**
         * MD文档-查看
         */
        DOCUMENT_READ(ActionPermissionRange.ACTION_RANGE_DOCUMENT, "read"),
        /**
         * MD文档-创建
         */
        DOCUMENT_CREATE(ActionPermissionRange.ACTION_RANGE_DOCUMENT, "create"),
        /**
         * MD文档-编辑
         */
        DOCUMENT_EDIT(ActionPermissionRange.ACTION_RANGE_DOCUMENT, "edit"),
        /**
         * MD文档-重命名
         */
        DOCUMENT_RENAME(ActionPermissionRange.ACTION_RANGE_DOCUMENT, "rename"),
        /**
         * MD文档-移动至
         */
        DOCUMENT_MOVE(ActionPermissionRange.ACTION_RANGE_DOCUMENT, "move"),
        /**
         * MD文档-复制
         */
        DOCUMENT_COPY(ActionPermissionRange.ACTION_RANGE_DOCUMENT, "copy"),
        /**
         * MD文档-下载为PDF
         */
        DOCUMENT_DOWNLOAD_TO_PDF(ActionPermissionRange.ACTION_RANGE_DOCUMENT, "download-to-pdf"),
        /**
         * MD文档-操作历史
         */
        DOCUMENT_OPERATING_HISTORY(ActionPermissionRange.ACTION_RANGE_DOCUMENT, "operating-history"),
        /**
         * MD文档-版本对比-查看
         */
        DOCUMENT_VIEW_VERSION(ActionPermissionRange.ACTION_RANGE_DOCUMENT, "view-version"),
        /**
         * MD文档-版本对比-回滚
         */
        DOCUMENT_ROLL_BACK(ActionPermissionRange.ACTION_RANGE_DOCUMENT, "roll-back"),
        /**
         * MD文档-删除
         */
        DOCUMENT_DELETE(ActionPermissionRange.ACTION_RANGE_DOCUMENT, "delete"),
        /**
         * MD文档-删除回收站-恢复
         */
        DOCUMENT_RECOVER(ActionPermissionRange.ACTION_RANGE_DOCUMENT, "recover"),
        /**
         * MD文档-回收站-永久删除
         */
        DOCUMENT_PERMANENTLY_DELETE(ActionPermissionRange.ACTION_RANGE_DOCUMENT, "permanently-delete"),
        /**
         * MD文档-分享
         */
        DOCUMENT_SHARE(ActionPermissionRange.ACTION_RANGE_DOCUMENT, "share"),
        /**
         * MD文档-管理协作者
         */
        DOCUMENT_COLLABORATORS(ActionPermissionRange.ACTION_RANGE_DOCUMENT, "collaborators"),
        /**
         * MD文档-安全设置
         */
        DOCUMENT_SECURITY_SETTINGS(ActionPermissionRange.ACTION_RANGE_DOCUMENT, "security-settings"),

        // 其他文档操作权限
        /**
         * 其他文档-查看
         */
        FILE_READ(ActionPermissionRange.ACTION_RANGE_FILE, "read"),
        /**
         * 其他文档-创建
         */
        FILE_CREATE(ActionPermissionRange.ACTION_RANGE_FILE, "create"),
        /**
         * 其他文档-编辑
         */
        FILE_EDIT(ActionPermissionRange.ACTION_RANGE_FILE, "edit"),
        /**
         * 其他文档-重命名
         */
        FILE_RENAME(ActionPermissionRange.ACTION_RANGE_FILE, "rename"),
        /**
         * 其他文档-移动至
         */
        FILE_MOVE(ActionPermissionRange.ACTION_RANGE_FILE, "move"),
        /**
         * 其他文档-复制
         */
        FILE_COPY(ActionPermissionRange.ACTION_RANGE_FILE, "copy"),
        /**
         * 其他文档-下载
         */
        FILE_DOWNLOAD(ActionPermissionRange.ACTION_RANGE_FILE, "download"),
        /**
         * 其他文档-删除
         */
        FILE_DELETE(ActionPermissionRange.ACTION_RANGE_FILE, "delete"),
        /**
         * 其他文档-回收站-恢复
         */
        FILE_RECOVER(ActionPermissionRange.ACTION_RANGE_FILE, "recover"),
        /**
         * 其他文档-回收站-永久删除
         */
        FILE_PERMANENTLY_DELETE(ActionPermissionRange.ACTION_RANGE_FILE, "permanently-delete"),
        /**
         * 其他文档-分享
         */
        FILE_SHARE(ActionPermissionRange.ACTION_RANGE_FILE, "share"),
        /**
         * 其他文档-管理协作者
         */
        FILE_COLLABORATORS(ActionPermissionRange.ACTION_RANGE_FILE, "collaborators"),
        /**
         * 其他文档-安全设置
         */
        FILE_SECURITY_SETTINGS(ActionPermissionRange.ACTION_RANGE_FILE, "security-settings")

        ;

        /**
         * 操作权限范围
         * @author gaokuo.dai@zknow.com 2022-10-14
         */
        public static class ActionPermissionRange {
            /**
             * 操作权限范围-知识库
             */
            public static final String ACTION_RANGE_KNOWLEDGE_BASE = PermissionTargetBaseType.KNOWLEDGE_BASE.getKebabCaseName();
            /**
             * 操作权限范围-文件夹
             */
            public static final String ACTION_RANGE_FOLDER = PermissionTargetBaseType.FOLDER.getKebabCaseName();
            /**
             * 操作权限范围-MD文档
             */
            public static final String ACTION_RANGE_DOCUMENT = "document";
            /**
             * 操作权限范围-其他文档
             */
            public static final String ACTION_RANGE_FILE = PermissionTargetBaseType.FILE.getKebabCaseName();
        }

        /**
         * 所有操作权限
         */
        public static final ActionPermission[] ALL_ACTION_PERMISSION = ActionPermission.values();

        /**
         * 知识库操作权限
         */
        public static final ActionPermission[] KNOWLEDGE_BASE_ACTION_PERMISSION = Arrays.stream(ActionPermission.values())
                .filter(ap -> ap.getActionRange().equals(ActionPermissionRange.ACTION_RANGE_KNOWLEDGE_BASE))
                .collect(Collectors.toList())
                .toArray(new ActionPermission[0]);
        /**
         * 文件夹操作权限
         */
        public static final ActionPermission[] FOLDER_ACTION_PERMISSION = Arrays.stream(ActionPermission.values())
                .filter(ap -> ap.getActionRange().equals(ActionPermissionRange.ACTION_RANGE_FOLDER))
                .collect(Collectors.toList())
                .toArray(new ActionPermission[0]);
        /**
         * MD文档操作权限
         */
        public static final ActionPermission[] DOCUMENT_ACTION_PERMISSION = Arrays.stream(ActionPermission.values())
                .filter(ap -> ap.getActionRange().equals(ActionPermissionRange.ACTION_RANGE_DOCUMENT))
                .collect(Collectors.toList())
                .toArray(new ActionPermission[0]);
        /**
         * 其他文档操作权限
         */
        public static final ActionPermission[] FILE_ACTION_PERMISSION = Arrays.stream(ActionPermission.values())
                .filter(ap -> ap.getActionRange().equals(ActionPermissionRange.ACTION_RANGE_FILE))
                .collect(Collectors.toList())
                .toArray(new ActionPermission[0]);

        /**
         * 操作权限Code查找Map
         */
        private static final Map<String, ActionPermission> CODE_TO_ACTION_PERMISSION = Stream.of(ALL_ACTION_PERMISSION)
                .collect(Collectors.toMap(ActionPermission::getCode, Function.identity()));

        ActionPermission(String actionRange, String baseActionCode) {
            this.actionRange = actionRange;
            this.baseActionCode = baseActionCode;
            this.code = actionRange + BaseConstants.Symbol.POINT + baseActionCode;
        }

        /**
         * 操作权限编码
         */
        private final String actionRange;
        /**
         * 操作权限范围
         */
        private final String baseActionCode;
        /**
         * 权限基础操作
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

        /**
         * @return 操作权限范围
         */
        public String getActionRange() {
            return actionRange;
        }

        /**
         * @return 权限基础操作
         */
        public String getBaseActionCode() {
            return baseActionCode;
        }
    }

    /**
     * 权限角色编码
     *
     * @author gaokuo.dai@zknow.com 2022-09-23
     */
    public static class PermissionRole {

        /**
         * 比较两个角色编码的权重
         * @param roleCode1 角色编码1
         * @param roleCode2 角色编码1
         * @return getOrder(roleCode1) - getOrder(roleCode2)
         */
        public static int compare(String roleCode1, String roleCode2) {
            Assert.isTrue(roleCode1 == null || ALL_CODES.contains(roleCode1), BaseConstants.ErrorCode.DATA_INVALID);
            Assert.isTrue(roleCode2 == null || ALL_CODES.contains(roleCode2), BaseConstants.ErrorCode.DATA_INVALID);
            return getOrder(roleCode1) - getOrder(roleCode2);
        }
        
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
         * 获取角色权重<br/>
         *     <table border="1">
         *         <tr>
         *             <th>角色编码</th>
         *             <th>权重</th>
         *         </tr>
         *         <tr>
         *             <td>MANAGER</td>
         *             <td>3</td>
         *         </tr>
         *         <tr>
         *             <td>EDITOR</td>
         *             <td>2</td>
         *         </tr>
         *         <tr>
         *             <td>READER</td>
         *             <td>1</td>
         *         </tr>
         *         <tr>
         *             <td>NULL/空指针</td>
         *             <td>0</td>
         *         </tr>
         *         <tr>
         *             <td>其他</td>
         *             <td>CommonException(BaseConstants.ErrorCode.DATA_INVALID)</td>
         *         </tr>
         *     </table>
         * @param roleCode  角色编码
         * @return          权重
         */
        private static int getOrder(String roleCode) {
            if(roleCode == null || NULL.equals(roleCode)) {
                return 0;
            } else if(READER.equals(roleCode)) {
                return 1;
            } else if(EDITOR.equals(roleCode)) {
                return 2;
            } else if(MANAGER.equals(roleCode)) {
                return 3;
            } else {
                throw new CommonException(BaseConstants.ErrorCode.DATA_INVALID);
            }
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
        /**
         * 管理者
         */
        MANAGER,
        /**
         * 成员
         */
        MEMBER,
        /**
         * 用户
         */
        USER,
        /**
         * 角色
         */
        ROLE,
        /**
         * 工作组
         */
        WORK_GROUP,
        /**
         * 公开
         */
        PUBLIC,

        /**
         * <b style="color:red">
         * 非数据库值, 仅供前端显示
         * </b>
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
         * 组织设置界面前端渲染所使用的授权对象类型
         */
        public static final Set<String> RADIO_RANGES_TYPES_FOR_FRONT = SetUtils.hashSet(
                MANAGER.toString(),
                MEMBER.toString()
        );

        /**
         * 知识库创建和默认类型
         */
        public static final Set<String> KNOWLEDGE_BASE_SETTING_RANGE_TYPES = SetUtils.union(
                OBJECT_SETTING_RANGE_TYPES,
                RADIO_RANGES_TYPES_FOR_FRONT
        );

        /**
         * 获取字符串对应的枚举
         *
         * @param value 字符串
         * @return 对应的枚举
         */
        public static PermissionRangeType of(String value) {
            return PermissionRangeType.valueOf(value);
        }

    }

    /**
     * 控制对象基础类型
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

        /**
         * 根据字符串获取对应的枚举
         *
         * @param permissionTargetBaseTypeCode 字符串
         * @return 对应的枚举
         */
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
     * 控制对象类型
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
         * 基础权限--知识库创建
         */
        private static final String KNOWLEDGE_BASE_CREATE = PermissionTargetBaseType.KNOWLEDGE_BASE + "_CREATE";
        /**
         * 基础权限--知识库默认值
         */
        private static final String KNOWLEDGE_BASE_DEFAULT = PermissionTargetBaseType.KNOWLEDGE_BASE + "_DEFAULT";

        /**
         * 权限目标基础类型
         */
        private final PermissionTargetBaseType baseType;
        /**
         * 存到数据库的Code
         */
        private final String code;

        /**
         * 构造函数
         *
         * @param baseType 控制对象基础类型
         * @param suffix   明细尾缀
         */
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
         * 根据项目id和基础指向类型获取真正的指向类型
         *
         * @param projectId      项目id
         * @param baseTargetType 基础指向类型
         */
        public static PermissionConstants.PermissionTargetType getPermissionTargetType(Long projectId, String baseTargetType) {
            if(StringUtils.isBlank(baseTargetType)) {
                return null;
            }
            PageResourceType resourceType = getPageResourceType(projectId);
            PermissionConstants.PermissionTargetType permissionTargetType = null;

            if(KNOWLEDGE_BASE_CREATE.equals(baseTargetType)) {
                // 处理知识库创建权限
                if(resourceType == PageResourceType.PROJECT) {
                    permissionTargetType =  PermissionTargetType.KNOWLEDGE_BASE_CREATE_PROJECT;
                } else {
                    permissionTargetType = PermissionTargetType.KNOWLEDGE_BASE_CREATE_ORG;
                }
            } else if(KNOWLEDGE_BASE_DEFAULT.equals(baseTargetType)) {
                // 处理知识库默认权限
                if(resourceType == PageResourceType.PROJECT) {
                    permissionTargetType =  PermissionTargetType.KNOWLEDGE_BASE_DEFAULT_PROJECT;
                } else {
                    permissionTargetType = PermissionTargetType.KNOWLEDGE_BASE_DEFAULT_ORG;
                }
            } else {
                // 处理其他权限
                permissionTargetType = PermissionConstants.PermissionTargetType.BASE_TYPE_TARGET_TYPE_MAPPING
                        .get(PermissionConstants.PermissionTargetBaseType.of(baseTargetType), resourceType);
            }
            Assert.notNull(permissionTargetType, PermissionErrorCode.ERROR_TARGET_TYPES);
            return permissionTargetType;
        }

        /**
         * 根据项目id获得资源类型
         *
         * @param projectId 项目id
         */
        public static PageResourceType getPageResourceType(Long projectId) {
            return projectId == null || projectId == 0 ? PageResourceType.ORGANIZATION : PageResourceType.PROJECT;
        }

        /**
         * 根据项目id获取workspace的类型集合
         *
         * @param projectId 项目id
         */
        public static Set<PermissionTargetType> getKBObjectTargetTypes(Long projectId) {
            PageResourceType pageResourceType = getPageResourceType(projectId);
            return pageResourceType == PageResourceType.ORGANIZATION
                    ? Sets.newHashSet(FOLDER_ORG, FILE_ORG)
                    : Sets.newHashSet(FOLDER_PROJECT, FILE_PROJECT);
        }

        /**
         * 根据项目id获取knowledge base的类型
         *
         * @param projectId 项目id
         */
        public static PermissionTargetType getKBTargetType(Long projectId) {
            PageResourceType pageResourceType = getPageResourceType(projectId);
            return pageResourceType == PageResourceType.ORGANIZATION ? KNOWLEDGE_BASE_ORG : KNOWLEDGE_BASE_PROJECT;
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
         * 知识库创建类型
         */
        public static final Set<String> KNOWLEDGE_BASE_SETTING_CREATE_TARGET_TYPES = SetUtils.hashSet(
                KNOWLEDGE_BASE_CREATE_ORG.code,
                KNOWLEDGE_BASE_CREATE_PROJECT.code
        );
        /**
         * 知识库类型 包含组织层和项目层
         */
        public static final Set<String> KB_TARGET_TYPES = SetUtils.hashSet(KNOWLEDGE_BASE_ORG.code, KNOWLEDGE_BASE_PROJECT.code);
        /**
         * 文件夹类型
         */
        public static final Set<String> FOLDER_TARGET_TYPES = SetUtils.hashSet(FOLDER_ORG.code, FOLDER_PROJECT.code);
        /**
         * 文档类型
         */
        public static final Set<String> FILE_TARGET_TYPES = SetUtils.hashSet(FILE_ORG.code, FILE_PROJECT.code);
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

        /**
         * 基础类型 & 层级映射 多键映射
         */
        public static final MultiKeyMap<Enum<?>, PermissionTargetType> BASE_TYPE_TARGET_TYPE_MAPPING;

        static {
            BASE_TYPE_TARGET_TYPE_MAPPING = new MultiKeyMap<>();
            BASE_TYPE_TARGET_TYPE_MAPPING.put(PermissionTargetBaseType.KNOWLEDGE_BASE, PageResourceType.ORGANIZATION, KNOWLEDGE_BASE_ORG);
            BASE_TYPE_TARGET_TYPE_MAPPING.put(PermissionTargetBaseType.FOLDER, PageResourceType.ORGANIZATION, FOLDER_ORG);
            BASE_TYPE_TARGET_TYPE_MAPPING.put(PermissionTargetBaseType.FILE, PageResourceType.ORGANIZATION, FILE_ORG);
            BASE_TYPE_TARGET_TYPE_MAPPING.put(PermissionTargetBaseType.KNOWLEDGE_BASE, PageResourceType.PROJECT, KNOWLEDGE_BASE_PROJECT);
            BASE_TYPE_TARGET_TYPE_MAPPING.put(PermissionTargetBaseType.FOLDER, PageResourceType.PROJECT, FOLDER_PROJECT);
            BASE_TYPE_TARGET_TYPE_MAPPING.put(PermissionTargetBaseType.FILE, PageResourceType.PROJECT, FILE_PROJECT);
        }

        /**
         * 根据字符串获取对应的枚举
         *
         * @param value 字符串
         * @return 对应的枚举
         */
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

        /**
         * 根据控制对象基础类型获得安全配置权限编码
         *
         * @param permissionBaseTarget 根据控制对象基础类型
         * @return 安全配置权限编码
         */
        public static Set<String> buildPermissionCodeByType(PermissionTargetBaseType permissionBaseTarget) {
            return Stream.of(SecurityConfigAction.values())
                    // 生成规则: 小写中划线分隔的permissionBaseTarget + '.' + 安全设置选项枚举小写
                    // 如: knowledge-base.download
                    .map(sca -> permissionBaseTarget.getKebabCaseName() + BaseConstants.Symbol.POINT + sca.toString().toLowerCase())
                    .collect(Collectors.toSet());
        }
    }

    /**
     * @author superlee
     * @since 2022-10-11
     */
    public enum PermissionRefreshType {

        /**
         * 权限控制矩阵
         */
        ROLE_CONFIG("role-config"),
        /**
         * 权限范围
         */
        RANGE("range"),
        /**
         * 安全设置
         */
        SECURITY_CONFIG("security-config"),
        /**
         * 知识库对象父子关系
         */
        TARGET_PARENT("target-parent");

        final String kebabCaseName;

        PermissionRefreshType(String kebabCaseName) {
            this.kebabCaseName = kebabCaseName;
        }

        public static PermissionRefreshType ofKebabCaseName(String kebabCaseName) {
            for (PermissionRefreshType permissionRefreshType : PermissionRefreshType.values()) {
                if (permissionRefreshType.kebabCaseName.equals(kebabCaseName)) {
                    return permissionRefreshType;
                }
            }
            return null;
        }

        public String getKebabCaseName() {
            return kebabCaseName;
        }}
}

package io.choerodon.kb.infra.enums;

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

    String kebabCaseName;

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

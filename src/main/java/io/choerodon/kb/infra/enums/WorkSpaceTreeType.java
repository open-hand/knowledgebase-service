package io.choerodon.kb.infra.enums;

/**
 * 知识库对象树类型
 * @author gaokuo.dai@zknow.com 2022-10-17
 */
public enum WorkSpaceTreeType {

    /**
     * 项目
     */
    PROJECT("project"),
    /**
     * 组织
     */
    ORGANIZATION("organization"),
    /**
     * 共享
     */
    SHARE("share");

    WorkSpaceTreeType(String code) {
        this.code = code;
    }
    private final String code;

    public String getCode() {
        return code;
    }
}

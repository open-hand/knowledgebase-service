package io.choerodon.kb.infra.common.enums;

/**
 * Created by Zenger on 2019/4/29.
 */
public enum PageResourceType {

    /**
     * 标记
     */
    ORGANIZATION("organization"),
    PROJECT("project");

    private String type;

    PageResourceType(String type) {
        this.type = type;
    }

    public String getResourceType() {
        return type;
    }

    public static PageResourceType forString(String value) {
        switch (value) {
            case "organization":
                return PageResourceType.ORGANIZATION;
            case "project":
                return PageResourceType.PROJECT;
            default:
                return null;
        }
    }

}

package io.choerodon.kb.infra.enums;

/**
 * @author zhaotianxin
 * @since 2020/1/8
 */
public enum OpenRangeType {
    RANGE_PRIVATE("range_private"),
    RANGE_PUBLIC("range_public"),
    RANGE_PROJECT("range_project");
    private String type;

    OpenRangeType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}

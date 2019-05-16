package io.choerodon.kb.infra.common;

/**
 * Created by Zenger on 2018/7/18.
 */
public abstract class BaseStage {

    private BaseStage() {

    }

    public static final String REFERENCE_PAGE = "referencePage";
    public static final String REFERENCE_URL = "referenceUrl";
    public static final String SELF = "self";
    public static final String BACKETNAME = "knowledgebase-service";
    public static final String INSERT = "insert";
    public static final String UPDATE = "update";
}

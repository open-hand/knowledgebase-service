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

    //data log
    public static final String PAGE_CREATE = "pageCreate";
    public static final String PAGE_UPDATE = "pageUpdate";
    public static final String COMMENT_CREATE = "commentCreate";
    public static final String COMMENT_UPDATE = "commentUpdate";
    public static final String COMMENT_DELETE = "commentDelete";
    public static final String ATTACHMENT_CREATE = "attachmentCreate";
    public static final String ATTACHMENT_DELETE = "attachmentDelete";
    public static final String CREATE_OPERATION = "Create";
    public static final String UPDATE_OPERATION = "Update";
    public static final String DELETE_OPERATION = "Delete";
    public static final String PAGE = "Page";
    public static final String COMMENT = "Comment";
    public static final String ATTACHMENT = "Attachment";
}

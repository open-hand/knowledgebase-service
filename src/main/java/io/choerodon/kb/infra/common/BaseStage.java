package io.choerodon.kb.infra.common;

/**
 * Created by Zenger on 2018/7/18.
 */
public abstract class BaseStage {

    private BaseStage() {

    }

    public static final String BACKETNAME = "knowledgebase-service";
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
    //es page index
    public static final String ES_PAGE_INDEX = "knowledge_page";
    public static final String ES_PAGE_FIELD_PAGE_ID = "id";
    public static final String ES_PAGE_FIELD_PROJECT_ID = "project_id";
    public static final String ES_PAGE_FIELD_ORGANIZATION_ID = "organization_id";
    public static final String ES_PAGE_FIELD_TITLE = "title";
    public static final String ES_PAGE_FIELD_CONTENT = "content";
    public static final String ES_PAGE_FIELD_BASE_ID = "base_id";
}

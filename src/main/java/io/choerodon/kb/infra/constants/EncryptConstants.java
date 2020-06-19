package io.choerodon.kb.infra.constants;

/**
 * @author jiaxu.cui@hand-china.com 2020/6/12 下午2:11
 */
public final class EncryptConstants {
    private static final String NULL_KEY = "";
    public static final String TN_KB_BOOK = NULL_KEY; /*"kb_book";*/
    public static final String TN_KB_WORKSPACE = NULL_KEY; /*"kb_workspace";*/
    public static final String TN_KB_PAGE_ATTACHMENT = NULL_KEY; /*"kb_page_attachment";*/
    public static final String TN_KB_PAGE_LOG = NULL_KEY; /*"kb_page_log";*/
    public static final String TN_KB_WORKSPACE_SHARE = NULL_KEY; /*"kb_workspace_share";*/
    public static final String TN_KB_PAGE_TAG = NULL_KEY; /*"kb_page_tag";*/
    public static final String TN_KB_PAGE_VERSION = NULL_KEY; /*"kb_page_version";*/
    public static final String TN_KB_KNOWLEDGE_BASE = NULL_KEY; /*"kb_knowledge_base";*/
    public static final String TN_KB_PAGE = NULL_KEY; /*"kb_page";*/
    public static final String TN_KB_USER_SETTING = NULL_KEY; /*"kb_user_setting";*/
    public static final String TN_KB_WORKSPACE_PAGE = NULL_KEY; /*"kb_workspace_page";*/
    public static final String TN_KB_PAGE_CONTENT = NULL_KEY; /*"kb_page_content";*/
    public static final String TN_KB_TAG = NULL_KEY; /*"kb_tag";*/
    public static final String TN_KB_PAGE_COMMENT = NULL_KEY; /*"kb_page_comment";*/


    private EncryptConstants() throws InstantiationException {
        throw new InstantiationException();
    }
}

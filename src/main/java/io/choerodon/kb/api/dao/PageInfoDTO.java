package io.choerodon.kb.api.dao;

import java.util.Collections;
import java.util.List;

/**
 * Created by Zenger on 2019/4/29.
 */
public class PageInfoDTO {

    public PageInfoDTO() {
        this.attachment = Collections.emptyList();
        this.comment = Collections.emptyList();
    }

    private List<PageAttachmentDTO> attachment;
    private List<PageCommentDTO> comment;

    public List<PageAttachmentDTO> getAttachment() {
        return attachment;
    }

    public void setAttachment(List<PageAttachmentDTO> attachment) {
        this.attachment = attachment;
    }

    public List<PageCommentDTO> getComment() {
        return comment;
    }

    public void setComment(List<PageCommentDTO> comment) {
        this.comment = comment;
    }
}

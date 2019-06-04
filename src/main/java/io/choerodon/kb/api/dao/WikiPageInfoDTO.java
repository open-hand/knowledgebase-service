package io.choerodon.kb.api.dao;

import java.util.List;

/**
 * Created by Zenger on 2019/5/31.
 */
public class WikiPageInfoDTO {

    private String title;
    private String content;
    private Boolean hasChildren;
    private List<String> children;
    private List<WikiAttachment> attachments;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getHasChildren() {
        return hasChildren;
    }

    public void setHasChildren(Boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    public List<String> getChildren() {
        return children;
    }

    public void setChildren(List<String> children) {
        this.children = children;
    }

    public List<WikiAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<WikiAttachment> attachments) {
        this.attachments = attachments;
    }

    public static class WikiAttachment {
        private String filename;
        private Boolean image;
        private String mimeType;
        private byte[] content;

        public String getMimeType() {
            return mimeType;
        }

        public void setMimeType(String mimeType) {
            this.mimeType = mimeType;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public Boolean getImage() {
            return image;
        }

        public void setImage(Boolean image) {
            this.image = image;
        }

        public byte[] getContent() {
            return content;
        }

        public void setContent(byte[] content) {
            this.content = content;
        }
    }
}

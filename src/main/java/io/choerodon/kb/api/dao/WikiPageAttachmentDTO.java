package io.choerodon.kb.api.dao;

/**
 * Created by Zenger on 2019/6/5.
 */
public class WikiPageAttachmentDTO {

    private String name;
    private Long size;
    private String url;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

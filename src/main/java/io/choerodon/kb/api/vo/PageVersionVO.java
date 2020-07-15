package io.choerodon.kb.api.vo;

import io.choerodon.kb.infra.feign.vo.UserDO;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import java.util.Date;

/**
 * @author shinan.chen
 * @since 2019/5/16
 */
public class PageVersionVO {
    @ApiModelProperty(value = "版本id")
    @Encrypt
    private Long id;
    @ApiModelProperty(value = "版本名称")
    private String name;
    @ApiModelProperty(value = "页面id")
    @Encrypt
    private Long pageId;
    @ApiModelProperty(value = "创建日期")
    private Date creationDate;
    @ApiModelProperty(value = "创建用户id")
    private Long createdBy;
    @ApiModelProperty(value = "创建用户对象")
    private UserDO createUser;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getPageId() {
        return pageId;
    }

    public void setPageId(Long pageId) {
        this.pageId = pageId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public UserDO getCreateUser() {
        return createUser;
    }

    public void setCreateUser(UserDO createUser) {
        this.createUser = createUser;
    }
}

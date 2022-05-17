package io.choerodon.kb.api.vo;

import java.util.Date;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.kb.infra.feign.vo.UserDO;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author: 25499
 * @date: 2020/1/2 17:46
 * @description:
 */
public class RecycleVO {
    @ApiModelProperty(value = "回收知识id")
    @Encrypt
    private Long id;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "所属知识库id")
    @Encrypt
    private Long belongToBaseId;

    @ApiModelProperty(value = "所属知识库名称")
    private String belongToBaseName;

    @ApiModelProperty(value = "类型")
    private String type;

    @ApiModelProperty(value = "更新人id")
    private Long lastUpdatedBy;

    @ApiModelProperty(value = "更新时间")
    private Date lastUpdateDate;

    @ApiModelProperty(value = "最后修改用户对象")
    private UserDO lastUpdatedUser;
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

    public Long getBelongToBaseId() {
        return belongToBaseId;
    }

    public void setBelongToBaseId(Long belongToBaseId) {
        this.belongToBaseId = belongToBaseId;
    }

    public String getBelongToBaseName() {
        return belongToBaseName;
    }

    public void setBelongToBaseName(String belongToBaseName) {
        this.belongToBaseName = belongToBaseName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(Long lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public UserDO getLastUpdatedUser() {
        return lastUpdatedUser;
    }

    public void setLastUpdatedUser(UserDO lastUpdatedUser) {
        this.lastUpdatedUser = lastUpdatedUser;
    }
}

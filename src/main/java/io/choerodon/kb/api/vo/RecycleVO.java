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
    @Encrypt
    private Long id;

    private String name;

    @Encrypt
    private Long belongToBaseId;

    private String belongToBaseName;

    private String type;

    private Long lastUpdatedBy;

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

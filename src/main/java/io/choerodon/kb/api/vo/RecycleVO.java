package io.choerodon.kb.api.vo;

import java.util.Date;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import io.choerodon.kb.api.vo.permission.PermissionCheckVO;
import io.choerodon.kb.infra.feign.vo.UserDO;

import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 回收站VO
 * @author 25499 2020/1/2 17:46
 */
@ApiModel(value = "回收站VO")
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
    @ApiModelProperty(value = "权限信息")
    private List<PermissionCheckVO> permissionCheckInfos;

    public Long getId() {
        return id;
    }

    public RecycleVO setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public RecycleVO setName(String name) {
        this.name = name;
        return this;
    }

    public Long getBelongToBaseId() {
        return belongToBaseId;
    }

    public RecycleVO setBelongToBaseId(Long belongToBaseId) {
        this.belongToBaseId = belongToBaseId;
        return this;
    }

    public String getBelongToBaseName() {
        return belongToBaseName;
    }

    public RecycleVO setBelongToBaseName(String belongToBaseName) {
        this.belongToBaseName = belongToBaseName;
        return this;
    }

    public String getType() {
        return type;
    }

    public RecycleVO setType(String type) {
        this.type = type;
        return this;
    }

    public Long getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public RecycleVO setLastUpdatedBy(Long lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
        return this;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public RecycleVO setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
        return this;
    }

    public UserDO getLastUpdatedUser() {
        return lastUpdatedUser;
    }

    public RecycleVO setLastUpdatedUser(UserDO lastUpdatedUser) {
        this.lastUpdatedUser = lastUpdatedUser;
        return this;
    }

    /**
     * @return 权限信息
     */
    public List<PermissionCheckVO> getPermissionCheckInfos() {
        return permissionCheckInfos;
    }

    public RecycleVO setPermissionCheckInfos(List<PermissionCheckVO> permissionCheckInfos) {
        this.permissionCheckInfos = permissionCheckInfos;
        return this;
    }
}

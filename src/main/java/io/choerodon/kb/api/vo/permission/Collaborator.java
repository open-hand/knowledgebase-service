package io.choerodon.kb.api.vo.permission;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;

import io.choerodon.kb.infra.enums.PermissionConstants;
import io.choerodon.kb.infra.feign.vo.UserDO;

import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * Copyright (c) 2022. Zknow Enterprise Solution. All right reserved.
 * 协作者vo，承载了用户、角色、工作组的基本属性
 *
 * @author zongqi.hao@zknow.com
 * @since 2022/9/26
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Collaborator {

    public static Collaborator ofUser(UserDO userDO) {
        Collaborator collaborator = new Collaborator();
        collaborator.setId(userDO.getId());
        collaborator.setName(userDO.getRealName());
        collaborator.setImageUrl(userDO.getImageUrl());
        collaborator.setType(PermissionConstants.PermissionRangeType.USER.toString());
        return collaborator;
    }

    public static Collaborator ofRole(RoleVO roleVO) {
        Collaborator collaborator = new Collaborator();
        collaborator.setId(roleVO.getId());
        collaborator.setName(roleVO.getName());
        collaborator.setType(PermissionConstants.PermissionRangeType.ROLE.toString());
        return collaborator;
    }

    public static Collaborator ofWorkGroup(WorkGroupVO workGroupVO) {
        Collaborator collaborator = new Collaborator();
        collaborator.setId(workGroupVO.getId());
        collaborator.setName(workGroupVO.getName());
        collaborator.setType(PermissionConstants.PermissionRangeType.WORK_GROUP.toString());
        return collaborator;
    }

    @Encrypt
    @ApiModelProperty(value = "id")
    private Long id;
    @ApiModelProperty(value = "名称")
    private String name;
    /**
     * {@link PermissionConstants.PermissionRangeType}
     */
    @ApiModelProperty(value = "协作者类型")
    private String type;

    @Encrypt
    @ApiModelProperty(value = "父级id")
    private Long parentId;

    @ApiModelProperty(value = "头像url")
    private String imageUrl;
    @ApiModelProperty(value = "工作组路径")
    private String workGroupPath;

    @ApiModelProperty(value = "组织层返回项目层角色标记")
    private Boolean projectRoleFlag;
    @ApiModelProperty(value = "角色下的用户数")
    private Integer userUnderRoleCount;


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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getWorkGroupPath() {
        return workGroupPath;
    }

    public void setWorkGroupPath(String workGroupPath) {
        this.workGroupPath = workGroupPath;
    }

    public Boolean getProjectRoleFlag() {
        return projectRoleFlag;
    }

    public void setProjectRoleFlag(Boolean projectRoleFlag) {
        this.projectRoleFlag = projectRoleFlag;
    }

    public Integer getUserUnderRoleCount() {
        return userUnderRoleCount;
    }

    public void setUserUnderRoleCount(Integer userUnderRoleCount) {
        this.userUnderRoleCount = userUnderRoleCount;
    }
}

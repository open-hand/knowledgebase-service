package io.choerodon.kb.api.vo.permission;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;

import io.choerodon.kb.infra.enums.PermissionConstants;

import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 查询已有协作者VO
 * Copyright (c) 2022. Zknow Enterprise Solution. All right reserved.
 *
 * @author zongqi.hao@zknow.com
 * @since 2022/9/26
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PermissionSearchVO {

    /**
     * 前端组织层和项目层使用同一组件，所以后端根据baseTargetType转换为targetType
     *
     * @param projectId 项目id 等于 0 为组织层， 非0时为项目层
     */
    public PermissionSearchVO transformBaseTargetType(Long projectId) {
        // 前端公用组件，不区分项目组织层，后端添加一下后缀
        PermissionConstants.PermissionTargetType permissionTargetType = PermissionConstants.PermissionTargetType.getPermissionTargetType(projectId, this.baseTargetType);
        if(permissionTargetType != null) {
            this.targetType = permissionTargetType.getCode();
        return this;
        }
        return this;
    }

    /**
     * 前端组织层和项目层使用同一组件，所以后端根据此字段转换为 {@link PermissionSearchVO#transformBaseTargetType(Long)}
     */
    @ApiModelProperty(value = "控制对象基础类型")
    @NotBlank
    private String baseTargetType;

    /**
     * 实际使用字段
     * <p>
     * {@link PermissionConstants.PermissionTargetType}
     **/
    @ApiModelProperty(value = "控制对象类型", required = true)
    private String targetType;

    @ApiModelProperty(value = "控制对象")
    @NotNull
    @Encrypt(ignoreValue = {"0"})
    private Long targetValue;

    /**
     * @return 控制对象基础类型
     */
    public String getBaseTargetType() {
        return baseTargetType;
    }

    public PermissionSearchVO setBaseTargetType(String baseTargetType) {
        this.baseTargetType = baseTargetType;
        return this;
    }

    /**
     * @return 控制对象
     */
    public Long getTargetValue() {
        return targetValue;
    }

    public PermissionSearchVO setTargetValue(Long targetValue) {
        this.targetValue = targetValue;
        return this;
    }

    /**
     * @return 控制对象类型
     */
    public String getTargetType() {
        return targetType;
    }

    public PermissionSearchVO setTargetType(String targetType) {
        this.targetType = targetType;
        if(StringUtils.isNotBlank(targetType)) {
            this.baseTargetType = PermissionConstants.PermissionTargetType.of(targetType).getBaseType().toString();
        }
        return this;
    }
}

package io.choerodon.kb.api.vo.permission;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.util.Assert;

import io.choerodon.kb.infra.common.PermissionErrorCode;
import io.choerodon.kb.infra.enums.PageResourceType;
import io.choerodon.kb.infra.enums.PermissionConstants;

import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * Copyright (c) 2022. Zknow Enterprise Solution. All right reserved.
 * 查询已有协作者vo
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
    public void transformBaseTargetType(Long projectId) {
        // 前端公用组件，不区分项目组织层，后端添加一下后缀
        PageResourceType resourceType = projectId == 0 ? PageResourceType.ORGANIZATION : PageResourceType.PROJECT;
        PermissionConstants.PermissionTargetType permissionTargetType = PermissionConstants.PermissionTargetType.getBaseTypeTargetTypeMapping()
                .get(PermissionConstants.PermissionTargetBaseType.of(this.getBaseTargetType()), resourceType);
        Assert.notNull(permissionTargetType, PermissionErrorCode.ERROR_TARGET_TYPES);
        this.targetType = permissionTargetType.getCode();
    }

    /**
     * 前端组织层和项目层使用同一组件，所以后端根据此字段转换为 {@link PermissionSearchVO#transformBaseTargetType(Long)}
     */
    @NotBlank
    private String baseTargetType;

    /**
     * 实际使用字段
     */
    private String targetType;

    @NotNull
    @Encrypt(ignoreValue = {"0"})
    private Long targetValue;

    public String getBaseTargetType() {
        return baseTargetType;
    }

    public void setBaseTargetType(String baseTargetType) {
        this.baseTargetType = baseTargetType;
    }

    public Long getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(Long targetValue) {
        this.targetValue = targetValue;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }
}
